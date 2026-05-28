package com.hubdelivery.deliveryservice.delivery.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.deliveryservice.delivery.application.DeliveryService;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryCreateRequest;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryResponse;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliverySearchCondition;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryResponse>> create(
            @Valid @RequestBody DeliveryCreateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        deliveryService.create(request, userId, UserRole.valueOf(role))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute DeliverySearchCondition cond,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getAll(page, size, cond, userId, UserRole.valueOf(role))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> getById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getById(id, userId, UserRole.valueOf(role))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.update(id, request, userId, UserRole.valueOf(role))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        deliveryService.delete(id, userId, UserRole.valueOf(role));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
