package ru.service.session.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketProxy {
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketProxy(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessage(String userId, String destination, Object payload) {
        log.info("Sending message to user id: {} по: {}", userId, destination);
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
    }
}
