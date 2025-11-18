package com.early_express.product_service.domain.product.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재입고 이벤트 (Inventory 서비스에서 발행)
 * - Product 서비스에서 구독하여 품절 해제
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRestockedEvent {

    private String eventId;
    private String eventType;
    private String inventoryId;
    private String productId;
    private String hubId;
    private Integer restockedQuantity;
    private Integer currentQuantity;
    private LocalDateTime restockedAt;
}
