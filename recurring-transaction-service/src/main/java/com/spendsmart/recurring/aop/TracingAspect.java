package com.spendsmart.recurring.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TracingAspect {

    private static final Logger log = LoggerFactory.getLogger(TracingAspect.class);

    @Pointcut("within(com.spendsmart.recurring.controller..*) || within(com.spendsmart.recurring.service..*) || within(com.spendsmart.recurring.repository..*)")
    public void tracedComponents() {
        // Pointcut definition only.
    }

    @Around("tracedComponents()")
    public Object traceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long started = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString();

        if (log.isDebugEnabled()) {
            log.debug("--> {} args={} ", signature, Arrays.toString(sanitizeArgs(joinPoint.getArgs())));
        }

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - started;
            log.info("<-- {} completed in {} ms", signature, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - started;
            log.error("xx> {} failed in {} ms: {}", signature, elapsed, ex.getMessage(), ex);
            throw ex;
        }
    }


    private Object[] sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }

        return Arrays.stream(args)
                .map(this::sanitizeArg)
                .toArray();
    }

    private Object sanitizeArg(Object arg) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean || arg.getClass().isEnum()) {
            return arg;
        }

        Class<?> type = arg.getClass();
        Object[] fields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> isSensitiveField(field.getName()))
                .map(field -> maskFieldValue(arg, field))
                .toArray();

        if (fields.length == 0) {
            return arg;
        }

        return type.getSimpleName() + "[masked=" + Arrays.toString(fields) + "]";
    }

    private String maskFieldValue(Object target, java.lang.reflect.Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(target);
            if (value == null) {
                return field.getName() + "=null";
            }
            return field.getName() + "=***";
        } catch (Exception ignored) {
            return field.getName() + "=<unreadable>";
        }
    }

    private boolean isSensitiveField(String fieldName) {
        String normalized = fieldName.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("key")
                || normalized.contains("credential")
                || normalized.contains("otp");
    }
    @AfterThrowing(pointcut = "tracedComponents()", throwing = "ex")
    public void traceException(JoinPoint joinPoint, Throwable ex) {
        log.error("!! {} threw {}", joinPoint.getSignature().toShortString(), ex.getClass().getSimpleName());
    }
}
