package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class KakaoRouteNotFoundException extends BaseException {
    public KakaoRouteNotFoundException() {
        super(HubToHubErrorCode.KAKAO_ROUTE_NOT_FOUND);
    }
}
