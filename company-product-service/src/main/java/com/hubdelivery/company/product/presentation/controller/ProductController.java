package com.hubdelivery.company.product.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.company.product.application.service.ProductService;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "상품 생성", description = "상품 정보를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 업체를 찾을 수 없음")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
        return ApiResponse.created(productService.createProduct(request));
    }
}
