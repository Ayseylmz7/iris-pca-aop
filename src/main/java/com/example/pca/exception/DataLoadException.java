package com.example.pca.exception;

/** Veri seti okuma / ayristirma hatalari. */
public class DataLoadException extends PcaException {
    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
