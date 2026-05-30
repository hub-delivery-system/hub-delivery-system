package com.hubdelivery.slack.global.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExternalApiLog {

    /**
     * 외부 API 이름
     * 예시:
     * GEMINI
     * NAVER_DIRECTIONS
     * SLACK
     */
    String value();
}
