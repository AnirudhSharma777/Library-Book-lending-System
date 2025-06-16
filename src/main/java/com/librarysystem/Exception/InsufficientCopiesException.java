package com.librarysystem.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientCopiesException extends RuntimeException{
    public InsufficientCopiesException(String message){
        super(message);
    }
}
