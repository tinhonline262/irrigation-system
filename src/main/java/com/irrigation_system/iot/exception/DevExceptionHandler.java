package com.irrigation_system.iot.exception;

import com.irrigation_system.iot.constant.ProfileConstant;
import com.irrigation_system.iot.dto.ErrorResponseDTO;
import com.irrigation_system.iot.utility.ExceptionHandlerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
@Profile(ProfileConstant.DEVELOPMENT)
class DevExceptionHandler {

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex, WebRequest request) {
        return ExceptionHandlerUtils.generateErrorResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ErrorResponseDTO> handleRuntimeException(Exception ex, WebRequest request) {
        return ExceptionHandlerUtils.generateErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }
}
