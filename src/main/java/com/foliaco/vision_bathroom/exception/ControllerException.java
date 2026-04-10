package com.foliaco.vision_bathroom.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerException {
    
    @ExceptionHandler({ NotFoundException.class })
    public ProblemDetail badRequestException(RuntimeException runtimeException) {

        ErrorMessage errorMessage = ErrorMessage.builder()
        .message(runtimeException.getMessage())
        .timestamp(LocalDateTime.now())
        .statusCode(HttpStatus.BAD_REQUEST.value())                                    
        .build();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage.getMessage());
        problemDetail.setTitle("Bad request");
        problemDetail.setProperty("timestamp", errorMessage.getTimestamp());

        return problemDetail;
    }

    @ExceptionHandler({UnauthorizedException.class, InvalidGoogleTokenException.class})
    public ProblemDetail unauthorizedException(RuntimeException runtimeException) {

        ErrorMessage errorMessage = ErrorMessage.builder()
                .message(runtimeException.getMessage())
                .timestamp(LocalDateTime.now())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, errorMessage.getMessage());

        problemDetail.setTitle("Unauthorized");
        problemDetail.setProperty("timestamp", errorMessage.getTimestamp());
        problemDetail.setProperty("statusCode", errorMessage.getStatusCode());

        return problemDetail;
    }


}
