package com.example.pca.service;

import com.example.pca.model.IrisDataset;
import com.example.pca.util.Matrices;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Veri setinin neleri kapsadigini ozetler (ornek sayisi, ozellikler, siniflar, istatistikler). */
public class DatasetInspector {

    public String describe(IrisDataset ds) {
        double[][] x = ds.getFeatures();
        double[] mean = Matrices.columnMeans(x);
        double[] std = Matrices.columnStd(x, mean);
        List<String> names = ds.getFeatureNames();

        StringBuilder sb = new StringBuilder();
        sb.append("=== IRIS VERI SETI OZETI ===\n");
        sb.append(String.format("Ornek sayisi  : %d%nOzellik sayisi : %d (hepsi cm cinsinden, sayisal)%n",
                ds.getSampleCount(), ds.getFeatureCount()));
        sb.append("Eksik deger   : 0 (dogrulama asamasinda NaN/Infinity kontrolu yapildi)\n\n");

        sb.append("Siniflar (hedef degisken):\n");
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String l : ds.getLabels()) counts.merge(l, 1, Integer::sum);
        counts.forEach((k, v) -> sb.append(String.format("  - %-12s %d ornek%n", k, v)));

        sb.append("\nOzellik istatistikleri:\n");
        sb.append(String.format("  %-14s %8s %8s %8s %8s%n", "ozellik", "min", "max", "ortalama", "std"));
        for (int c = 0; c < names.size(); c++) {
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
            for (double[] row : x) {
                min = Math.min(min, row[c]);
                max = Math.max(max, row[c]);
            }
            sb.append(String.format("  %-14s %8.2f %8.2f %8.3f %8.3f%n", names.get(c), min, max, mean[c], std[c]));
        }
        return sb.toString();
    }
}
