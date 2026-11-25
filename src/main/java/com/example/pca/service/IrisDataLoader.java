package com.example.pca.service;

import com.example.pca.exception.DataLoadException;
import com.example.pca.model.IrisDataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Iris CSV dosyasini classpath'ten okur. */
public class IrisDataLoader {

    public IrisDataset loadFromResource(String resourcePath) {
        try (InputStream in = IrisDataLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new DataLoadException("Kaynak dosya bulunamadi: " + resourcePath);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String header = br.readLine();
            if (header == null) {
                throw new DataLoadException("Dosya bos: " + resourcePath);
            }
            String[] cols = header.trim().split(",");
            if (cols.length < 2) {
                throw new DataLoadException("Baslik satiri gecersiz: " + header);
            }
            int d = cols.length - 1;
            List<double[]> rows = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            String line;
            int lineNo = 1;
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length != cols.length) {
                    throw new DataLoadException("Satir " + lineNo + ": beklenen " + cols.length
                            + " alan, bulunan " + p.length);
                }
                double[] row = new double[d];
                try {
                    for (int i = 0; i < d; i++) row[i] = Double.parseDouble(p[i].trim());
                } catch (NumberFormatException e) {
                    throw new DataLoadException("Satir " + lineNo + ": sayisal olmayan deger", e);
                }
                rows.add(row);
                labels.add(p[d].trim());
            }
            return new IrisDataset(Arrays.asList(cols).subList(0, d),
                    rows.toArray(new double[0][]), labels.toArray(new String[0]));
        } catch (IOException e) {
            throw new DataLoadException("Dosya okunamadi: " + resourcePath, e);
        }
    }
}
