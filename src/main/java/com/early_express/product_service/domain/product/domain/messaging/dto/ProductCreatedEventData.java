package com.early_express.product_service.domain.product.domain.messaging.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 생성 이벤트 데이터 (도메인 DTO)
 * Product Service → Inventory Service
 */
@Getter
@Builder
public class ProductCreatedEventData {

    /**
     * 상품 ID
     */
    private final String productId;

    /**
     * 판매자 ID
     */
    private final String sellerId;

    /**
     * 허브 ID
     */
    private final String hubId;

    /**
     * 상품명
     */
    private final String name;

    /**
     * 생성 시간
     */
    private final LocalDateTime createdAt;

    /**
     * 이벤트 데이터 생성
     */
    public static ProductCreatedEventData of(
            String productId,
            String sellerId,
            String hubId,
            String name) {

        return ProductCreatedEventData.builder()
                .productId(productId)
                .sellerId(sellerId)
                .hubId(hubId)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
    }
}