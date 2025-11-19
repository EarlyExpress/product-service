package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상품 상태 변경 이벤트
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusChangedEvent {

    private String eventId;
    private String eventType;
    private String productId;
    private ProductStatus oldStatus;
    private ProductStatus newStatus;
    private LocalDateTime changedAt;

    public static ProductStatusChangedEvent of(
            String productId,
            ProductStatus oldStatus,
            ProductStatus newStatus
    ) {
        return ProductStatusChangedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType("PRODUCT_STATUS_CHANGED")
                .productId(productId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
