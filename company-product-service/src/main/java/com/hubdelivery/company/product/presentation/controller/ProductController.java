package com.hubdelivery.company.product.presentation.controller;

import com.hubdelivery.common.audit.AuditorAwareImpl;
import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.company.product.application.service.ProductService;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
        return ApiResponse.created(productService.createProduct(request));
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
    @Override
    public ApiResponse<ProductResponseDto> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequestDto request
    ) {
        return ApiResponse.ok(productService.updateProduct(productId, request));
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Void> deleteProduct(
            @PathVariable UUID productId,
            @RequestHeader(value = AuditorAwareImpl.X_USER_ID, defaultValue = AuditorAwareImpl.SYSTEM_AUDITOR) String deletedBy
    ) {
        productService.deleteProduct(productId, deletedBy);
        return ApiResponse.ok(null);
    }
}
