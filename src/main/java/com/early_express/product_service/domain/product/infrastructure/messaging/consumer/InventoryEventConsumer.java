package com.early_express.product_service.domain.product.infrastructure.messaging.consumer;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.InventoryLowStockEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.InventoryRestockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Inventory 이벤트 구독자
 * - Inventory 서비스에서 발행하는 이벤트 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final ProductService productService;

    /**
     * 재고 부족 이벤트 처리
     * - 상품을 품절 상태로 변경
     */
    @KafkaListener(
            topics = "inventory-service-events",
            groupId = "product-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryLowStockEvent(
            @Payload InventoryLowStockEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("InventoryLowStockEvent 수신: productId={}, hubId={}, currentQuantity={}, partition={}, offset={}",
                    event.getProductId(),
                    event.getHubId(),
                    event.getCurrentQuantity(),
                    partition,
                    offset);

            // 품절 처리
            productService.markAsOutOfStock(event.getProductId());

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("InventoryLowStockEvent 처리 완료: productId={}", event.getProductId());

        } catch (Exception e) {
            log.error("InventoryLowStockEvent 처리 실패: productId={}, error={}",
                    event.getProductId(), e.getMessage(), e);
            // 예외 발생 시 재시도 (Spring Kafka 자동 재시도)
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }

    /**
     * 재입고 이벤트 처리
     * - 품절 상태 해제
     */
    @KafkaListener(
            topics = "inventory-service-events",
            groupId = "product-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryRestockedEvent(
            @Payload InventoryRestockedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("InventoryRestockedEvent 수신: productId={}, hubId={}, restockedQuantity={}, partition={}, offset={}",
                    event.getProductId(),
                    event.getHubId(),
                    event.getRestockedQuantity(),
                    partition,
                    offset);

            // 품절 해제
            productService.restoreFromOutOfStock(event.getProductId());

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("InventoryRestockedEvent 처리 완료: productId={}", event.getProductId());

        } catch (Exception e) {
            log.error("InventoryRestockedEvent 처리 실패: productId={}, error={}",
                    event.getProductId(), e.getMessage(), e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
}
