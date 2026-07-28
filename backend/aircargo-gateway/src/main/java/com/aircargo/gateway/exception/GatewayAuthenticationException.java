package com.aircargo.gateway.exception;

public class GatewayAuthenticationException extends RuntimeException {
    public GatewayAuthenticationException(String message) {
        super(message);
    }
}
