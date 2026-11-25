package com.example.pca.model;

/** Kendi gecerliligini kontrol edebilen nesneler (ValidationAspect bunu cagirir). */
public interface Validatable {
    /** Gecersizse DataValidationException firlatir. */
    void validate();
}
