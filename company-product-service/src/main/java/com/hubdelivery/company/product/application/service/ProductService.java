package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.ProductCompanyNotFoundException;
import com.hubdelivery.company.product.domain.exception.ProductNotFoundException;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.hubdelivery.company.global.util.SearchSortPolicy.PRODUCT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;

    /** 상품 생성 로직 */
    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto request) {
        // TODO: HubClient 확정 후 hubId 존재 여부와 HUB_MANAGER 담당 허브 여부를 검증한다.
        validateCompanyExists(request);

        Product product = productRepository.save(request.toEntity());
        return ProductResponseDto.from(product);
    }

    /** 상품 전체 조회 로직 */
    public PageResponse<ProductResponseDto> getAllProducts(String keyword, Integer page, Integer size, String sort) {
        String normalizedKeyword = SearchPageableUtils.normalizeKeyword(keyword);
        Pageable pageable = SearchPageableUtils.createPageable(page, size, sort, PRODUCT);

        return PageResponse.from(productRepository.searchProducts(normalizedKeyword, pageable)
                .map(ProductResponseDto::from));
    }

    /** 상품 상세 조회 로직 */
    public ProductResponseDto getProduct(UUID productId) {
        return ProductResponseDto.from(productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new));
    }

    private void validateCompanyExists(ProductCreateRequestDto request) {
        if (companyRepository.findByIdAndDeletedAtIsNull(request.companyId()).isEmpty()) {
            throw new ProductCompanyNotFoundException();
        }
    }
}
