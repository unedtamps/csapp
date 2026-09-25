package com.mycomp.csapp.shared.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {
  private final HttpStatus status;

  public ApplicationException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }
}
