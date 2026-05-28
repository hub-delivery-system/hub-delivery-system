package com.hubdelivery.orderservice.order.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.orderservice.order.presentation.dto.OrderCreateRequest;
import com.hubdelivery.orderservice.order.presentation.dto.OrderResponse;
import com.hubdelivery.orderservice.order.presentation.dto.OrderSearchCondition;
import com.hubdelivery.orderservice.order.presentation.dto.OrderUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order", description = "주문 관리 API")
public interface OrderControllerDocs {

    @Operation(summary = "주문 생성", description = "주문을 생성하고 배송을 자동으로 생성합니다. 모든 로그인 사용자가 생성 가능합니다.")
    ResponseEntity<ApiResponse<OrderResponse>> create(
            OrderCreateRequest request,
            @Parameter(description = "요청자 ID (Gateway 주입)") String userId,
            @Parameter(description = "요청자 역할 (Gateway 주입)") String role);

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 조회합니다. DELIVERY_MANAGER·COMPANY_MANAGER는 본인 주문만 조회됩니다.")
    ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAll(
            int page, int size,
            OrderSearchCondition cond,
            @Parameter(description = "요청자 ID (Gateway 주입)") String userId,
            @Parameter(description = "요청자 역할 (Gateway 주입)") String role);

    @Operation(summary = "주문 단건 조회", description = "주문 ID로 단건 조회합니다. DELIVERY_MANAGER·COMPANY_MANAGER는 본인 주문만 조회 가능합니다.")
    ResponseEntity<ApiResponse<OrderResponse>> getById(
            @Parameter(description = "주문 ID") UUID id,
            @Parameter(description = "요청자 ID (Gateway 주입)") String userId,
            @Parameter(description = "요청자 역할 (Gateway 주입)") String role);

    @Operation(summary = "주문 수정", description = "주문 상태 및 요청사항을 수정합니다. MASTER·HUB_MANAGER(담당 허브)만 가능합니다.")
    ResponseEntity<ApiResponse<OrderResponse>> update(
            @Parameter(description = "주문 ID") UUID id,
            OrderUpdateRequest request,
            @Parameter(description = "요청자 ID (Gateway 주입)") String userId,
            @Parameter(description = "요청자 역할 (Gateway 주입)") String role);

    @Operation(summary = "주문 삭제", description = "주문을 논리적으로 삭제합니다. MASTER·HUB_MANAGER(담당 허브)만 가능합니다.")
    ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "주문 ID") UUID id,
            @Parameter(description = "요청자 ID (Gateway 주입)") String userId,
            @Parameter(description = "요청자 역할 (Gateway 주입)") String role);
}
