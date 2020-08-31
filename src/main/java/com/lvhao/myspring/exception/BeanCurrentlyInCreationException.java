package com.lvhao.myspring.exception;

public class BeanCurrentlyInCreationException extends RuntimeException{

    public BeanCurrentlyInCreationException() {
    }

    public BeanCurrentlyInCreationException(String message) {
        super(message);
    }

    public BeanCurrentlyInCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
