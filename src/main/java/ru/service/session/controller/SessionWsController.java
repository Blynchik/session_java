package ru.service.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import ru.service.session.config.common.ServerProperties;
import ru.service.session.config.security.JwtUser;
import ru.service.session.config.websocket.WebSocketProxy;
import ru.service.session.dto.exception.ExceptionResponse;
import ru.service.session.facade.SessionFacade;

import java.util.concurrent.ExecutionException;

import static ru.service.session.util.ExceptionHandlerUtil.*;

@Controller
@Slf4j
public class SessionWsController {

    private final SessionFacade sessionFacade;
    private final WebSocketProxy webSocketProxy;
    private final ServerProperties serverProperties;
    private final ObjectMapper objectMapper;

    //todo
    // тесты на null решение
    // на решение без события
    // добавить уникальный идентификатор текущего события, чтобы различать на UI
    // добавить в ответ тип ответа, чтобы различать типы ответов на UI

    @Autowired
    public SessionWsController(SessionFacade sessionFacade,
                               WebSocketProxy webSocketProxy,
                               ServerProperties serverProperties,
                               ObjectMapper objectMapper) {
        this.sessionFacade = sessionFacade;
        this.webSocketProxy = webSocketProxy;
        this.serverProperties = serverProperties;
        this.objectMapper = objectMapper;
    }

    @MessageMapping("/random-event")
    @SendToUser("/topic/event")
    public void startRandomEvent(Authentication authentication) throws ExecutionException, InterruptedException {
        JwtUser principal = (JwtUser) authentication.getPrincipal();
        log.info("Request to /app/random-event from user id: {}", principal.getUserId());
        sessionFacade.processRandomEvent(principal.getUserId(), principal.getJwt());
    }

    @MessageMapping("/choose-decision")
    @SendToUser("/topic/event")
    public void processUserDecision(Authentication authentication,
                                    String chosenDecisionId) {
        JwtUser principal = (JwtUser) authentication.getPrincipal();
        log.info("Request to /app/choose-decision from: {} with decision id: {}", principal.getUserId(), chosenDecisionId);
        sessionFacade.setChosenDecisionInCurrentEvent(authentication.getName(), chosenDecisionId);
    }

    @MessageExceptionHandler
    @SendToUser("/topic/error")
    public ExceptionResponse handleException(Exception e) {
        logException(e);
        if (e instanceof ResponseStatusException) {
            return deserializeExceptionResponseFromResponseStatusException(e, objectMapper, this::handleException);
        }
        return new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
    }
}
