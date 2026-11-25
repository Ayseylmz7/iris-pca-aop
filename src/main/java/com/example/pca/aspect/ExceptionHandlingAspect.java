package com.example.pca.aspect;

import com.example.pca.exception.PcaException;
import com.example.pca.exception.PcaProcessingException;
import com.example.pca.util.Describe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merkezi hata yonetimi:
 *  - Bilinen (PcaException) hatalar: baglam bilgisiyle bir kez loglanir, aynen yeniden firlatilir.
 *  - Beklenmeyen hatalar: stack trace ile loglanir, PcaProcessingException icine sarilir.
 */
@Aspect
public class ExceptionHandlingAspect {

    private static final Logger log = LoggerFactory.getLogger("ERROR_HANDLER");

    @Around(Pointcuts.SERVICE_METHODS)
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (PcaException e) {
            if (!e.isLogged()) {
                log.error("HATA [{}] {} | metot={}{} | mesaj={}", e.getClass().getSimpleName(), "yakalandi",
                        Describe.methodKey(pjp), Describe.args(pjp.getArgs()), e.getMessage());
                e.markLogged();
            }
            throw e;
        } catch (Throwable t) {
            String method = Describe.methodKey(pjp);
            log.error("BEKLENMEYEN HATA | metot={}{}", method, Describe.args(pjp.getArgs()), t);
            PcaProcessingException wrapped =
                    new PcaProcessingException(method + " calisirken beklenmeyen hata: " + t, t);
            wrapped.markLogged();
            throw wrapped;
        }
    }
}
