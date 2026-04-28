package co.com.menor.commerce_core_bd.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MinorExcepcion.class)
    public ResponseEntity<ErrorResponse> handleMinorExcepcion(MinorExcepcion ex) {
        log.warn("Excepcion de negocio: codigo={} mensaje={}", ex.getCodigo(), ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.unprocessableEntity().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Error inesperado", ex);
        ErrorResponse body = ErrorResponse.builder()
                .codigo("ERROR_INTERNO")
                .mensaje("Ocurrió un error inesperado. Contacte al administrador.")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
