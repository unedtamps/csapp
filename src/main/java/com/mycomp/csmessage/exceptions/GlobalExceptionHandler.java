package com.mycomp.csmessage.exceptions;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private ResponseEntity<ErrorResponse> buildError(
      HttpStatus status, String message, HttpServletRequest req) {

    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                req.getRequestURI()));
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleApp(BaseException ex, HttpServletRequest req) {

    return buildError(ex.getStatus(), ex.getMessage(), req);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(
      MissingRequestHeaderException ex, HttpServletRequest req) {

    return buildError(
        HttpStatus.UNAUTHORIZED, "Missing required header: " + ex.getHeaderName(), req);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {

    String message =
        String.format(
            "HTTP method '%s' is not supported for this endpoint. Supported methods are: %s",
            ex.getMethod(), ex.getSupportedHttpMethods());

    return buildError(HttpStatus.METHOD_NOT_ALLOWED, message, req);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      NoHandlerFoundException ex, HttpServletRequest req) {

    return buildError(
        HttpStatus.NOT_FOUND,
        "No resource found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
        req);
  }
}
