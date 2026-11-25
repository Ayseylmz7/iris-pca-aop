package com.example.pca.model;

import com.example.pca.exception.DataValidationException;
import com.example.pca.util.Matrices;

/** PCA sonucu. components: k x d (satirlar ana bilesenler), scores: n x k. */
public final class PCAResult implements Validatable {

    private final double[] eigenvalues;            // d adet (tum bilesenler)
    private final double[] explainedVarianceRatio; // d adet
    private final double[] cumulativeRatio;        // d adet
    private final double[][] components;           // k x d
    private final double[][] scores;               // n x k
    private final double[] means;                  // d adet

    public PCAResult(double[] eigenvalues, double[] explainedVarianceRatio, double[] cumulativeRatio,
                     double[][] components, double[][] scores, double[] means) {
        this.eigenvalues = eigenvalues;
        this.explainedVarianceRatio = explainedVarianceRatio;
        this.cumulativeRatio = cumulativeRatio;
        this.components = components;
        this.scores = scores;
        this.means = means;
    }

    public double[] getEigenvalues()            { return eigenvalues; }
    public double[] getExplainedVarianceRatio() { return explainedVarianceRatio; }
    public double[] getCumulativeRatio()        { return cumulativeRatio; }
    public double[][] getComponents()           { return components; }
    public double[][] getScores()               { return scores; }
    public double[] getMeans()                  { return means; }
    public int getComponentCount()              { return components.length; }

    @Override
    public void validate() {
        if (eigenvalues == null || explainedVarianceRatio == null || cumulativeRatio == null || means == null) {
            throw new DataValidationException("PCAResult: bos dizi iceriyor");
        }
        Matrices.requireValidMatrix("PCAResult.components", components);
        Matrices.requireValidMatrix("PCAResult.scores", scores);
        int d = eigenvalues.length;
        if (explainedVarianceRatio.length != d || cumulativeRatio.length != d
                || means.length != d || components[0].length != d) {
            throw new DataValidationException("PCAResult: boyutlar tutarsiz");
        }
        if (scores[0].length != components.length) {
            throw new DataValidationException("PCAResult: skor sutun sayisi bilesen sayisina esit degil");
        }
    }
}
