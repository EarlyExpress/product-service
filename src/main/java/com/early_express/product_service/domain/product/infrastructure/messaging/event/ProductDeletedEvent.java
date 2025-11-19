package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 삭제(단종) 이벤트
 * - Inventory 서비스에서 해당 상품의 모든 재고 삭제
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDeletedEvent {

    private String eventId;
    private String eventType;
    private String productId;
    private String sellerId;
    private LocalDateTime deletedAt;

    public static ProductDeletedEvent of(String productId, String sellerId) {
        return ProductDeletedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PRODUCT_DELETED")
                .productId(productId)
                .sellerId(sellerId)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
