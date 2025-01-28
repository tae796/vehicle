package com.example.vehicle.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(final CustomException e) {
        ErrorResponse response = ErrorResponse
                .builder()
                .status(e.getErrorCode().getStatus())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(final MissingServletRequestPartException e) {
        ErrorResponse response = ErrorResponse
                .builder()
                .status(400)
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected  ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        ErrorResponse response = ErrorResponse
                .builder()
                .status(ErrorCode.INVALID_REQUEST_PART.getStatus())
                .message(ErrorCode.INVALID_REQUEST_PART.getMessage())
                .errors(e.getBindingResult())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}
