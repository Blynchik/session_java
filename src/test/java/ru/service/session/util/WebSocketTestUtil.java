package ru.service.session.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class WebSocketTestUtil {

    public static String lastHandledErrorPayload;

    public WebSocketStompClient createWebSocketClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        return stompClient;
    }

    public StompSession connectWebSocket(String token, Integer port, WebSocketStompClient stompClient) throws Exception {
        // Подключаемся к WebSocket с помощью STOMP
        String url = String.format("ws://localhost:%s/ws", port);
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", String.format("Bearer %s", token));  // Добавляем JWT токен в заголовки
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", token));
        // работает, просто deprecated
        return stompClient.connect(url, headers, connectHeaders, new MyStompSessionHandler()).get();
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    public static class MyStompSessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("Stomp client is connected");
            super.afterConnected(session, connectedHeaders);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            lastHandledErrorPayload = new String(payload, StandardCharsets.UTF_8);
            log.error("Error payload: {}", lastHandledErrorPayload);
            super.handleException(session, command, headers, payload, exception);
        }
    }

    public static class MyStompFrameHandler implements StompFrameHandler {

        private final Consumer<JsonNode> frameHandler;

        public MyStompFrameHandler(Consumer<JsonNode> frameHandler) {
            this.frameHandler = frameHandler;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return JsonNode.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            log.info("received message: {} with headers: {}", payload, headers);
            JsonNode jsonNode = (JsonNode) payload;
            frameHandler.accept(jsonNode);
        }
    }
}