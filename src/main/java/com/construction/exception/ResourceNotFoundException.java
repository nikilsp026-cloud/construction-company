package com.construction.exception;

/**
 * Thrown when a requested entity (project, gallery item, blog post, etc.)
 * does not exist. Handled centrally by {@link GlobalExceptionHandler} and
 * translated into a friendly 404 response instead of a stack trace.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, Object id) {
        super(entityName + " with id " + id + " was not found.");
    }
}
