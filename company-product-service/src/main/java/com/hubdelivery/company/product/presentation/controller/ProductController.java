package com.hubdelivery.company.product.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.product.application.service.ProductService;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
