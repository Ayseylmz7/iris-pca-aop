package com.example.pca.exception;

/** PCA hesabi veya beklenmeyen islem hatalari. */
public class PcaProcessingException extends PcaException {
    public PcaProcessingException(String message) {
        super(message);
    }

    public PcaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
