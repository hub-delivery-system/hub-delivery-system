package com.hubdelivery.deliveryservice.deliveryroute.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.deliveryservice.deliveryroute.application.DeliveryRouteService;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteResponse;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteSearchCondition;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    // 배송별 경로 목록 조회
    @GetMapping("/api/v1/deliveries/{deliveryId}/routes")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryRouteResponse>>> getByDeliveryId(
            @PathVariable UUID deliveryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute DeliveryRouteSearchCondition cond,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryRouteService.getByDeliveryId(deliveryId, page, size, cond, userId, UserRole.valueOf(role))));
    }

    // 경로 단건 조회
    @GetMapping("/api/v1/delivery-routes/{id}")
    public ResponseEntity<ApiResponse<DeliveryRouteResponse>> getById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryRouteService.getById(id, userId, UserRole.valueOf(role))));
    }

    // 경로 수정
    @PutMapping("/api/v1/delivery-routes/{id}")
    public ResponseEntity<ApiResponse<DeliveryRouteResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryRouteUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryRouteService.update(id, request, userId, UserRole.valueOf(role))));
    }

    // 경로 삭제
    @DeleteMapping("/api/v1/delivery-routes/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        deliveryRouteService.delete(id, userId, UserRole.valueOf(role));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
