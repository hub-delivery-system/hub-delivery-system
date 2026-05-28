package com.hubdelivery.deliveryservice.deliverymanager.presentation.dto;

import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryManagerSearchCondition {

    /** 담당자 타입 필터 (선택) */
    private DeliveryManagerType type;

    /** 허브 ID 필터 (선택) */
    private UUID hubId;

    /** 유저 ID 필터 (선택) */
    private UUID userId;
}
