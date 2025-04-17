package com.novel.vippro.exception;

import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.security.jwt.AuthTokenFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        Map<String, String> errors = new HashMap<>();

        if (ex.getParameter().getParameterType().equals(UUID.class)) {
            errors.put("error", "Invalid ID format. Please provide a valid UUID.");
        } else {
            errors.put("error", "Invalid parameter format: " + ex.getName());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Invalid parameter format", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Validation failed", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("resource", ex.getResourceName());
        errors.put("field", ex.getFieldName());
        errors.put("value", ex.getFieldValue().toString());
        errors.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ControllerResponse.error("Resource not found", errors, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Runtime error", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleAccessDeniedException(
            AccessDeniedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Access denied. You don't have permission to perform this action.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ControllerResponse.error("Access denied", errors, HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleBadCredentialsException(
            BadCredentialsException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Invalid username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerResponse.error("Authentication failed", errors, HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleGenericException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ControllerResponse.error("Internal server error", errors,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}