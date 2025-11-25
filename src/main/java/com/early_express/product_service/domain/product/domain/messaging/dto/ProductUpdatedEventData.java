package com.early_express.product_service.domain.product.domain.messaging.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 수정 이벤트 데이터 (도메인 DTO)
 * Product Service → 외부 서비스
 */
@Getter
@Builder
public class ProductUpdatedEventData {

    /**
     * 상품 ID
     */
    private final String productId;

    /**
     * 상품명
     */
    private final String name;

    /**
     * 가격
     */
    private final BigDecimal price;

    /**
     * 수정 시간
     */
    private final LocalDateTime updatedAt;

    /**
     * 이벤트 데이터 생성
     */
    public static ProductUpdatedEventData of(
            String productId,
            String name,
            BigDecimal price) {

        return ProductUpdatedEventData.builder()
                .productId(productId)
                .name(name)
                .price(price)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}