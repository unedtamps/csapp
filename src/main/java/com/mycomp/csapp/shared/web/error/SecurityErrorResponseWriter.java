package com.mycomp.csapp.shared.web.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {

  public void writeUnauthorized(HttpServletRequest req, HttpServletResponse res, String message)
      throws IOException {
    writeError(req, res, HttpStatus.UNAUTHORIZED, message);
  }

  public void writeForbidden(HttpServletRequest req, HttpServletResponse res, String message)
      throws IOException {
    writeError(req, res, HttpStatus.FORBIDDEN, message);
  }

  private void writeError(
      HttpServletRequest req, HttpServletResponse res, HttpStatus status, String message)
      throws IOException {

    res.setContentType("application/json");
    res.setStatus(status.value());

    var body =
        new ErrorResponse(
            Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI());

    res.getWriter().write(toJson(body));
  }

  private String toJson(ErrorResponse body) {
    return "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}"
        .formatted(
            body.timestamp().toString(),
            body.status(),
            body.error(),
            body.message(),
            body.path());
  }
}
