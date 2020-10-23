package com.djcodes.spring.kafka.eventsproducer.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodNotValidException(MethodArgumentNotValidException exception) {

        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + " - " + fieldError.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<?> handleException(ValidationException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMsg());
    }

     @ResponseBody
    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity<?> handleException(JsonProcessingException exception) {

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(exception.getMessage());
    }


}
