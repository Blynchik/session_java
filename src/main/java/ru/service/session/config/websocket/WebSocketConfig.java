package ru.service.session.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import ru.service.session.config.common.ServerProperties;
import ru.service.session.config.security.JwtUser;
import ru.service.session.dto.exception.ExceptionInfo;
import ru.service.session.dto.exception.ExceptionResponse;
import ru.service.session.service.JwtService;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final ServerProperties serverProperties;

    @Autowired
    public WebSocketConfig(JwtService jwtService,
                           ObjectMapper objectMapper,
                           ServerProperties serverProperties) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.serverProperties = serverProperties;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        // Обработка исключений в перехватчиках и самой библиотеке Spring.
        // Завершит соединение и отправит ERROR фрейм клиенту.
        registry.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override
            protected Message<byte[]> handleInternal(
                    StompHeaderAccessor errorHeaderAccessor,
                    byte[] errorPayload,
                    Throwable cause,
                    StompHeaderAccessor clientHeaderAccessor) {
                errorHeaderAccessor.setMessage(null);
                ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(cause), serverProperties.getName(), serverProperties.getPort());
                byte[] messageBytes;
                try {
                    messageBytes = objectMapper.writeValueAsBytes(response);
                } catch (Exception e) {
                    log.error("Error of serialization of error message: {}", e.getMessage());
                    messageBytes = String.format("""
                                            {
                                                "exceptions":
                                                    [
                                                        {
                                                            "exception":"%s",
                                                            "field":"",
                                                            "descr":"%s"
                                                        }
                                                    ],
                                                "server":"%s:%s",
                                                "date":"%s"
                                            }
                                            """,
                                    e.getMessage(), String.format("Error of serialization of error message: %s", e.getMessage()),
                                    serverProperties.getName(), serverProperties.getPort(),
                                    System.currentTimeMillis())
                            .getBytes();
                }
                return MessageBuilder.createMessage(messageBytes, errorHeaderAccessor.getMessageHeaders());
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                try {
                    StompHeaderAccessor accessor =
                            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                    log.info("Headers: {}", accessor);
                    Long userId = null;
                    String login = null;
                    List<String> authorities = new ArrayList<>();
                    assert accessor != null;
                    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

                        String token = authorizationHeader.substring(7);
                        Claims claims = jwtService.extractAllClaims(token);
                        userId = claims.get("userId", Long.class);
                        login = claims.getSubject();
                        authorities = claims.get("authorities", List.class);
                        jwtService.validateToken(token);
                        if (userId != null && !authorities.isEmpty()) {
                            List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList();
                            UserDetails userDetails = new JwtUser(userId.toString(), login, authorities, token);
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, grantedAuthorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            accessor.setUser(authentication);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while web-socket connection: {}", e.getMessage());
                    throw new MessageDeliveryException(e.getMessage());
                }
                return message;
            }
        });
    }

    private List<ExceptionInfo> getExceptionInfoList(Throwable e) {
        return List.of(new ExceptionInfo(e.getClass().getSimpleName(), "", e.getMessage()));
    }
}
