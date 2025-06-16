package com.librarysystem.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SignupFailedException extends RuntimeException{
    public SignupFailedException(String message){
        super(message);
    }
}
