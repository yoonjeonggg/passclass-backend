package app_programming_development.Class.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        String userId = MDC.get("userId");
        String action = auditLog.action();
        String clazz = pjp.getSignature().getDeclaringType().getSimpleName();
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[AUDIT] action={} user={} class={} result=SUCCESS elapsed={}ms",
                    action, userId != null ? userId : "anonymous", clazz, elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[AUDIT] action={} user={} class={} result=FAIL elapsed={}ms error={}",
                    action, userId != null ? userId : "anonymous", clazz, elapsed, e.getMessage());
            throw e;
        }
    }
}
