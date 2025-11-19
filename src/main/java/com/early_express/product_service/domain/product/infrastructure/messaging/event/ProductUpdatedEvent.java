package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 정보 수정 이벤트
 * - 상품명, 가격 등 기본 정보 변경 시 발행
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdatedEvent {

    private String eventId;
    private String eventType;
    private String productId;
    private String name;
    private BigDecimal price;
    private LocalDateTime updatedAt;

    public static ProductUpdatedEvent of(String productId, String name, BigDecimal price) {
        return ProductUpdatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PRODUCT_UPDATED")
                .productId(productId)
                .name(name)
                .price(price)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}