package com.example.pca.exception;

/** Veri dogrulama (null, NaN, bos matris, gecersiz parametre...) hatalari. */
public class DataValidationException extends PcaException {
    public DataValidationException(String message) {
        super(message);
    }
}
