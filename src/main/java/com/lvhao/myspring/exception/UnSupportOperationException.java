package com.lvhao.myspring.exception;

public class UnSupportOperationException extends RuntimeException {

    public UnSupportOperationException() {
    }

    public UnSupportOperationException(String message) {
        super(message);
    }

    public UnSupportOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
