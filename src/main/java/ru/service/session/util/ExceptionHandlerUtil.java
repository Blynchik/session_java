package ru.service.session.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import ru.service.session.dto.exception.ExceptionInfo;
import ru.service.session.dto.exception.ExceptionResponse;
import ru.service.session.util.exception.BindingValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Slf4j
public class ExceptionHandlerUtil {

    public static ExceptionResponse deserializeExceptionResponseFromResponseStatusException(Exception e,
                                                                                            ObjectMapper objectMapper,
                                                                                            Consumer<Exception> exceptionHandler) {
        ExceptionResponse exceptionResponse = null;
        try {
            String json = e.getMessage().substring(e.getMessage().indexOf("{"), e.getMessage().length() - 1);
            exceptionResponse = objectMapper.readValue(json, ExceptionResponse.class);
        } catch (Exception ex) {
            exceptionHandler.accept(ex);
        }
        return exceptionResponse;
    }

    public static List<ExceptionInfo> getExceptionInfoList(Exception e) {
        return List.of(new ExceptionInfo(e.getClass().getSimpleName(), "", e.getMessage()));
    }

    public static void logException(Exception e) {
        log.error("""
                exception: {}, message: {}
                """, e.getClass().getSimpleName(), e.getMessage());
        e.printStackTrace();
    }

    /**
     * Метод преобразует все ошибки BindingResult в список ExceptionInfo
     */
    public static List<ExceptionInfo> getExceptionInfoList(BindingResult bindingResult) {
        List<ExceptionInfo> errorMessages = new ArrayList<>();
        for (ObjectError error : bindingResult.getAllErrors()) {
            String fieldName = "";
            String defaultMessage = error.getDefaultMessage();
            // Если ошибка связана с конкретным полем
            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
                defaultMessage = fieldError.getDefaultMessage();
            }
            // Формируем сообщение об ошибке
            errorMessages.add(new ExceptionInfo(
                    BindingValidationException.class.getSimpleName(),
                    fieldName,
                    defaultMessage != null ? defaultMessage : ""));
        }
        return errorMessages;
    }
}
