package com.novel.vippro.exception;

import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.security.jwt.AuthTokenFilter;

import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

        logger.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
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
        logger.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
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

        logger.error("ResourceNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ControllerResponse.error("Resource not found", errors, HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        logger.error("RuntimeException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Runtime error", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleAccessDeniedException(
            AccessDeniedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Access denied. You don't have permission to perform this action.");
        logger.error("AccessDeniedException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ControllerResponse.error("Access denied", errors, HttpStatus.FORBIDDEN.value()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleBadCredentialsException(
            BadCredentialsException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Invalid username or password.");
        logger.error("BadCredentialsException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ControllerResponse.error("Authentication failed", errors, HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleGenericException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "An unexpected error occurred. Please try again later.");
        logger.error("Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ControllerResponse.error("Internal server error", errors,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "HTTP method not supported: " + ex.getMethod());
        logger.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ControllerResponse.error("Method not allowed", errors, HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Malformed JSON request");
        logger.error("HttpMessageNotReadableException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Invalid JSON input", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            errors.put(field, violation.getMessage());
        });
        logger.error("ConstraintViolationException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Validation error", errors, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ControllerResponse<Map<String, String>>> handleIllegalArgument(
            IllegalArgumentException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        logger.error("IllegalArgumentException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ControllerResponse.error("Invalid argument", errors, HttpStatus.BAD_REQUEST.value()));
    }
}
