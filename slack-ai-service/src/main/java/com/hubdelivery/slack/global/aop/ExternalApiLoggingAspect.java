package com.hubdelivery.slack.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExternalApiLoggingAspect {

    /**
     * 외부 API 호출 시간 측정
     * 성공 여부 로깅
     * 실패 원인 로깅
     */
    @Around("@annotation(externalApiLog)")
    public Object logExternalApiCall(
            ProceedingJoinPoint joinPoint,
            ExternalApiLog externalApiLog
    ) throws Throwable {

        long startTime = System.currentTimeMillis();

        String apiName = externalApiLog.value();
        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed();

            long elapsedTime =
                    System.currentTimeMillis() - startTime;

            log.info(
                    "[API SUCCESS] api={}, method={}, elapsedMs={}",
                    apiName,
                    methodName,
                    elapsedTime
            );

            return result;

        } catch (Exception e) {

            long elapsedTime =
                    System.currentTimeMillis() - startTime;

            log.error(
                    "[API FAIL] api={}, method={}, elapsedMs={}, error={}",
                    apiName,
                    methodName,
                    elapsedTime,
                    e.getMessage()
            );

            throw e;
        }
    }
}
