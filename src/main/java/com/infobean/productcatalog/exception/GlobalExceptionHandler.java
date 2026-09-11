package com.infobean.productcatalog.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@code @Valid @RequestBody} validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.VALIDATION_FAILED,
                request.getRequestURI(),
                errors
        );
    }

    /**
     * Handles constraint violations on request parameters and path variables.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getParameterValidationResults().forEach(result -> {
            String field = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error ->
                    errors.putIfAbsent(field != null ? field : "request", error.getDefaultMessage()));
        });

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.VALIDATION_FAILED,
                request.getRequestURI(),
                errors
        );
    }

    /**
     * Handles an unknown or unsortable {@code sortBy} property.
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handlePropertyReference(
            PropertyReferenceException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of("sortBy", exception.getMessage())
        );
    }

    /**
     * Handles a request body that fails to parse as JSON.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                ErrorMessages.MALFORMED_REQUEST_BODY,
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Handles a lookup or mutation against a product id that doesn't exist.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Handles an attempt to create or rename a product to a name already in use.
     */
    @ExceptionHandler(DuplicateProductNameException.class)
    public ResponseEntity<ApiError> handleDuplicateProductName(
            DuplicateProductNameException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Handles a database constraint violation, most notably a name-uniqueness race
     * that slips past the pre-save check under concurrent requests.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.CONFLICT,
                ErrorMessages.DATA_INTEGRITY_VIOLATION,
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Handles domain-level argument problems raised directly by application code.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Handles constraint violations outside the normal Spring MVC validation path.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Catch-all for anything not handled above.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessages.UNEXPECTED_ERROR,
                request.getRequestURI(),
                Map.of()
        );
    }

    /**
     * Shared assembly point for every handler above.
     */
    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fieldErrors) {

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );

        return ResponseEntity.status(status).body(error);
    }
}
