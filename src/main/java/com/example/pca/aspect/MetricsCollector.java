package com.example.pca.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Aspect'lerin topladigi performans metriklerinin merkezi deposu (thread-safe singleton). */
public final class MetricsCollector {

    private static final Logger PERF = LoggerFactory.getLogger("PERFORMANCE");
    private static final MetricsCollector INSTANCE = new MetricsCollector();

    private final Map<String, MethodStats> stats = new ConcurrentHashMap<>();
    private final AtomicInteger validationsPassed = new AtomicInteger();
    private final AtomicInteger validationsFailed = new AtomicInteger();

    private MetricsCollector() { }

    public static MetricsCollector getInstance() { return INSTANCE; }

    /** Tek bir metot calismasini kaydeder. */
    public void record(String method, long wallNanos, long cpuNanos, long heapDeltaBytes, boolean success) {
        stats.computeIfAbsent(method, k -> new MethodStats()).add(wallNanos, cpuNanos, heapDeltaBytes, success);
    }

    public void recordValidation(boolean passed) {
        (passed ? validationsPassed : validationsFailed).incrementAndGet();
    }

    public int getValidationsPassed() { return validationsPassed.get(); }
    public int getValidationsFailed() { return validationsFailed.get(); }

    /** Okunabilir ozet tablo. Sure degerleri kapsayicidir (cagirdigi alt metotlar dahil). */
    public String buildReport() {
        List<Map.Entry<String, MethodStats>> rows = new ArrayList<>(stats.entrySet());
        rows.sort(Comparator.comparingLong((Map.Entry<String, MethodStats> e) -> e.getValue().totalWall).reversed());

        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFORMANS RAPORU (sureler ms; kapsayici sure) ===\n");
        sb.append(String.format("%-28s %6s %6s %10s %10s %10s %10s %10s %12s%n",
                "Metot", "Cagri", "Hata", "Toplam", "Ort", "Min", "Maks", "CPU(top)", "Heap d(KB)"));
        sb.append("-".repeat(112)).append('\n');
        long totalCalls = 0, totalErrors = 0;
        for (Map.Entry<String, MethodStats> e : rows) {
            MethodStats s = e.getValue();
            synchronized (s) {
                sb.append(String.format("%-28s %6d %6d %10.3f %10.3f %10.3f %10.3f %10.3f %12.1f%n",
                        e.getKey(), s.calls, s.errors, ms(s.totalWall), ms(s.totalWall) / s.calls,
                        ms(s.minWall), ms(s.maxWall), ms(s.totalCpu), s.totalHeap / 1024.0));
                totalCalls += s.calls;
                totalErrors += s.errors;
            }
        }
        sb.append("-".repeat(112)).append('\n');
        sb.append(String.format("Toplam metot cagrisi: %d | Hatali: %d | Dogrulama: %d basarili, %d basarisiz%n",
                totalCalls, totalErrors, validationsPassed.get(), validationsFailed.get()));
        return sb.toString();
    }

    /** Ayni verileri CSV olarak yazar (Excel'de grafik/tablo icin). */
    public void writeCsv(Path path) {
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(path))) {
                w.println("method,calls,errors,total_ms,avg_ms,min_ms,max_ms,cpu_total_ms,heap_delta_kb");
                stats.forEach((name, s) -> {
                    synchronized (s) {
                        w.printf(java.util.Locale.ROOT, "%s,%d,%d,%.4f,%.4f,%.4f,%.4f,%.4f,%.2f%n",
                                name, s.calls, s.errors, ms(s.totalWall), ms(s.totalWall) / s.calls,
                                ms(s.minWall), ms(s.maxWall), ms(s.totalCpu), s.totalHeap / 1024.0);
                    }
                });
            }
            PERF.info("Performans CSV yazildi: {}", path.toAbsolutePath());
        } catch (IOException e) {
            PERF.error("Performans CSV yazilamadi: {}", path, e);
        }
    }

    private static double ms(long nanos) { return nanos / 1_000_000.0; }

    private static final class MethodStats {
        long calls, errors, totalWall, totalCpu, totalHeap;
        long minWall = Long.MAX_VALUE, maxWall = Long.MIN_VALUE;

        synchronized void add(long wall, long cpu, long heap, boolean ok) {
            calls++;
            if (!ok) errors++;
            totalWall += wall;
            totalCpu += cpu;
            totalHeap += heap;
            minWall = Math.min(minWall, wall);
            maxWall = Math.max(maxWall, wall);
        }
    }
}
