package com.booking.bookingsystem.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " not found with id " + identifier);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " not found with " + field + " " + value);
    }
}
