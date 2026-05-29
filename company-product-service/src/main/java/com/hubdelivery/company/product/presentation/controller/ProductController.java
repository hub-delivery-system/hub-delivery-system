package com.hubdelivery.company.product.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.product.application.service.ProductService;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductStockUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController implements ProductControllerDocs {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_ROLE = "X-Role";

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<ProductResponseDto> createProduct(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @Valid @RequestBody ProductCreateRequestDto request
    ) {
        return ApiResponse.created(productService.createProduct(userId, userRole, request));
    }

    @GetMapping
    @Override
    public ApiResponse<PageResponse<ProductResponseDto>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(productService.getAllProducts(keyword, page, size, sort));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductResponseDto> getProduct(
            @PathVariable UUID productId
    ) {
        return ApiResponse.ok(productService.getProduct(productId));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @Override
    public ApiResponse<ProductResponseDto> updateProduct(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequestDto request
    ) {
        return ApiResponse.ok(productService.updateProduct(userId, userRole, productId, request));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @Override
    public ApiResponse<Void> deleteProduct(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID productId
    ) {
        productService.deleteProduct(userId, userRole, productId);
        return ApiResponse.ok(null);
    }

    /** 상품 재고 감소 API */
    @PatchMapping("/{productId}/stock/decrease")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @Override
    public ApiResponse<ProductResponseDto> decreaseStock(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStockUpdateRequestDto request
    ) {
        return ApiResponse.ok(productService.decreaseStock(userId, userRole, productId, request));
    }

    /** 상품 재고 증가 API */
    @PatchMapping("/{productId}/stock/increase")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @Override
    public ApiResponse<ProductResponseDto> increaseStock(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStockUpdateRequestDto request
    ) {
        return ApiResponse.ok(productService.increaseStock(userId, userRole, productId, request));
    }
}
