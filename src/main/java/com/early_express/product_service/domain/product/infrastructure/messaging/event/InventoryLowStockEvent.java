package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 부족 이벤트 (Inventory 서비스에서 발행)
 * - Product 서비스에서 구독하여 품절 처리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLowStockEvent {

    private String eventId;
    private String eventType;
    private String inventoryId;
    private String productId;
    private String hubId;
    private Integer currentQuantity;
    private Integer safetyStock;
    private LocalDateTime detectedAt;
}
