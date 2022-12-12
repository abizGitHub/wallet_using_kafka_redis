package com.abiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppAdviceController {


    @ExceptionHandler
    public ResponseEntity handleNotFoundException(EntityNotFoundException exception) {
        return new ResponseEntity(exception.getMessage() + " NotFound!", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity handleDataFormatException(DataFormatException exception) {
        return new ResponseEntity("DataFormatException", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity handleNotValidException(MethodArgumentNotValidException exception) {
        return new ResponseEntity(String.format("%s %s",
                exception.getFieldError().getField(),
                exception.getFieldError().getDefaultMessage()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity handleOtherException(Exception exception) {
        return new ResponseEntity(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
