package com.early_express.product_service.domain.product.infrastructure.persistence.entity;

import com.early_express.product_service.global.infrastructure.entity.BaseEntity;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product JPA Entity
 * - BaseEntity 상속 (Audit 필드)
 * - Domain Model과 완전 분리
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity extends BaseEntity {

    @Id
    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "seller_id", length = 36, nullable = false)
    private String sellerId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ProductStatus status;

    @Column(name = "is_sellable", nullable = false)
    private boolean isSellable;

    @Column(name = "has_event", nullable = false)
    private boolean hasEvent;

    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity;

    @Column(name = "max_order_quantity", nullable = false)
    private Integer maxOrderQuantity;

    @Builder
    private ProductEntity(
            String productId,
            String sellerId,
            String name,
            String description,
            BigDecimal price,
            ProductStatus status,
            boolean isSellable,
            boolean hasEvent,
            Integer minOrderQuantity,
            Integer maxOrderQuantity
    ) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.isSellable = isSellable;
        this.hasEvent = hasEvent;
        this.minOrderQuantity = minOrderQuantity;
        this.maxOrderQuantity = maxOrderQuantity;
    }

    /**
     * Domain Model -> Entity 변환 (신규 생성)
     * ID가 없으면 자동 생성
     */
    public static ProductEntity fromDomain(Product product) {
        String productId = product.getProductId();
        if (productId == null || productId.isBlank()) {
            productId = com.early_express.product_service.global.common.utils.UuidUtils.generate();
        }

        return ProductEntity.builder()
                .productId(productId)
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().getAmount())
                .status(product.getStatus())
                .isSellable(product.isSellable())
                .hasEvent(product.isHasEvent())
                .minOrderQuantity(product.getMinOrderQuantity())
                .maxOrderQuantity(product.getMaxOrderQuantity())
                .build();
    }

    /**
     * Domain Model -> Entity 변환 (업데이트 시 ID 포함)
     */
    public static ProductEntity fromDomainWithId(Product product) {
        ProductEntity entity = fromDomain(product);

        // BaseEntity의 삭제 정보 수동 설정
        if (product.isDeleted()) {
            entity.delete(product.getDeletedBy());
        }

        return entity;
    }

    /**
     * Entity -> Domain Model 변환
     */
    public Product toDomain() {
        return Product.reconstruct(
                this.productId,
                this.sellerId,
                this.name,
                this.description,
                Price.of(this.price),
                this.status,
                this.isSellable,
                this.hasEvent,
                this.minOrderQuantity,
                this.maxOrderQuantity,
                this.getCreatedAt(),
                this.getCreatedBy(),
                this.getUpdatedAt(),
                this.getUpdatedBy(),
                this.getDeletedAt(),
                this.getDeletedBy(),
                this.isDeleted()
        );
    }

    /**
     * Domain Model의 변경사항을 Entity에 반영
     */
    public void updateFromDomain(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice().getAmount();
        this.status = product.getStatus();
        this.isSellable = product.isSellable();
        this.hasEvent = product.isHasEvent();
        this.minOrderQuantity = product.getMinOrderQuantity();
        this.maxOrderQuantity = product.getMaxOrderQuantity();

        // 삭제 상태 동기화
        if (product.isDeleted() && !this.isDeleted()) {
            this.delete(product.getDeletedBy());
        } else if (!product.isDeleted() && this.isDeleted()) {
            this.restore();
        }
    }
}