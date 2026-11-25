package com.example.pca;

import com.example.pca.aspect.MetricsCollector;
import com.example.pca.exception.PcaException;
import com.example.pca.model.IrisDataset;
import com.example.pca.model.PCAResult;
import com.example.pca.service.DataPreprocessor;
import com.example.pca.service.DatasetInspector;
import com.example.pca.service.IrisDataLoader;
import com.example.pca.service.PCAAnalyzer;
import com.example.pca.service.Visualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Iris + PCA + AOP uygulamasi.
 * Calistirma:  mvn compile exec:java            (normal akis)
 *              mvn compile exec:java -Dexec.args="--demo-errors"   (hata yonetimi gosterimi)
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String OUT = "output/";

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        boolean demoErrors = Arrays.asList(args).contains("--demo-errors");

        IrisDataLoader loader = new IrisDataLoader();
        DatasetInspector inspector = new DatasetInspector();
        DataPreprocessor preprocessor = new DataPreprocessor();
        PCAAnalyzer pca = new PCAAnalyzer();
        Visualizer visualizer = new Visualizer();

        int exitCode = 0;
        double[][] scaled = null;
        try {
            log.info("Uygulama basladi");
            IrisDataset iris = loader.loadFromResource("/iris.csv");
            System.out.println(inspector.describe(iris));

            scaled = preprocessor.standardize(iris.getFeatures());
            PCAResult result = pca.compute(scaled, 4);
            double maxErr = pca.verify(result);
            printPcaSummary(result, iris.getFeatureNames(), maxErr);

            String scatter = visualizer.saveScatterPlot(result, iris.getLabels(), OUT + "pca_scatter");
            String scree = visualizer.saveScreePlot(result, OUT + "pca_scree");
            System.out.println("Grafikler kaydedildi: " + scatter + " , " + scree);
        } catch (PcaException e) {
            log.error("Ana akis hata ile sonlandi: {}", e.getMessage());
            exitCode = 1;
        }

        if (demoErrors) {
            runErrorDemos(loader, preprocessor, pca, scaled);
        }

        MetricsCollector metrics = MetricsCollector.getInstance();
        System.out.println();
        System.out.println(metrics.buildReport());
        metrics.writeCsv(Path.of("logs", "performance-report.csv"));
        log.info("Uygulama bitti (cikis kodu {})", exitCode);
        if (exitCode != 0) System.exit(exitCode);
    }

    private static void printPcaSummary(PCAResult r, List<String> features, double maxErr) {
        StringBuilder sb = new StringBuilder("\n=== PCA SONUCLARI (standardize edilmis veri) ===\n");
        sb.append(String.format("%-6s %12s %12s %12s%n", "PC", "Ozdeger", "Varyans %", "Kumulatif %"));
        for (int i = 0; i < r.getEigenvalues().length; i++) {
            sb.append(String.format("PC%-4d %12.5f %12.2f %12.2f%n", i + 1, r.getEigenvalues()[i],
                    r.getExplainedVarianceRatio()[i] * 100, r.getCumulativeRatio()[i] * 100));
        }
        sb.append("\nYuklemeler (components, satir = bilesen):\n");
        sb.append(String.format("%-6s", "PC"));
        for (String f : features) sb.append(String.format(" %14s", f));
        sb.append('\n');
        for (int i = 0; i < r.getComponents().length; i++) {
            sb.append(String.format("PC%-4d", i + 1));
            for (double v : r.getComponents()[i]) sb.append(String.format(" %14.5f", v));
            sb.append('\n');
        }
        sb.append(String.format("%nDogrulama (dikgenlik + varyans esitligi) maks. sapma: %.2e%n", maxErr));
        System.out.println(sb);
    }

    /** Hata yonetimi aspect'inin calistigini gostermek icin bilerek hatali cagrilar yapar. */
    private static void runErrorDemos(IrisDataLoader loader, DataPreprocessor pre, PCAAnalyzer pca, double[][] ok) {
        System.out.println("\n=== HATA YONETIMI DEMOSU ===");
        demo("Olmayan dosya", () -> loader.loadFromResource("/yok.csv"));
        demo("NaN iceren veri", () -> pca.compute(new double[][]{{1, 2}, {Double.NaN, 3}, {4, 5}}, 1));
        demo("Duzensiz (jagged) matris", () -> pca.compute(new double[][]{{1, 2}, {3}}, 1));
        demo("null veri", () -> pre.standardize(null));
        demo("Sabit sutun (std=0)", () -> pre.standardize(new double[][]{{1, 2}, {1, 3}, {1, 4}}));
        if (ok != null) {
            demo("Gecersiz bilesen sayisi (k=9)", () -> pca.compute(ok, 9));
        }
    }

    private static void demo(String name, Runnable action) {
        try {
            action.run();
            System.out.println("[?] " + name + ": hata beklenirdi ama olusmadi");
        } catch (PcaException e) {
            System.out.printf("[OK] %-30s -> %s: %s%n", name, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
