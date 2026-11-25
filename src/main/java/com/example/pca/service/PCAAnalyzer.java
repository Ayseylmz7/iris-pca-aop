package com.example.pca.service;

import com.example.pca.exception.DataValidationException;
import com.example.pca.exception.PcaProcessingException;
import com.example.pca.model.EigenResult;
import com.example.pca.model.PCAResult;
import com.example.pca.util.Matrices;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Temel Bilesenler Analizi (kovaryans matrisi + ozdeger ayristirma yontemi).
 *
 * Adimlar: 1) merkezleme  2) kovaryans  3) ozdeger ayristirma  4) siralama  5) projeksiyon.
 * Her adim ayri public metottur; boylece AOP her adimi ayri ayri olcer/loglar.
 */
public class PCAAnalyzer {

    private static final double TOLERANCE = 1e-8;

    /** Tum adimlari sirayla calistirir. */
    public PCAResult compute(double[][] data, int numComponents) {
        double[] means = Matrices.columnMeans(data);
        double[][] centered = centerData(data);
        double[][] cov = computeCovariance(centered);
        EigenResult eig = decompose(cov);

        double[] values = eig.values();
        double total = Arrays.stream(values).sum();
        if (total <= 0) {
            throw new DataValidationException("Toplam varyans 0: veri sabit olabilir");
        }
        double[] ratio = new double[values.length];
        double[] cumulative = new double[values.length];
        double running = 0;
        for (int i = 0; i < values.length; i++) {
            ratio[i] = values[i] / total;
            running += ratio[i];
            cumulative[i] = running;
        }

        double[][] components = Arrays.copyOf(eig.vectors(), numComponents);
        double[][] scores = project(centered, components);
        return new PCAResult(values, ratio, cumulative, components, scores, means);
    }

    /** 1) Her sutundan ortalamayi cikarir. */
    public double[][] centerData(double[][] data) {
        double[] mean = Matrices.columnMeans(data);
        double[][] out = new double[data.length][data[0].length];
        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < mean.length; c++) {
                out[r][c] = data[r][c] - mean[c];
            }
        }
        return out;
    }

    /** 2) Orneklem kovaryans matrisi: C = XtX / (n-1). */
    public double[][] computeCovariance(double[][] centered) {
        int n = centered.length, d = centered[0].length;
        if (n < 2) {
            throw new DataValidationException("Kovaryans icin en az 2 ornek gerekli");
        }
        double[][] cov = new double[d][d];
        for (int i = 0; i < d; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0;
                for (double[] row : centered) sum += row[i] * row[j];
                cov[i][j] = cov[j][i] = sum / (n - 1);
            }
        }
        return cov;
    }

    /** 3-4) Ozdeger ayristirma (Commons Math) ve buyukten kucuge siralama. */
    public EigenResult decompose(double[][] cov) {
        EigenDecomposition ed = new EigenDecomposition(new Array2DRowRealMatrix(cov, true));
        if (ed.hasComplexEigenvalues()) {
            throw new PcaProcessingException("Kovaryans matrisi karmasik ozdegerler uretti (simetrik olmali)");
        }
        double[] raw = ed.getRealEigenvalues();
        Integer[] idx = new Integer[raw.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingDouble((Integer i) -> raw[i]).reversed());

        double[] values = new double[raw.length];
        double[][] vectors = new double[raw.length][];
        for (int k = 0; k < idx.length; k++) {
            values[k] = Math.max(0.0, raw[idx[k]]); // sayisal gurultu: -1e-17 -> 0
            vectors[k] = normalizeSign(ed.getEigenvector(idx[k]).toArray());
        }
        return new EigenResult(values, vectors);
    }

    /** 5) Merkezlenmis veriyi bilesenlere izdusurur: scores = X * V^t. */
    public double[][] project(double[][] centered, double[][] components) {
        int n = centered.length, k = components.length, d = centered[0].length;
        if (components[0].length != d) {
            throw new DataValidationException("Bilesen boyutu (" + components[0].length
                    + ") veri sutun sayisiyla (" + d + ") uyusmuyor");
        }
        double[][] scores = new double[n][k];
        for (int r = 0; r < n; r++) {
            for (int j = 0; j < k; j++) {
                double s = 0;
                for (int c = 0; c < d; c++) s += centered[r][c] * components[j][c];
                scores[r][j] = s;
            }
        }
        return scores;
    }

    /**
     * Sonucun matematiksel dogrulugunu kontrol eder:
     *  - bilesenler birbirine dik ve birim uzunlukta mi (V*Vt = I)
     *  - her bilesenin skor varyansi ozdegerine esit mi
     * Maksimum sapmayi dondurur; tolerans asilirsa hata firlatir.
     */
    public double verify(PCAResult result) {
        double[][] v = result.getComponents();
        double[][] s = result.getScores();
        int k = v.length, d = v[0].length, n = s.length;
        double maxErr = 0;

        for (int i = 0; i < k; i++) {
            for (int j = 0; j <= i; j++) {
                double dot = 0;
                for (int c = 0; c < d; c++) dot += v[i][c] * v[j][c];
                maxErr = Math.max(maxErr, Math.abs(dot - (i == j ? 1.0 : 0.0)));
            }
        }
        for (int j = 0; j < k; j++) {
            double var = 0;
            for (double[] row : s) var += row[j] * row[j];
            var /= (n - 1);
            double ev = result.getEigenvalues()[j];
            maxErr = Math.max(maxErr, Math.abs(var - ev) / Math.max(1.0, ev));
        }
        if (maxErr > TOLERANCE) {
            throw new PcaProcessingException("PCA dogrulamasi basarisiz, maksimum sapma = " + maxErr);
        }
        return maxErr;
    }

    /** Isaret belirsizligini gidermek icin en buyuk mutlak degerli eleman pozitif olacak sekilde cevirir. */
    private static double[] normalizeSign(double[] v) {
        int maxIdx = 0;
        for (int i = 1; i < v.length; i++) {
            if (Math.abs(v[i]) > Math.abs(v[maxIdx])) maxIdx = i;
        }
        if (v[maxIdx] < 0) {
            for (int i = 0; i < v.length; i++) v[i] = -v[i];
        }
        return v;
    }
}
