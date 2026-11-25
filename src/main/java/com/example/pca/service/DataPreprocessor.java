package com.example.pca.service;

import com.example.pca.exception.DataValidationException;
import com.example.pca.util.Matrices;

/** On isleme: z-skor standardizasyonu (ortalama 0, standart sapma 1). */
public class DataPreprocessor {

    public double[][] standardize(double[][] data) {
        int n = data.length, d = data[0].length;
        if (n < 2) {
            throw new DataValidationException("Standardizasyon icin en az 2 ornek gerekli");
        }
        double[] mean = Matrices.columnMeans(data);
        double[] std = Matrices.columnStd(data, mean);
        for (int c = 0; c < d; c++) {
            if (std[c] == 0.0) {
                throw new DataValidationException("Sutun " + c + " sabit (std=0), standardizasyon yapilamaz");
            }
        }
        double[][] out = new double[n][d];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < d; c++) {
                out[r][c] = (data[r][c] - mean[c]) / std[c];
            }
        }
        return out;
    }
}
