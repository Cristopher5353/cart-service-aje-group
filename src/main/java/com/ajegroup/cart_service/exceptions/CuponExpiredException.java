package com.ajegroup.cart_service.exceptions;

public class CuponExpiredException extends RuntimeException {
    public CuponExpiredException(String message) {
        super(message);
    }
}
