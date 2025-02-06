package org.pnurecord.recordbook.exceptions;

public record ResponseDto(
        String error,
        int status,
        String message
) { }

