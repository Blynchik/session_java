package ru.service.session.config.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.service.session.config.common.ServerProperties;
import ru.service.session.dto.exception.ExceptionInfo;
import ru.service.session.dto.exception.ExceptionResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@NoArgsConstructor
public class ClientErrorDecoder implements ErrorDecoder {

    private ServerProperties serverProperties;
    private ObjectMapper objectMapper;

    @Autowired
    public ClientErrorDecoder(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("An error was received from the server");
        // Считываем тело ответа при ошибке
        String errorBody = null;
        try {
            //feignClient не декодирует тело данных ошибок
            if (response.status() == 401 || response.status() == 403) {
                errorBody = new ObjectMapper().writeValueAsString(
                        new ExceptionResponse(List.of(new ExceptionInfo("", "", "Authorization or authentication error")),
                               "", ""));
            } else {
                errorBody = new BufferedReader(new InputStreamReader(response.body().asInputStream()))
                        .lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Пробрасываем исходный ответ сервера с кодом ошибки
        return new ResponseStatusException(
                HttpStatus.valueOf(response.status()),
                errorBody
        );
    }
}