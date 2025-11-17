package com.ruhcom.KafkaAvro.exception;

import com.ruhcom.KafkaAvro.dto.ApiResponseDTO;
import jakarta.xml.bind.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice()
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(404).body(new ApiResponseDTO("404", ex.getMessage(), null, false));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponseDTO> handleValidationException(ValidationException ex) {
        log.warn("ValidationException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponseDTO("400", ex.getMessage(), null, false));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponseDTO("400", ex.getMessage(), null, false));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponseDTO> handleIOException(IOException ex) {
        log.error("IOException: {}", ex.getMessage());
        return ResponseEntity.status(500).body(new ApiResponseDTO("500", "IO operation failed: " + ex.getMessage(), null, false));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage());
        return ResponseEntity.status(500).body(new ApiResponseDTO("500", "Internal Server Error : "+ex.getMessage(), null, false));
    }
}
