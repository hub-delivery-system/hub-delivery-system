package com.hubdelivery.company.product.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.company.product.domain.exception.ProductStockNotEnoughException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "stock_quantity", nullable = false, columnDefinition = "integer default 0")
    private int stockQuantity;

    @Builder
    public Product(String productName, UUID hubId, UUID companyId, int stockQuantity) {
        this.productName = productName;
        this.hubId = hubId;
        this.companyId = companyId;
        this.stockQuantity = stockQuantity;
    }

    /** 상품 수정 메서드 */
    public void update(String productName, UUID hubId, UUID companyId, int stockQuantity) {
        this.productName = productName;
        this.hubId = hubId;
        this.companyId = companyId;
        this.stockQuantity = stockQuantity;
    }

    public void validateStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new ProductStockNotEnoughException();
        }
    }

    public void decreaseStock(int quantity) {
        validateStock(quantity);
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
