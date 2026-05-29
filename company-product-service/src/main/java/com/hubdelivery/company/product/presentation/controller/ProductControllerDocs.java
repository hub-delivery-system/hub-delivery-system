package com.hubdelivery.company.product.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductStockUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Product", description = "상품 API")
public interface ProductControllerDocs {

    @Operation(summary = "상품 생성", description = "상품 정보를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 업체를 찾을 수 없음")
    })
    ApiResponse<ProductResponseDto> createProduct(
            UUID userId,
            UserRole userRole,
            ProductCreateRequestDto request
    );

    @Operation(summary = "상품 조회 및 검색", description = "상품 목록을 검색 조건과 페이지 조건으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<PageResponse<ProductResponseDto>> getAllProducts(
            @Parameter(description = "상품명 검색어")
            String keyword,
            @Parameter(description = "페이지 번호")
            Integer page,
            @Parameter(description = "페이지 크기")
            Integer size,
            @Parameter(description = "정렬 조건")
            String sort
    );

    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ApiResponse<ProductResponseDto> getProduct(
            @Parameter(description = "상품 ID")
            UUID productId
    );

    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 또는 상품 업체를 찾을 수 없음")
    })
    ApiResponse<ProductResponseDto> updateProduct(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "상품 ID")
            UUID productId,
            ProductUpdateRequestDto request
    );

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ApiResponse<Void> deleteProduct(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "상품 ID")
            UUID productId
    );

    @Operation(summary = "상품 재고 감소", description = "상품 재고를 요청 수량만큼 감소합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 감소 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "상품 재고 부족")
    })
    ApiResponse<ProductResponseDto> decreaseStock(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "상품 ID")
            UUID productId,
            ProductStockUpdateRequestDto request
    );

    @Operation(summary = "상품 재고 증가", description = "상품 재고를 요청 수량만큼 증가합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재고 증가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ApiResponse<ProductResponseDto> increaseStock(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "상품 ID")
            UUID productId,
            ProductStockUpdateRequestDto request
    );
}
