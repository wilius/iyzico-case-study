package com.iyzico.challenge.integrator.controller.interceptor;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.iyzico.challenge.integrator.dto.BadRequestResponse;
import com.iyzico.challenge.integrator.dto.ErrorCode;
import com.iyzico.challenge.integrator.exception.BaseIntegratorException;
import com.iyzico.challenge.integrator.exception.auth.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.iyzico.challenge.integrator.dto.ErrorCode.UNEXPECTED_ERROR;

@RestControllerAdvice
public class ErrorAdvice {
    private final Logger log = LoggerFactory.getLogger(ErrorAdvice.class);

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BadRequestResponse handle(Exception e) {
        log.warn(e.getMessage(), e);
        return new BadRequestResponse(UNEXPECTED_ERROR, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BadRequestResponse handleConverterErrors(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        return new BadRequestResponse(ErrorCode.INVALID_REQUEST, error.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BadRequestResponse handleConverterErrors(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException e1 = (InvalidFormatException) e.getCause();
            message = String.format("Invalid value for path '%s'. ", getFieldPath(e1));
            if (e1.getTargetType().equals(LocalDate.class)) {
                message += String.format("It is a date and its format is 'yyyy-MM-dd' but your value is '%s'", e1.getValue());
            } else if (e1.getTargetType().equals(LocalTime.class)) {
                message += String.format("It is a time and its format is 'hh:mm:ss' but your value is '%s'", e1.getValue());
            } else if (e1.getTargetType().equals(LocalDate.class)) {
                message += String.format("It is a datetime and its format is 'yyyy-MM-DD hh:mm:ss' but your value is '%s'", e1.getValue());
            } else if (e1.getTargetType().isPrimitive()) {
                message += String.format("It is a %s but your value is '%s'", e1.getTargetType().getSimpleName(), e1.getValue());
            }
        } else if (e.getCause() instanceof JsonMappingException) {
            if (e.getCause().getCause() instanceof BaseIntegratorException) {
                return handle((BaseIntegratorException) e.getCause().getCause());
            }
        }

        return new BadRequestResponse(ErrorCode.INVALID_REQUEST, message);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BaseIntegratorException.class)
    public BadRequestResponse handle(BaseIntegratorException e) {
        return new BadRequestResponse(e.getErrorCode(), e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizationException.class)
    public BadRequestResponse handle(AuthorizationException e) {
        return new BadRequestResponse(e.getErrorCode(), e.getMessage());
    }

    private String getFieldPath(InvalidFormatException e) {
        StringBuilder path = new StringBuilder();
        for (JsonMappingException.Reference reference : e.getPath()) {
            if (path.length() > 0) {
                path.append(".");
            }
            path.append(reference.getFieldName());
        }
        return path.toString();
    }
}
