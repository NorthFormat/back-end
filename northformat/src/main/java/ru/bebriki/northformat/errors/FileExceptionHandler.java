package ru.bebriki.northformat.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FileExceptionHandler {

    @ExceptionHandler(value = {FileNotFoundException.class})
    public ResponseEntity<Object> handleUserNotFoundException(FileNotFoundException fileNotFoundException) {

        FileException fileException = new FileException(
                fileNotFoundException.getMessage(),
                HttpStatus.NOT_FOUND
        );

        return new ResponseEntity<>(fileException, fileException.getHttpStatus());

    }

}


