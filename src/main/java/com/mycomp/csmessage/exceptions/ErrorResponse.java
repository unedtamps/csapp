package com.mycomp.csmessage.exceptions;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp, int status, String error, String message, String path) {}
