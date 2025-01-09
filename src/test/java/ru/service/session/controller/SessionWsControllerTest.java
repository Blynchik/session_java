package ru.service.session.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import ru.service.session.client.EventApi;
import ru.service.session.client.HeroApi;
import ru.service.session.dto.event.ws.DecisionResultWsResponse;
import ru.service.session.dto.event.ws.DecisionWsResponse;
import ru.service.session.dto.event.ws.EventWsResponse;
import ru.service.session.dto.exception.ExceptionResponse;
import ru.service.session.dto.hero.HeroRequest;
import ru.service.session.util.WebSocketTestUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static ru.service.session.util.ObjectFactory.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class SessionWsControllerTest {

    @LocalServerPort
    private int port; // Инжектируем случайный порт

    private ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    private WebSocketTestUtil webSocketTestUtil;
    private HeroApi heroApi;
    private EventApi eventApi;

    @Autowired
    public SessionWsControllerTest(MockMvc mockMvc,
                                   HeroApi heroApi,
                                   EventApi eventApi) {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = mockMvc;
        this.webSocketTestUtil = new WebSocketTestUtil();
        this.heroApi = heroApi;
        this.eventApi = eventApi;
    }

    @Nested
    @DisplayName(value = "Тесты на авторизацию")
    class AuthTest {
        private WebSocketStompClient stompClient;
        private CompletableFuture<String> completableFuture;
        private StompSession session;

        @BeforeEach
        public void setup() throws Exception {
            // Инициализация WebSocketStompClient
            this.stompClient = webSocketTestUtil.createWebSocketClient();
            this.completableFuture = new CompletableFuture<>();
        }

        @AfterEach
        public void tearDown() throws Exception {
            if (session != null) {
                session.disconnect();
            }
            stompClient.stop();
        }

        @Test
        @Description(value = "Тест на начало нового события без токена")
        void nullToken() throws Exception {
            //given
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0";

            //when
            WebSocketStompClient stompClient = webSocketTestUtil.createWebSocketClient();

            // Подключаемся к WebSocket без токена
            ExecutionException exception = assertThrows(ExecutionException.class,
                    () -> session = webSocketTestUtil.connectWebSocket(null, port, stompClient));

            //then
            // Проверяем, что причина исключения — ConnectionLostException
            assertTrue(objectMapper.readValue(WebSocketTestUtil.lastHandledErrorPayload, ExceptionResponse.class)
                    .getExceptions().get(0).getDescr().contains(expectedErrorMessage));
            Throwable cause = exception.getCause();
            assertTrue(cause instanceof ConnectionLostException, "Причиной исключения должно быть ConnectionLostException");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "      "})
        @Description(value = "Тест на начало нового события без токена")
        void emptyBlankToken(String tokenForTest) throws Exception {
            //given
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "CharSequence cannot be null or empty.";

            //when
            WebSocketStompClient stompClient = webSocketTestUtil.createWebSocketClient();

            // Подключаемся к WebSocket без токена
            ExecutionException exception = assertThrows(ExecutionException.class,
                    () -> session = webSocketTestUtil.connectWebSocket(tokenForTest, port, stompClient));

            //then
            // Проверяем, что причина исключения — ConnectionLostException
            assertTrue(objectMapper.readValue(WebSocketTestUtil.lastHandledErrorPayload, ExceptionResponse.class)
                    .getExceptions().get(0).getDescr().contains(expectedErrorMessage));
            Throwable cause = exception.getCause();
            assertTrue(cause instanceof ConnectionLostException, "Причиной исключения должно быть ConnectionLostException");
        }

        @Test
        @Description(value = "Тест на начало нового события с неверным токеном")
        void wrongToken() throws Exception {
            //given
            String token = getChangedSignToken();
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.";

            //when
            WebSocketStompClient stompClient = webSocketTestUtil.createWebSocketClient();

            // Подключаемся к WebSocket без токена
            ExecutionException exception = assertThrows(ExecutionException.class,
                    () -> session = webSocketTestUtil.connectWebSocket(token, port, stompClient));

            //then
            // Проверяем, что причина исключения — ConnectionLostException
            assertTrue(objectMapper.readValue(WebSocketTestUtil.lastHandledErrorPayload, ExceptionResponse.class)
                    .getExceptions().get(0).getDescr().contains(expectedErrorMessage));
            Throwable cause = exception.getCause();
            assertTrue(cause instanceof ConnectionLostException, "Причиной исключения должно быть ConnectionLostException");
        }

        @Test
        @Description(value = "Тест на начало нового события с просроченным токеном")
        void expiredToken() throws Exception {
            //given
            String token = getExpiredToken();
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "JWT expired";

            //when
            WebSocketStompClient stompClient = webSocketTestUtil.createWebSocketClient();

            // Подключаемся к WebSocket без токена
            ExecutionException exception = assertThrows(ExecutionException.class,
                    () -> session = webSocketTestUtil.connectWebSocket(token, port, stompClient));

            //then
            // Проверяем, что причина исключения — ConnectionLostException
            assertTrue(objectMapper.readValue(WebSocketTestUtil.lastHandledErrorPayload, ExceptionResponse.class)
                    .getExceptions().get(0).getDescr().contains(expectedErrorMessage));
            Throwable cause = exception.getCause();
            assertTrue(cause instanceof ConnectionLostException, "Причиной исключения должно быть ConnectionLostException");
        }
    }

    @Nested
    @DisplayName(value = "Тесты на начало случайного события c авто-выбором")
    class StartRandomEventTest {

        private String userAsString;
        private String heroAsString;
        private WebSocketStompClient stompClient;
        private CompletableFuture<String> completableFuture;
        private StompSession session;

        @BeforeEach
        public void setup() throws Exception {
            // Инициализация WebSocketStompClient
            this.stompClient = webSocketTestUtil.createWebSocketClient();
            this.completableFuture = new CompletableFuture<>();
        }

        @AfterEach
        public void tearDown() throws Exception {
            if (session != null) {
                session.disconnect();
            }
            stompClient.stop();
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с авто-выбором")
        void success() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest1());
            eventApi.create("Bearer " + token, getEventRequest1());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            //then
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest1().getTitle(), decisionResultResponseObject.getEventTitle());
        }

        @Test
        @Description(value = "Тест на начало нового события, если событие уже начато")
        void eventAlreadyStarted() throws Exception {
            //given
            String token = getEternalAdminToken();
//            heroApi.create("Bearer " + token, getHeroRequest2());
//            eventApi.create("Bearer " + token, getEventRequest1());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "The current event is not finished yet";
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            Thread.currentThread().sleep(1000);
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());
            Thread.currentThread().sleep(1000);

            //when
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            completableFuture = new CompletableFuture<>();
            String exceptionResponse = completableFuture.get(2, TimeUnit.SECONDS);
            ExceptionResponse response = objectMapper.readValue(exceptionResponse, ExceptionResponse.class);

            //then
            assertEquals(expectedErrorMessage, response.getExceptions().get(0).getDescr());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(1000);
            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest1().getTitle(), decisionResultResponseObject.getEventTitle());
        }

        @Test
        @Description(value = "Тест на начало нового события, если событие уже начато, но прошло время жизни сессии")
        void eventAlreadyStartedButSessionLifetimeExpired() throws Exception {
            //given
            String token = getEternalAdminToken();
//            heroApi.create("Bearer " + token, getHeroRequest2());
//            eventApi.create("Bearer " + token, getEventRequest1());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            Thread.currentThread().sleep(1000);
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());
            Thread.currentThread().sleep(65000);

            //when
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            completableFuture = new CompletableFuture<>();
            //then
            // Ожидаем первое сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse secondEventResponseObject = objectMapper.readValue(secondResponse, EventWsResponse.class);
            assertEquals(1, secondEventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String thirdResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(thirdResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest1().getTitle(), decisionResultResponseObject.getEventTitle());
        }

        @Test
        @Description(value = "Тест на начало нового события, если нет героя")
        void noHero() throws Exception {
            //given
            String token = getEternalToken();
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "The hero was not found";
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            Thread.currentThread().sleep(5000);
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            ExceptionResponse response = objectMapper.readValue(firstResponse, ExceptionResponse.class);

            assertEquals(expectedErrorMessage, response.getExceptions().get(0).getDescr());
        }

        @Test
        @Description(value = "Тест на начало нового события, если нет события")
        void noEvent() throws Exception {
            //given
            String token = getEternalToken();
            heroApi.create("Bearer " + token, getHeroRequest2());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "Event not found";
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            Thread.currentThread().sleep(5000);
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            ExceptionResponse response = objectMapper.readValue(firstResponse, ExceptionResponse.class);

            assertEquals(expectedErrorMessage, response.getExceptions().get(0).getDescr());
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с авто-выбором и если решения одной категории, но с разной сложностью")
        void sameCharacteristicDecisionTypes() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest1());
            eventApi.create("Bearer " + token, getEventRequest2());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            //then
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest2().getTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(getEventRequest2().getDecisions().get(2).getDescription(),
                    decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > getEventRequest2().getDecisions().get(2).getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= getEventRequest2().getDecisions().get(2).getDifficulty());
            }
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с авто-выбором и если решения одной категории, но с одной сложностью, выбирается первое")
        void sameCharacteristicDecisionTypesAndDifficulties() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest1());
            eventApi.create("Bearer " + token, getEventRequest5());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            //then
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest5().getTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(getEventRequest5().getDecisions().get(0).getDescription(),
                    decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > getEventRequest5().getDecisions().get(0).getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= getEventRequest5().getDecisions().get(0).getDifficulty());
            }
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с авто-выбором по наибольшей характеристике и наименьшей сложностью в типе")
        void strongestCharacteristicAndMinDifficulty() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest3());
            eventApi.create("Bearer " + token, getEventRequest3());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            //then
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest3().getTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(getEventRequest3().getDecisions().get(1).getDescription(),
                    decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > getEventRequest3().getDecisions().get(1).getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= getEventRequest3().getDecisions().get(1).getDifficulty());
            }
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с авто-выбором, если нет выборов с проверкой характеристик, выбирается первое текстовое")
        void autoChoiceWhenNoCharacteristicDecision() throws Exception {
            //given
            String token = getEternalAdminToken();
//            heroApi.create("Bearer " + token, getHeroRequest3());
//            eventApi.create("Bearer " + token, getEventRequest4());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            //when
            // Подписка на /user/topic/event, чтобы получать ответы
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);

            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);

            //then
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());

            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(15000);

            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(1000, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(getEventRequest4().getTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(getEventRequest4().getDecisions().get(0).getDescription(),
                    decisionResultResponseObject.getDecisionDescr());
            assertEquals(getEventRequest4().getDecisions().get(0).getResults().get(true).getResultDescr(),
                    decisionResultResponseObject.getResultDescr());
        }
    }

    //
    @Nested
    @DisplayName(value = "Тесты на выбор решения случайного события")
    class ProcessUserDecisionTest {
        private String userAsString;
        private HeroRequest heroRequest;
        private String heroAsString;
        private WebSocketStompClient stompClient;
        private CompletableFuture<String> completableFuture;
        private StompSession session;

        @BeforeEach
        public void setup() throws Exception {
            // Инициализация WebSocketStompClient
            this.stompClient = webSocketTestUtil.createWebSocketClient();
            this.completableFuture = new CompletableFuture<>();
            this.heroRequest = getHeroRequest2();
            this.heroAsString = objectMapper.writeValueAsString(heroRequest);
        }

        @AfterEach
        public void tearDown() throws Exception {
            if (session != null) {
                session.disconnect();
            }
            stompClient.stop();
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с выбором пользователя")
        void success() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest2());
            eventApi.create("Bearer " + token, getEventRequest2());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());
            DecisionWsResponse chosenDecision = eventResponseObject.getDecisions().get(1);
            Thread.currentThread().sleep(1000);

            //when
            session.send("/app/choose-decision", chosenDecision.getId());

            //then
            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(1000);
            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(chosenDecision.getEventTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(chosenDecision.getDescription(), decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > chosenDecision.getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= chosenDecision.getDifficulty());
            }
        }

        @Test
        @Description(value = "Тест на успешность начала нового события с повторным выбором пользователя")
        void doubleChooseDecision() throws Exception {
            //given
            String token = getEternalAdminToken();
            heroApi.create("Bearer " + token, getHeroRequest2());
            eventApi.create("Bearer " + token, getEventRequest1());
            // Ожидаемое сообщение об ошибке
            String expectedErrorMessage = "Decision already selected";
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            DecisionWsResponse chosenDecision = eventResponseObject.getDecisions().get(1);
            Thread.currentThread().sleep(1000);
            session.send("/app/choose-decision", chosenDecision.getId());
            Thread.currentThread().sleep(1000);

            //when
            DecisionWsResponse secondChosenDecision = eventResponseObject.getDecisions().get(2);
            session.send("/app/choose-decision", secondChosenDecision.getId());
            completableFuture = new CompletableFuture<>();
            String exceptionResponse = completableFuture.get(5, TimeUnit.SECONDS);
            ExceptionResponse response = objectMapper.readValue(exceptionResponse, ExceptionResponse.class);

            //then
            assertEquals(expectedErrorMessage, response.getExceptions().get(0).getDescr());
            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(1000);
            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(chosenDecision.getEventTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(chosenDecision.getDescription(),
                    decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > chosenDecision.getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= chosenDecision.getDifficulty());
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
        @Description(value = "Тест на вычисление успешности выбора")
        void chosenDecisionSuccess() throws Exception {
            //given
            String token = getEternalAdminToken();
//            heroApi.create("Bearer " + token, getHeroRequest2());
//            eventApi.create("Bearer " + token, getEventRequest1());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            DecisionWsResponse chosenDecision = eventResponseObject.getDecisions().get(1);
            assertEquals(1, eventResponseObject.getId());
            Thread.currentThread().sleep(1000);

            //when
            session.send("/app/choose-decision", chosenDecision.getId());

            //then
            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(1000);
            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(chosenDecision.getEventTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(chosenDecision.getDescription(), decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > chosenDecision.getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= chosenDecision.getDifficulty());
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "     ", "1000", "F"})
        @Description(value = "Тест на успешность начала нового события с неверными id решений")
        void wrongDecision(String decisionId) throws Exception {
            //given
            String token = getEternalAdminToken();
//            heroApi.create("Bearer " + token, getHeroRequest2());
//            eventApi.create("Bearer " + token, getEventRequest2());
            session = webSocketTestUtil.connectWebSocket(token, port, stompClient);
            session.subscribe("/user/topic/event", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            session.subscribe("/user/topic/error", new WebSocketTestUtil.MyStompFrameHandler(
                    (payload) -> {
                        try {
                            completableFuture.complete(objectMapper.writeValueAsString(payload));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));
            Thread.currentThread().sleep(1000);
            // Отправляем сообщение на /app/random-event
            session.send("/app/random-event", null);
            // Ожидаем первое сообщение
            String firstResponse = completableFuture.get(40, TimeUnit.SECONDS);
            EventWsResponse eventResponseObject = objectMapper.readValue(firstResponse, EventWsResponse.class);
            assertEquals(1, eventResponseObject.getId());
            DecisionWsResponse chosenDecision = eventResponseObject.getDecisions().get(1);
            Thread.currentThread().sleep(1000);

            //when
            session.send("/app/choose-decision", decisionId);

            completableFuture = new CompletableFuture<>();
            String exceptionResponse = completableFuture.get(5, TimeUnit.SECONDS);
            ExceptionResponse response = objectMapper.readValue(exceptionResponse, ExceptionResponse.class);
            assertEquals(response.getClass(), ExceptionResponse.class);

            //then
            // Обновляем CompletableFuture для второго сообщения
            completableFuture = new CompletableFuture<>();
            Thread.currentThread().sleep(1000);
            // Ожидаем второе сообщение
            String secondResponse = completableFuture.get(40, TimeUnit.SECONDS);
            DecisionResultWsResponse decisionResultResponseObject = objectMapper.readValue(secondResponse, DecisionResultWsResponse.class);
            assertEquals(chosenDecision.getEventTitle(), decisionResultResponseObject.getEventTitle());
            assertEquals(chosenDecision.getDescription(), decisionResultResponseObject.getDecisionDescr());
            if (decisionResultResponseObject.getResult()) {
                assertTrue(decisionResultResponseObject.getValue() > chosenDecision.getDifficulty());
            } else {
                assertTrue(decisionResultResponseObject.getValue() <= chosenDecision.getDifficulty());
            }
        }
    }
}

