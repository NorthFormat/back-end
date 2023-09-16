package ru.bebriki.northformat.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class FileException {

    private final String message;

    private final HttpStatus httpStatus;

}
