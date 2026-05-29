package com.hubdelivery.company.product.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.global.infrastructure.client.HubClient;
import com.hubdelivery.company.global.infrastructure.client.dto.UserResponse;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import com.hubdelivery.company.product.domain.entity.Product;
import com.hubdelivery.company.product.domain.exception.*;
import com.hubdelivery.company.product.domain.repository.ProductRepository;
import com.hubdelivery.company.product.presentation.dto.request.ProductCreateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductStockUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.request.ProductUpdateRequestDto;
import com.hubdelivery.company.product.presentation.dto.response.ProductResponseDto;
import feign.FeignException;
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
    private final HubClient hubClient;

    /** 상품 생성 로직 */
    @Transactional
    public ProductResponseDto createProduct(UUID userId, UserRole userRole, ProductCreateRequestDto request) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        // validateProductCreateAuthority(user, request.hubId(), request.companyId());
        validateHubExists(userId, userRole, request.hubId());
        validateCompanyExists(request.companyId());

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

    /** 상품 수정 로직 */
    @Transactional
    public ProductResponseDto updateProduct(UUID userId, UserRole userRole, UUID productId, ProductUpdateRequestDto request) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateProductUpdateAuthority(user, product, request.hubId(), request.companyId());
        validateHubExists(userId, userRole, request.hubId());
        validateCompanyExists(request.companyId());

        product.update(request.productName(), request.hubId(), request.companyId(), request.stockQuantity());

        return ProductResponseDto.from(product);
    }

    /** 상품 삭제 로직 */
    @Transactional
    public void deleteProduct(UUID userId, UserRole userRole, UUID productId) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateProductDeleteAuthority(user, product);

        // TODO: user-service 연동 후 userId 대신 username 으로 기록
        product.softDelete(userId.toString());
    }

    /** 상품 재고 감소 로직 */
    @Transactional
    public ProductResponseDto decreaseStock(UUID userId, UserRole userRole, UUID productId, ProductStockUpdateRequestDto request) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Product product = productRepository.findLockedByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateProductStockAuthority(user, product);
        product.decreaseStock(request.quantity());

        return ProductResponseDto.from(product);
    }

    /** 상품 재고 증가 로직 */
    @Transactional
    public ProductResponseDto increaseStock(UUID userId, UserRole userRole, UUID productId, ProductStockUpdateRequestDto request) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Product product = productRepository.findLockedByIdAndDeletedAtIsNull(productId)
                .orElseThrow(ProductNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateProductStockAuthority(user, product);
        product.increaseStock(request.quantity());

        return ProductResponseDto.from(product);
    }

    private void validateCompanyExists(UUID companyId) {
        if (companyRepository.findByIdAndDeletedAtIsNull(companyId).isEmpty()) {
            throw new ProductCompanyNotFoundException();
        }
    }

    private void validateProductCreateAuthority(UserResponse user, UUID requestHubId, UUID requestCompanyId) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER && requestHubId.equals(user.hubId())) {
            return;
        }

        if (user.role() == UserRole.COMPANY_MANAGER && requestCompanyId.equals(user.companyId())) {
            return;
        }

        throw new ProductAccessDeniedException();
    }

    private void validateProductUpdateAuthority(UserResponse user, Product product, UUID requestHubId, UUID requestCompanyId) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER
                && product.getHubId().equals(user.hubId())
                && requestHubId.equals(user.hubId())) {
            return;
        }

        if (user.role() == UserRole.COMPANY_MANAGER
                && product.getCompanyId().equals(user.companyId())
                && requestCompanyId.equals(user.companyId())) {
            return;
        }

        throw new ProductAccessDeniedException();
    }

    private void validateProductDeleteAuthority(UserResponse user, Product product) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER && product.getHubId().equals(user.hubId())) {
            return;
        }

        throw new ProductAccessDeniedException();
    }

    private void validateProductStockAuthority(UserResponse user, Product product) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER && product.getHubId().equals(user.hubId())) {
            return;
        }

        if (user.role() == UserRole.COMPANY_MANAGER && product.getCompanyId().equals(user.companyId())) {
            return;
        }

        throw new ProductAccessDeniedException();
    }

    private void validateHubExists(UUID userId, UserRole userRole, UUID hubId) {
        try {
            hubClient.getHub(userId, userRole, hubId);
        } catch (FeignException.NotFound e) {
            throw new ProductHubNotFoundException();
        } catch (FeignException e) {
            throw new ProductHubIntegrationException();
        }
    }
}
