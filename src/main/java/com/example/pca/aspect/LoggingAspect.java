package com.example.pca.aspect;

import com.example.pca.util.Describe;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Islem adimlarini loglar: metot girisi (argumanlar) ve basarili cikisi (donus degeri ozeti). */
@Aspect
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("PIPELINE");

    @Before(Pointcuts.SERVICE_METHODS)
    public void logEntry(JoinPoint jp) {
        log.info(">> {}{}", Describe.methodKey(jp), Describe.args(jp.getArgs()));
    }

    @AfterReturning(pointcut = Pointcuts.SERVICE_METHODS, returning = "result")
    public void logExit(JoinPoint jp, Object result) {
        log.info("<< {} tamamlandi -> {}", Describe.methodKey(jp), Describe.value(result));
    }
}
