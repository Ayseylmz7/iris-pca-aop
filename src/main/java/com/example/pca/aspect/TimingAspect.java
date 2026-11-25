package com.example.pca.aspect;

import com.example.pca.util.Describe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Her service metodu icin: duvar saati suresi, CPU suresi, heap degisimi ve basari durumunu olcer.
 * Veriyi MetricsCollector'a yazar. Heap farki GC nedeniyle yaklasik bir degerdir (negatif olabilir).
 */
@Aspect
public class TimingAspect {

    private static final Logger PERF = LoggerFactory.getLogger("PERFORMANCE");
    private static final double SLOW_THRESHOLD_MS = 100.0;
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    @Around(Pointcuts.SERVICE_METHODS)
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        String key = Describe.methodKey(pjp);
        long heapBefore = usedHeap();
        long cpuBefore = cpuTime();
        long start = System.nanoTime();
        boolean success = false;
        try {
            Object result = pjp.proceed();
            success = true;
            return result;
        } finally {
            long wall = System.nanoTime() - start;
            long cpu = cpuTime() - cpuBefore;
            long heapDelta = usedHeap() - heapBefore;
            MetricsCollector.getInstance().record(key, wall, cpu, heapDelta, success);

            double ms = wall / 1_000_000.0;
            PERF.info("{} | sure={} ms | cpu={} ms | heapDelta={} KB | durum={}", key,
                    String.format("%.3f", ms), String.format("%.3f", cpu / 1_000_000.0),
                    String.format("%.1f", heapDelta / 1024.0), success ? "BASARILI" : "HATA");
            if (ms > SLOW_THRESHOLD_MS) {
                PERF.warn("YAVAS CAGRI: {} {} ms (esik {} ms)", key, String.format("%.1f", ms), SLOW_THRESHOLD_MS);
            }
        }
    }

    private static long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static long cpuTime() {
        return THREADS.isCurrentThreadCpuTimeSupported() ? THREADS.getCurrentThreadCpuTime() : 0L;
    }
}
