package com.early_express.product_service.domain.product.domain.messaging.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 삭제(단종) 이벤트 데이터 (도메인 DTO)
 * Product Service → Inventory Service
 */
@Getter
@Builder
public class ProductDeletedEventData {

    /**
     * 상품 ID
     */
    private final String productId;

    /**
     * 판매자 ID
     */
    private final String sellerId;

    /**
     * 삭제 시간
     */
    private final LocalDateTime deletedAt;

    /**
     * 이벤트 데이터 생성
     */
    public static ProductDeletedEventData of(
            String productId,
            String sellerId) {

        return ProductDeletedEventData.builder()
                .productId(productId)
                .sellerId(sellerId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}