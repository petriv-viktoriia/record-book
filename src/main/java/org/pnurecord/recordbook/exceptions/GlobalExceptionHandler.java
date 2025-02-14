package org.pnurecord.recordbook.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto handleInvalidArgument(MethodArgumentNotValidException ex) {
        String messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return new ResponseDto(
                HttpStatus.BAD_REQUEST.name(),
                HttpStatus.BAD_REQUEST.value(),
                messages
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseDto handleNotFoundException(NotFoundException ex) {
        return new ResponseDto(
                HttpStatus.NOT_FOUND.name(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateValueException.class)
    public ResponseDto handleDuplicateValueException(DuplicateValueException ex) {
        return new ResponseDto(
                HttpStatus.CONFLICT.name(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseDto handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return new ResponseDto(
                HttpStatus.PAYLOAD_TOO_LARGE.name(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File size exceeds the maximum allowed limit!"
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseDto handleUserNotAuthenticatedException(UserNotAuthenticatedException ex) {
        return new ResponseDto(
                HttpStatus.FORBIDDEN.name(),
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
    }
}
