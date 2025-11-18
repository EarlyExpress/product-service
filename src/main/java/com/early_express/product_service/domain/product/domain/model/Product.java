package com.early_express.product_service.domain.product.domain.model;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Product 도메인 모델 (Aggregate Root)
 * - 순수 자바 객체 (JPA 의존성 없음)
 * - 비즈니스 로직 포함
 */
@Getter
public class Product {

    private final String productId;
    private final String sellerId;
    private String name;
    private String description;
    private Price price;
    private ProductStatus status;
    private boolean isSellable;
    private boolean hasEvent;
    private Integer minOrderQuantity;
    private Integer maxOrderQuantity;

    // Audit 필드 (BaseEntity와 매핑용)
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private boolean isDeleted;

    @Builder(access = AccessLevel.PRIVATE)
    private Product(
            String productId,
            String sellerId,
            String name,
            String description,
            Price price,
            ProductStatus status,
            boolean isSellable,
            boolean hasEvent,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy,
            LocalDateTime deletedAt,
            String deletedBy,
            boolean isDeleted
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
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.isDeleted = isDeleted;
    }

    /**
     * 새 상품 생성 (팩토리 메서드)
     */
    public static Product create(
            String productId,
            String sellerId,
            String name,
            String description,
            Price price,
            Integer minOrderQuantity,
            Integer maxOrderQuantity
    ) {
        validateName(name);
        validateOrderQuantity(minOrderQuantity, maxOrderQuantity);

        return Product.builder()
                .productId(productId)
                .sellerId(sellerId)
                .name(name)
                .description(description)
                .price(price)
                .status(ProductStatus.DRAFT)
                .isSellable(false)
                .hasEvent(false)
                .minOrderQuantity(minOrderQuantity)
                .maxOrderQuantity(maxOrderQuantity)
                .isDeleted(false)
                .build();
    }

    /**
     * 기존 상품 재구성 (from Entity)
     */
    public static Product reconstruct(
            String productId,
            String sellerId,
            String name,
            String description,
            Price price,
            ProductStatus status,
            boolean isSellable,
            boolean hasEvent,
            Integer minOrderQuantity,
            Integer maxOrderQuantity,
            LocalDateTime createdAt,
            String createdBy,
            LocalDateTime updatedAt,
            String updatedBy,
            LocalDateTime deletedAt,
            String deletedBy,
            boolean isDeleted
    ) {
        return Product.builder()
                .productId(productId)
                .sellerId(sellerId)
                .name(name)
                .description(description)
                .price(price)
                .status(status)
                .isSellable(isSellable)
                .hasEvent(hasEvent)
                .minOrderQuantity(minOrderQuantity)
                .maxOrderQuantity(maxOrderQuantity)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy)
                .isDeleted(isDeleted)
                .build();
    }

    // ==================== 비즈니스 로직 ====================

    /**
     * 상품 정보 수정
     */
    public void update(String name, String description, Price price) {
        if (!this.status.isModifiable()) {
            throw new ProductException(ProductErrorCode.CANNOT_MODIFY_DISCONTINUED_PRODUCT);
        }

        validateName(name);
        this.name = name;
        this.description = description;
        this.price = price;
    }

    /**
     * 판매 활성화
     */
    public void activate() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_DISCONTINUED);
        }

        this.status = ProductStatus.ACTIVE;
        this.isSellable = true;
    }

    /**
     * 판매 중지
     */
    public void suspend() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_DISCONTINUED);
        }

        this.status = ProductStatus.SUSPENDED;
        this.isSellable = false;
    }

    /**
     * 상품 단종
     */
    public void discontinue() {
        if (this.status == ProductStatus.DISCONTINUED) {
            throw new ProductException(ProductErrorCode.PRODUCT_ALREADY_DISCONTINUED);
        }

        this.status = ProductStatus.DISCONTINUED;
        this.isSellable = false;
    }

    /**
     * 품절 처리
     */
    public void markOutOfStock() {
        this.status = ProductStatus.OUT_OF_STOCK;
        this.isSellable = false;
    }

    /**
     * 이벤트 활성화/비활성화
     */
    public void setEventStatus(boolean hasEvent) {
        this.hasEvent = hasEvent;
    }

    /**
     * 주문 수량 검증
     */
    public void validateOrderQuantity(int quantity) {
        if (quantity < minOrderQuantity) {
            throw new ProductException(
                    ProductErrorCode.ORDER_QUANTITY_BELOW_MINIMUM,
                    String.format("최소 주문 수량: %d, 요청 수량: %d", minOrderQuantity, quantity)
            );
        }

        if (quantity > maxOrderQuantity) {
            throw new ProductException(
                    ProductErrorCode.ORDER_QUANTITY_EXCEEDS_MAXIMUM,
                    String.format("최대 주문 수량: %d, 요청 수량: %d", maxOrderQuantity, quantity)
            );
        }
    }

    /**
     * 소프트 삭제
     */
    public void delete(String deletedBy) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 소프트 삭제 복구
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    // ==================== 검증 로직 ====================

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductException(
                    ProductErrorCode.INVALID_PRODUCT_NAME,
                    "상품명은 필수입니다."
            );
        }

        if (name.length() > 100) {
            throw new ProductException(
                    ProductErrorCode.INVALID_PRODUCT_NAME,
                    "상품명은 100자 이하여야 합니다."
            );
        }
    }

    private static void validateOrderQuantity(Integer minOrderQuantity, Integer maxOrderQuantity) {
        if (minOrderQuantity == null || minOrderQuantity < 1) {
            throw new ProductException(
                    ProductErrorCode.INVALID_MIN_MAX_ORDER_QUANTITY,
                    "최소 주문 수량은 1 이상이어야 합니다."
            );
        }

        if (maxOrderQuantity == null || maxOrderQuantity < 1) {
            throw new ProductException(
                    ProductErrorCode.INVALID_MIN_MAX_ORDER_QUANTITY,
                    "최대 주문 수량은 1 이상이어야 합니다."
            );
        }

        if (minOrderQuantity > maxOrderQuantity) {
            throw new ProductException(
                    ProductErrorCode.INVALID_MIN_MAX_ORDER_QUANTITY,
                    String.format("최소 주문 수량(%d)은 최대 주문 수량(%d)보다 작거나 같아야 합니다.",
                            minOrderQuantity, maxOrderQuantity)
            );
        }
    }

    // ==================== 조회 메서드 ====================

    public boolean isOwnedBy(String userId) {
        return this.sellerId.equals(userId);
    }

    public boolean isDiscontinued() {
        return this.status == ProductStatus.DISCONTINUED;
    }

    public boolean canBeSold() {
        return this.isSellable && this.status == ProductStatus.ACTIVE;
    }
}
