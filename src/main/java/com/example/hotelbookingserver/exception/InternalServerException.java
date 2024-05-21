package com.example.hotelbookingserver.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException (String message) {
        super(message);
    }
}
