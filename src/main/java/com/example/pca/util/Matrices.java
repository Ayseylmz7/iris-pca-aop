package com.example.pca.util;

import com.example.pca.exception.DataValidationException;

/** Kucuk matris yardimcilari. */
public final class Matrices {

    private Matrices() { }

    /** null, bos, duzensiz (jagged), NaN veya Infinity iceren matrisleri reddeder. */
    public static void requireValidMatrix(String name, double[][] m) {
        if (m == null) {
            throw new DataValidationException(name + " null olamaz");
        }
        if (m.length == 0) {
            throw new DataValidationException(name + " bos (0 satir)");
        }
        if (m[0] == null || m[0].length == 0) {
            throw new DataValidationException(name + " bos sutunlara sahip");
        }
        int cols = m[0].length;
        for (int r = 0; r < m.length; r++) {
            if (m[r] == null) {
                throw new DataValidationException(name + ": satir " + r + " null");
            }
            if (m[r].length != cols) {
                throw new DataValidationException(String.format(
                        "%s: satir %d uzunlugu %d, beklenen %d (duzensiz matris)", name, r, m[r].length, cols));
            }
            for (int c = 0; c < cols; c++) {
                if (!Double.isFinite(m[r][c])) {
                    throw new DataValidationException(String.format(
                            "%s: gecersiz deger (%s) satir=%d sutun=%d", name, m[r][c], r, c));
                }
            }
        }
    }

    public static double[] columnMeans(double[][] m) {
        int n = m.length, d = m[0].length;
        double[] mean = new double[d];
        for (double[] row : m) {
            for (int c = 0; c < d; c++) mean[c] += row[c];
        }
        for (int c = 0; c < d; c++) mean[c] /= n;
        return mean;
    }

    /** Orneklem standart sapmasi (n-1). */
    public static double[] columnStd(double[][] m, double[] means) {
        int n = m.length, d = m[0].length;
        double[] std = new double[d];
        for (double[] row : m) {
            for (int c = 0; c < d; c++) {
                double diff = row[c] - means[c];
                std[c] += diff * diff;
            }
        }
        for (int c = 0; c < d; c++) std[c] = Math.sqrt(std[c] / (n - 1));
        return std;
    }
}
