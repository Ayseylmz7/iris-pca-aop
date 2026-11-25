package com.example.pca.model;

/**
 * Ozdeger ayristirma sonucu. Ozdegerler buyukten kucuge siralidir;
 * vectors[i], values[i]'ye ait birim uzunluktaki ozvektordur (satir olarak).
 */
public record EigenResult(double[] values, double[][] vectors) { }
