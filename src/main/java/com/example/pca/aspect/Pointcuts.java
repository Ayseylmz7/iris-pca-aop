package com.example.pca.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Ortak pointcut tanimi + aspect siralamasi.
 *
 * Siralama (soldan saga = dis katmandan ic katmana):
 *   ExceptionHandling -> Timing -> Logging -> Validation -> [hedef metot]
 * Boylece hata yonetimi her seyi sarar; zamanlama, loglama ve dogrulamayi da kapsar
 * (dogrulama hatalari da metrik olarak "hatali cagri" sayilir).
 */
@Aspect
@DeclarePrecedence("com.example.pca.aspect.ExceptionHandlingAspect, "
        + "com.example.pca.aspect.TimingAspect, "
        + "com.example.pca.aspect.LoggingAspect, "
        + "com.example.pca.aspect.ValidationAspect")
public class Pointcuts {

    public static final String SERVICE_METHODS = "com.example.pca.aspect.Pointcuts.serviceMethods()";

    /** service paketindeki tum public metotlar (ic cagrilar dahil: compile-time weaving). */
    @Pointcut("execution(public * com.example.pca.service..*.*(..))")
    public void serviceMethods() { }
}
