package com.hubdelivery.deliveryservice.deliverymanager.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.deliveryservice.deliverymanager.application.DeliveryManagerService;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerCreateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerResponse;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> create(
            @Valid @RequestBody DeliveryManagerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(deliveryManagerService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryManagerResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryManagerService.getAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryManagerService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryManagerUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryManagerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId) {
        deliveryManagerService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // 순환 배정: 현재 순번 기준으로 다음 담당자 반환
    @GetMapping("/assign-next")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> assignNext(
            @RequestParam UUID hubId,
            @RequestParam DeliveryManagerType type,
            @RequestParam(defaultValue = "0") int currentSequence) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryManagerService.assignNext(hubId, type, currentSequence)));
    }
}
