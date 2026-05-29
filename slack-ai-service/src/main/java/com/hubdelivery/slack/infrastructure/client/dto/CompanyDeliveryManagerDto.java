package com.hubdelivery.slack.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDeliveryManagerDto {

    private UUID id;
    private UUID hubId;
    private String slackId;
    private String type;
    private Integer deliverySeq;
    private List<DeliveryDestinationDto> destinations;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryDestinationDto {
        private UUID deliveryId;
        private String address;
        private Double latitude;
        private Double longitude;
        private String receiverName;
    }
}
