package ru.service.session.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.service.session.config.common.ServerProperties;
import ru.service.session.dto.exception.ExceptionInfo;
import ru.service.session.dto.exception.ExceptionResponse;
import ru.service.session.util.ExceptionHandlerUtil;
import ru.service.session.util.exception.BindingValidationException;
import ru.service.session.util.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static ru.service.session.util.ExceptionHandlerUtil.*;
import static ru.service.session.util.ExceptionHandlerUtil.getExceptionInfoList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ServerProperties serverProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public GlobalExceptionHandler(ServerProperties serverProperties,
                                  ObjectMapper objectMapper) {
        this.serverProperties = serverProperties;
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<ExceptionResponse> handleException(ResponseStatusException ex) {
        logException(ex);
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(deserializeExceptionResponseFromResponseStatusException(ex, objectMapper, this::handleException));
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(SignatureException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(Exception e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(BindingValidationException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e.getBindingResult()), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(ValidationException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(BadCredentialsException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(InternalAuthenticationServiceException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(EntityNotFoundException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(JwtException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(HttpMessageNotReadableException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(IllegalArgumentException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(HandlerMethodValidationException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(NoResourceFoundException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> handleException(IllegalStateException e) {
        logException(e);
        ExceptionResponse response = new ExceptionResponse(getExceptionInfoList(e), serverProperties.getName(), serverProperties.getPort());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
