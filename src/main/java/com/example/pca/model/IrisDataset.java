package com.example.pca.model;

import com.example.pca.exception.DataValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/** Iris veri setini temsil eder: 150 ornek x 4 ozellik + tur etiketi. */
public final class IrisDataset implements Validatable {

    private final List<String> featureNames;
    private final double[][] features;
    private final String[] labels;

    public IrisDataset(List<String> featureNames, double[][] features, String[] labels) {
        this.featureNames = Collections.unmodifiableList(new ArrayList<>(featureNames));
        this.features = features;
        this.labels = labels;
    }

    public List<String> getFeatureNames() { return featureNames; }
    public double[][] getFeatures()       { return features; }
    public String[] getLabels()           { return labels; }
    public int getSampleCount()           { return features.length; }
    public int getFeatureCount()          { return featureNames.size(); }

    /** Tekrarsiz sinif adlari (veri setindeki sira korunur). */
    public List<String> getClassNames() {
        return new ArrayList<>(new LinkedHashSet<>(List.of(labels)));
    }

    @Override
    public void validate() {
        if (features == null || features.length == 0) {
            throw new DataValidationException("IrisDataset: ozellik matrisi bos");
        }
        if (labels == null || labels.length != features.length) {
            throw new DataValidationException("IrisDataset: etiket sayisi ornek sayisiyla uyusmuyor");
        }
        if (features[0].length != featureNames.size()) {
            throw new DataValidationException("IrisDataset: ozellik adi sayisi sutun sayisiyla uyusmuyor");
        }
    }
}
