package com.early_express.product_service.domain.product.domain.messaging.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 상품 상태 변경 이벤트 데이터 (도메인 DTO)
 * Product Service → 외부 서비스
 */
@Getter
@Builder
public class ProductStatusChangedEventData {

    /**
     * 상품 ID
     */
    private final String productId;

    /**
     * 이전 상태
     */
    private final String oldStatus;

    /**
     * 새 상태
     */
    private final String newStatus;

    /**
     * 변경 시간
     */
    private final LocalDateTime changedAt;

    /**
     * 이벤트 데이터 생성
     */
    public static ProductStatusChangedEventData of(
            String productId,
            String oldStatus,
            String newStatus) {

        return ProductStatusChangedEventData.builder()
                .productId(productId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();
    }
}