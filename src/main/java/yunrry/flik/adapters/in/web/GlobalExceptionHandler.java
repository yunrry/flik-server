package yunrry.flik.adapters.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yunrry.flik.adapters.in.dto.Response;
import yunrry.flik.core.domain.exception.EmailAlreadyExistsException;
import yunrry.flik.core.domain.exception.InvalidPasswordException;
import yunrry.flik.core.domain.exception.InvalidTokenException;
import yunrry.flik.core.domain.exception.UserNotFoundException;
import yunrry.flik.core.domain.exception.common.FlikException;
import yunrry.flik.adapters.in.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FlikException.class)
    public ResponseEntity<ErrorResponse> handleFlikException(FlikException ex) {
        ErrorResponse response = new ErrorResponse(ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Response<Void>> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Response.error("유효하지 않은 토큰입니다"));
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<Response<Void>> handleExpiredToken(io.jsonwebtoken.ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Response.error("만료된 토큰입니다"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Response<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Response.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Response<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<Void>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Response.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response.error(ex.getMessage()));
    }
}
