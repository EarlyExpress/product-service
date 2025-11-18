package com.early_express.product_service.domain.product.infrastructure.messaging.consumer;

import com.early_express.product_service.domain.product.application.service.ProductService;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.InventoryLowStockEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.InventoryRestockedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * InventoryEventConsumer 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryEventConsumer 테스트")
class InventoryEventConsumerTest {

    @Mock
    private ProductService productService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private InventoryEventConsumer inventoryEventConsumer;

    private static final String TEST_PRODUCT_ID = "PROD-001";
    private static final String TEST_HUB_ID = "HUB-SEOUL";

    @Nested
    @DisplayName("재고 부족 이벤트 처리")
    class HandleInventoryLowStockEventTest {

        @Test
        @DisplayName("InventoryLowStockEvent 수신 시 품절 처리 성공")
        void handleInventoryLowStockEvent_Success() {
            // given
            InventoryLowStockEvent event = InventoryLowStockEvent.builder()
                    .eventId("event-001")
                    .eventType("INVENTORY_LOW_STOCK")
                    .inventoryId("INV-001")
                    .productId(TEST_PRODUCT_ID)
                    .hubId(TEST_HUB_ID)
                    .currentQuantity(5)
                    .safetyStock(10)
                    .detectedAt(LocalDateTime.now())
                    .build();

            willDoNothing().given(productService).markAsOutOfStock(TEST_PRODUCT_ID);
            willDoNothing().given(acknowledgment).acknowledge();

            // when
            inventoryEventConsumer.handleInventoryLowStockEvent(event, 0, 100L, acknowledgment);

            // then
            verify(productService).markAsOutOfStock(TEST_PRODUCT_ID);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("InventoryLowStockEvent 처리 중 예외 발생 시 RuntimeException으로 래핑")
        void handleInventoryLowStockEvent_ThrowsException() {
            // given
            InventoryLowStockEvent event = InventoryLowStockEvent.builder()
                    .eventId("event-002")
                    .eventType("INVENTORY_LOW_STOCK")
                    .productId(TEST_PRODUCT_ID)
                    .hubId(TEST_HUB_ID)
                    .currentQuantity(5)
                    .safetyStock(10)
                    .build();

            willThrow(new RuntimeException("품절 처리 실패"))
                    .given(productService).markAsOutOfStock(TEST_PRODUCT_ID);

            // when & then
            assertThatThrownBy(() ->
                    inventoryEventConsumer.handleInventoryLowStockEvent(event, 0, 100L, acknowledgment))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이벤트 처리 실패");

            verify(acknowledgment, never()).acknowledge();
        }
    }

    @Nested
    @DisplayName("재입고 이벤트 처리")
    class HandleInventoryRestockedEventTest {

        @Test
        @DisplayName("InventoryRestockedEvent 수신 시 품절 해제 성공")
        void handleInventoryRestockedEvent_Success() {
            // given
            InventoryRestockedEvent event = InventoryRestockedEvent.builder()
                    .eventId("event-003")
                    .eventType("INVENTORY_RESTOCKED")
                    .inventoryId("INV-001")
                    .productId(TEST_PRODUCT_ID)
                    .hubId(TEST_HUB_ID)
                    .restockedQuantity(100)
                    .currentQuantity(105)
                    .restockedAt(LocalDateTime.now())
                    .build();

            willDoNothing().given(productService).restoreFromOutOfStock(TEST_PRODUCT_ID);
            willDoNothing().given(acknowledgment).acknowledge();

            // when
            inventoryEventConsumer.handleInventoryRestockedEvent(event, 0, 100L, acknowledgment);

            // then
            verify(productService).restoreFromOutOfStock(TEST_PRODUCT_ID);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("InventoryRestockedEvent 처리 중 예외 발생 시 RuntimeException으로 래핑")
        void handleInventoryRestockedEvent_ThrowsException() {
            // given
            InventoryRestockedEvent event = InventoryRestockedEvent.builder()
                    .eventId("event-004")
                    .eventType("INVENTORY_RESTOCKED")
                    .productId(TEST_PRODUCT_ID)
                    .hubId(TEST_HUB_ID)
                    .restockedQuantity(100)
                    .currentQuantity(105)
                    .build();

            willThrow(new RuntimeException("품절 해제 실패"))
                    .given(productService).restoreFromOutOfStock(TEST_PRODUCT_ID);

            // when & then
            assertThatThrownBy(() ->
                    inventoryEventConsumer.handleInventoryRestockedEvent(event, 0, 100L, acknowledgment))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("이벤트 처리 실패");

            verify(acknowledgment, never()).acknowledge();
        }
    }
}