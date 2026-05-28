package com.hubdelivery.orderservice.order.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.orderservice.order.application.OrderService;
import com.hubdelivery.orderservice.order.presentation.dto.OrderCreateRequest;
import com.hubdelivery.orderservice.order.presentation.dto.OrderResponse;
import com.hubdelivery.orderservice.order.presentation.dto.OrderSearchCondition;
import com.hubdelivery.orderservice.order.presentation.dto.OrderUpdateRequest;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody OrderCreateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        orderService.create(request, userId, UserRole.valueOf(role))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @ModelAttribute OrderSearchCondition cond,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getAll(page, size, cond, userId, UserRole.valueOf(role))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getById(id, userId, UserRole.valueOf(role))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrderUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.update(id, request, userId, UserRole.valueOf(role))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role) {
        orderService.delete(id, userId, UserRole.valueOf(role));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
