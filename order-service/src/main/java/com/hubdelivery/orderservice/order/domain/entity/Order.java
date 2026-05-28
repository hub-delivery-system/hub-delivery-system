package com.hubdelivery.orderservice.order.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.orderservice.order.domain.type.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // 주문을 요청한 업체 ID (공급업체)
    @Column(nullable = false, updatable = false)
    private UUID producerId;

    // 상품을 수령할 업체 ID
    @Column(nullable = false, updatable = false)
    private UUID receiverId;

    // 주문한 상품 ID
    @Column(nullable = false, updatable = false)
    private UUID productId;

    // 상품이 속한 허브 ID — HUB_MANAGER 목록 조회 필터용 (생성 시 product-service에서 조회해 저장)
    @Column(nullable = false, updatable = false)
    private UUID hubId;

    // 주문 수량
    @Column(nullable = false)
    private Integer amount;

    // 주문 생성 후 delivery-service 연동을 통해 채워지는 배송 ID
    @Column
    private UUID deliveryId;

    // 납품 기한 등 요청사항
    @Column(length = 255)
    private String requestMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Builder
    private Order(UUID producerId, UUID receiverId, UUID productId, UUID hubId,
                  Integer amount, String requestMessage) {
        this.producerId = producerId;
        this.receiverId = receiverId;
        this.productId = productId;
        this.hubId = hubId;
        this.amount = amount;
        this.requestMessage = requestMessage;
        this.status = OrderStatus.PENDING; // 주문 생성 시 항상 PENDING으로 시작
    }

    /** 주문 상태 및 요청사항 수정 */
    public void update(OrderStatus status, String requestMessage) {
        this.status = status;
        this.requestMessage = requestMessage;
    }

    /** 배송 생성 후 deliveryId 연결 */
    public void assignDelivery(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
}
