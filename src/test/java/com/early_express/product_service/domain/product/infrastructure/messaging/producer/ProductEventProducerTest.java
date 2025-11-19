package com.early_express.product_service.domain.product.infrastructure.messaging.producer;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductCreatedEvent;
import com.early_express.product_service.domain.product.infrastructure.messaging.event.ProductStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

/**
 * ProductEventProducer 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductEventProducer 테스트")
class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SendResult<String, Object> sendResult;

    @InjectMocks
    private ProductEventProducer eventProducer;

    private Product testProduct;
    private static final String TEST_PRODUCT_ID = "PROD-001";
    private static final String TEST_SELLER_ID = "SELLER-001";
    private static final String TEST_COMPANY_ID = "COMPANY-001";
    private static final String TOPIC_NAME = "product-service-events";
    private static final String hubId = "hub-101";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventProducer, "applicationName", "product-service");

        testProduct = Product.create(
                TEST_PRODUCT_ID,
                TEST_SELLER_ID,
                TEST_COMPANY_ID,
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );
    }

    @Nested
    @DisplayName("ProductCreatedEvent 발행 테스트")
    class PublishProductCreatedTest {

        @Test
        @DisplayName("상품 생성 이벤트 발행 성공")
        void publishProductCreated_Success() {
            // given

            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
            given(kafkaTemplate.send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any(ProductCreatedEvent.class)))
                    .willReturn(future);

            // when
            eventProducer.publishProductCreated(testProduct, hubId);

            // then
            ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
            verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), eventCaptor.capture());

            ProductCreatedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEventType()).isEqualTo("PRODUCT_CREATED");
            assertThat(capturedEvent.getProductId()).isEqualTo(TEST_PRODUCT_ID);
            assertThat(capturedEvent.getSellerId()).isEqualTo(TEST_SELLER_ID);
            assertThat(capturedEvent.getName()).isEqualTo("테스트 상품");
        }

        @Test
        @DisplayName("상품 생성 이벤트 발행 실패 시 로그 기록")
        void publishProductCreated_Failure() {
            // given
            CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Kafka 발행 실패"));
            given(kafkaTemplate.send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any(ProductCreatedEvent.class)))
                    .willReturn(future);

            // when
            eventProducer.publishProductCreated(testProduct, hubId);

            // then
            verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any(ProductCreatedEvent.class));
            // 실패 시 로그만 기록하고 예외를 던지지 않음
        }
    }

    @Nested
    @DisplayName("ProductStatusChangedEvent 발행 테스트")
    class PublishProductStatusChangedTest {

        @Test
        @DisplayName("상품 상태 변경 이벤트 발행 성공")
        void publishProductStatusChanged_Success() {
            // given
            ProductStatus oldStatus = ProductStatus.DRAFT;
            ProductStatus newStatus = ProductStatus.ACTIVE;
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
            given(kafkaTemplate.send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any(ProductStatusChangedEvent.class)))
                    .willReturn(future);

            // when
            eventProducer.publishProductStatusChanged(TEST_PRODUCT_ID, oldStatus, newStatus);

            // then
            ArgumentCaptor<ProductStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ProductStatusChangedEvent.class);
            verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), eventCaptor.capture());

            ProductStatusChangedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getEventType()).isEqualTo("PRODUCT_STATUS_CHANGED");
            assertThat(capturedEvent.getProductId()).isEqualTo(TEST_PRODUCT_ID);
            assertThat(capturedEvent.getOldStatus()).isEqualTo(oldStatus);
            assertThat(capturedEvent.getNewStatus()).isEqualTo(newStatus);
            assertThat(capturedEvent.getChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("품절 상태 변경 이벤트 발행")
        void publishProductStatusChanged_OutOfStock() {
            // given
            ProductStatus oldStatus = ProductStatus.ACTIVE;
            ProductStatus newStatus = ProductStatus.OUT_OF_STOCK;
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
            given(kafkaTemplate.send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any(ProductStatusChangedEvent.class)))
                    .willReturn(future);

            // when
            eventProducer.publishProductStatusChanged(TEST_PRODUCT_ID, oldStatus, newStatus);

            // then
            ArgumentCaptor<ProductStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ProductStatusChangedEvent.class);
            verify(kafkaTemplate).send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), eventCaptor.capture());

            ProductStatusChangedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.getOldStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(capturedEvent.getNewStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        }
    }

    @Nested
    @DisplayName("이벤트 발행 공통 테스트")
    class CommonEventPublishTest {

        @Test
        @DisplayName("모든 이벤트는 productId를 키로 사용한다")
        void allEvents_UseProductIdAsKey() {
            // given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
            given(kafkaTemplate.send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any()))
                    .willReturn(future);

            // when
            eventProducer.publishProductCreated(testProduct, hubId);
            eventProducer.publishProductStatusChanged(TEST_PRODUCT_ID, ProductStatus.DRAFT, ProductStatus.ACTIVE);

            // then
            verify(kafkaTemplate, times(2)).send(eq(TOPIC_NAME), eq(TEST_PRODUCT_ID), any());
        }

        @Test
        @DisplayName("모든 이벤트는 동일한 토픽으로 발행된다")
        void allEvents_PublishToSameTopic() {
            // given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
            given(kafkaTemplate.send(eq(TOPIC_NAME), any(), any()))
                    .willReturn(future);

            // when
            eventProducer.publishProductCreated(testProduct, hubId);
            eventProducer.publishProductStatusChanged(TEST_PRODUCT_ID, ProductStatus.DRAFT, ProductStatus.ACTIVE);

            // then
            verify(kafkaTemplate, times(2)).send(eq(TOPIC_NAME), any(), any());
        }
    }
}