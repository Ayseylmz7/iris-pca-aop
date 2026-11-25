package com.example.pca.service;

import com.example.pca.exception.DataValidationException;
import com.example.pca.exception.PcaProcessingException;
import com.example.pca.model.PCAResult;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PCA sonuclarini PNG olarak kaydeder (XChart). */
public class Visualizer {

    /** PC1 - PC2 dagilim grafigi (turlere gore renkli). Dosya adina .png otomatik eklenir. */
    public String saveScatterPlot(PCAResult result, String[] labels, String basePath) {
        double[][] scores = result.getScores();
        if (scores[0].length < 2) {
            throw new DataValidationException("Dagilim grafigi icin en az 2 bilesen gerekli");
        }
        if (labels.length != scores.length) {
            throw new DataValidationException("Etiket sayisi (" + labels.length
                    + ") skor sayisiyla (" + scores.length + ") uyusmuyor");
        }
        double[] ratio = result.getExplainedVarianceRatio();

        XYChart chart = new XYChartBuilder().width(900).height(650)
                .title("Iris Veri Seti - PCA (PC1 vs PC2)")
                .xAxisTitle(String.format("PC1 (%%%.1f varyans)", ratio[0] * 100))
                .yAxisTitle(String.format("PC2 (%%%.1f varyans)", ratio[1] * 100))
                .build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        chart.getStyler().setMarkerSize(9);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);

        Map<String, List<double[]>> groups = new LinkedHashMap<>();
        for (int i = 0; i < labels.length; i++) {
            groups.computeIfAbsent(labels[i], k -> new ArrayList<>())
                  .add(new double[]{scores[i][0], scores[i][1]});
        }
        groups.forEach((name, pts) -> {
            double[] x = pts.stream().mapToDouble(p -> p[0]).toArray();
            double[] y = pts.stream().mapToDouble(p -> p[1]).toArray();
            chart.addSeries(name, x, y);
        });
        return save(chart, basePath);
    }

    /** Scree plot: bilesen basina aciklanan varyans (%) ve kumulatif varyans. */
    public String saveScreePlot(PCAResult result, String basePath) {
        double[] ratio = result.getExplainedVarianceRatio();
        double[] cum = result.getCumulativeRatio();
        List<String> xs = new ArrayList<>();
        List<Double> individual = new ArrayList<>();
        List<Double> cumulative = new ArrayList<>();
        for (int i = 0; i < ratio.length; i++) {
            xs.add("PC" + (i + 1));
            individual.add(ratio[i] * 100);
            cumulative.add(cum[i] * 100);
        }
        CategoryChart chart = new CategoryChartBuilder().width(800).height(550)
                .title("Scree Plot - Aciklanan Varyans")
                .xAxisTitle("Temel Bilesen").yAxisTitle("Varyans (%)").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.addSeries("Bireysel (%)", xs, individual);
        chart.addSeries("Kumulatif (%)", xs, cumulative)
             .setChartCategorySeriesRenderStyle(CategorySeries.CategorySeriesRenderStyle.Line);
        return save(chart, basePath);
    }

    private String save(Chart<?, ?> chart, String basePath) {
        try {
            File parent = new File(basePath).getAbsoluteFile().getParentFile();
            if (parent != null) parent.mkdirs();
            BitmapEncoder.saveBitmap(chart, basePath, BitmapEncoder.BitmapFormat.PNG);
            return basePath + ".png";
        } catch (IOException e) {
            throw new PcaProcessingException("Grafik kaydedilemedi: " + basePath, e);
        }
    }
}
