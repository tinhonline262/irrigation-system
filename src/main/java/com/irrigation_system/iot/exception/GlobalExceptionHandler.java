package com.irrigation_system.iot.exception;

import com.irrigation_system.iot.dto.ErrorResponseDTO;
import com.irrigation_system.iot.utility.ExceptionHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler({
            ResourceNotFoundException.class, InvalidTokenException.class, BlacklistedTokenException.class,
            SignUpNotValidException.class, LoginNotValidException.class, ResourceAlreadyExistsException.class, IllegalStateException.class
    })
    ResponseEntity<ErrorResponseDTO> handleBadRequestsException(RuntimeException ex, WebRequest request) {
        return ExceptionHandlerUtils.generateErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation errors.
     *
     * @param ex the exception
     * @return error response with status 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
