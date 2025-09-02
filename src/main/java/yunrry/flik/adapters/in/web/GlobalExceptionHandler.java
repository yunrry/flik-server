package yunrry.flik.adapters.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yunrry.flik.core.domain.exception.common.FlikException;
import yunrry.flik.adapters.in.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FlikException.class)
    public ResponseEntity<ErrorResponse> handleFlikException(FlikException ex) {
        ErrorResponse response = new ErrorResponse(ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
