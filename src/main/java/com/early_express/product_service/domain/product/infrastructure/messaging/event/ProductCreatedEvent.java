package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 생성 이벤트
 * - Inventory 서비스에서 초기 재고 생성용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {

    private String eventId;
    private String eventType;
    private String productId;
    private String sellerId;
    private String name;
    private LocalDateTime createdAt;

    public static ProductCreatedEvent of(String productId, String sellerId, String name) {
        return ProductCreatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PRODUCT_CREATED")
                .productId(productId)
                .sellerId(sellerId)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
