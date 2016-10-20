package com.espirit.moddev.core;

/**
 * The Class LayerMappingException.
 */
public class LayerMappingException extends RuntimeException {

    private static final long serialVersionUID = 8164220948231148970L;

    /**
     * Instantiates a new layer mapping exception.
     */
    public LayerMappingException() {
        super();
    }

    /**
     * Instantiates a new layer mapping exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public LayerMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new layer mapping exception.
     *
     * @param message the message
     */
    public LayerMappingException(String message) {
        super(message);
    }

    /**
     * Instantiates a new layer mapping exception.
     *
     * @param cause the cause
     */
    public LayerMappingException(Throwable cause) {
        super(cause);
    }

}
