package com.example.pca.aspect;

import com.example.pca.exception.DataValidationException;
import com.example.pca.model.Validatable;
import com.example.pca.util.Describe;
import com.example.pca.util.Matrices;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metot calismadan ONCE girdileri dogrular. Hedef kodun icinde tekrar tekrar if-kontrolu yazmak yerine
 * bu "cross-cutting concern" tek yerde toplanir.
 */
@Aspect
public class ValidationAspect {

    private static final Logger log = LoggerFactory.getLogger("VALIDATION");

    /** Genel kural: null yasak, double[][] / double[] icinde NaN-Infinity yasak, Validatable nesneler kendini dogrular. */
    @Before(Pointcuts.SERVICE_METHODS)
    public void validateArguments(JoinPoint jp) {
        String method = Describe.methodKey(jp);
        Object[] args = jp.getArgs();
        String[] names = ((CodeSignature) jp.getSignature()).getParameterNames();
        try {
            for (int i = 0; i < args.length; i++) {
                String name = names[i];
                Object arg = args[i];
                if (arg == null) {
                    throw new DataValidationException(method + ": '" + name + "' parametresi null olamaz");
                }
                if (arg instanceof double[][] m) {
                    Matrices.requireValidMatrix(method + "." + name, m);
                } else if (arg instanceof double[] v) {
                    for (double x : v) {
                        if (!Double.isFinite(x)) {
                            throw new DataValidationException(method + "." + name + ": gecersiz deger " + x);
                        }
                    }
                } else if (arg instanceof Validatable validatable) {
                    validatable.validate();
                }
            }
            MetricsCollector.getInstance().recordValidation(true);
            log.debug("Dogrulama basarili: {}", method);
        } catch (DataValidationException e) {
            MetricsCollector.getInstance().recordValidation(false);
            log.warn("Dogrulama BASARISIZ: {}", e.getMessage());
            throw e;
        }
    }

    /** PCAAnalyzer.compute'a ozel is kurali: 2 <= n, 1 <= k <= ozellik sayisi. */
    @Before(value = "execution(public * com.example.pca.service.PCAAnalyzer.compute(..)) && args(data, k)",
            argNames = "data,k")
    public void validateComponentCount(double[][] data, int k) {
        if (data == null || data.length == 0) return; // genel kural zaten yakalar
        if (data.length < 2) {
            fail("PCA icin en az 2 ornek gerekli, verilen: " + data.length);
        }
        int d = data[0].length;
        if (k < 1 || k > d) {
            fail("Bilesen sayisi 1.." + d + " araliginda olmali, verilen: " + k);
        }
    }

    private void fail(String msg) {
        MetricsCollector.getInstance().recordValidation(false);
        log.warn("Dogrulama BASARISIZ: {}", msg);
        throw new DataValidationException(msg);
    }
}
