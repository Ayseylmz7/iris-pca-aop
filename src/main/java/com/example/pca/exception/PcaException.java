package com.example.pca.exception;

/** Uygulamanin tum ozel hatalarinin temel sinifi. */
public class PcaException extends RuntimeException {

    /** Ayni hatanin katman katman tekrar loglanmasini onlemek icin kullanilir. */
    private boolean logged;

    public PcaException(String message) {
        super(message);
    }

    public PcaException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean isLogged() {
        return logged;
    }

    public void markLogged() {
        this.logged = true;
    }
}
