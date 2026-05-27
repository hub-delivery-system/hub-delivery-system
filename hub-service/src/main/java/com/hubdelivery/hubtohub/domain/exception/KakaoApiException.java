package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class KakaoApiException extends BaseException {
    public KakaoApiException() {
        super(HubToHubErrorCode.KAKAO_API_FAILED);
    }
}
