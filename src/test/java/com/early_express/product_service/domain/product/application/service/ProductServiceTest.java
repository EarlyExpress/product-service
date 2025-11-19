package com.early_express.product_service.domain.product.application.service;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import com.early_express.product_service.domain.product.domain.messaging.ProductEventPublisher;
import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

/**
 * ProductService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private static final String TEST_PRODUCT_ID = "PROD-001";
    private static final String TEST_SELLER_ID = "SELLER-001";
    private static final String TEST_PRODUCT_NAME = "테스트 상품";

    @BeforeEach
    void setUp() {
        testProduct = Product.create(
                TEST_PRODUCT_ID,
                TEST_SELLER_ID,
                TEST_PRODUCT_NAME,
                "테스트 상품 설명",
                Price.of(10000),
                1,
                100
        );
    }

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("상품 생성 성공")
        void createProduct_Success() {
            // given
            String hubId = "hub-101";
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> {
                        Product product = invocation.getArgument(0);
                        return Product.reconstruct(
                                TEST_PRODUCT_ID,
                                product.getSellerId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                product.getStatus(),
                                product.isSellable(),
                                product.isHasEvent(),
                                product.getMinOrderQuantity(),
                                product.getMaxOrderQuantity(),
                                null, null, null, null, null, null, false
                        );
                    });

            // when
            Product result = productService.createProduct(
                    hubId,
                    TEST_SELLER_ID,
                    TEST_PRODUCT_NAME,
                    "상품 설명",
                    Price.of(10000),
                    1,
                    100
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(TEST_PRODUCT_ID);
            assertThat(result.getName()).isEqualTo(TEST_PRODUCT_NAME);
            assertThat(result.getStatus()).isEqualTo(ProductStatus.DRAFT);
            assertThat(result.isSellable()).isFalse();

            verify(productRepository).save(any(Product.class));
            verify(eventPublisher).publishProductCreated(any(Product.class), eq(hubId));
        }

        @Test
        @DisplayName("상품 생성 시 ProductCreatedEvent 발행")
        void createProduct_PublishesEvent() {
            // given
            String hubId = "hub-101";
            given(productRepository.save(any(Product.class)))
                    .willReturn(testProduct);
            willDoNothing().given(eventPublisher).publishProductCreated(any(Product.class), eq(hubId));

            // when
            productService.createProduct(
                    hubId,
                    TEST_SELLER_ID,
                    TEST_PRODUCT_NAME,
                    "상품 설명",
                    Price.of(10000),
                    1,
                    100
            );

            // then
            verify(eventPublisher).publishProductCreated(any(Product.class), eq(hubId));
        }
    }

    @Nested
    @DisplayName("상품 활성화 테스트")
    class ActivateProductTest {

        @Test
        @DisplayName("상품 활성화 성공")
        void activateProduct_Success() {
            // given
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Product result = productService.activateProduct(TEST_PRODUCT_ID);

            // then
            assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(result.isSellable()).isTrue();
            verify(eventPublisher).publishProductStatusChanged(
                    eq(TEST_PRODUCT_ID),
                    eq(ProductStatus.DRAFT),
                    eq(ProductStatus.ACTIVE)
            );
        }

        @Test
        @DisplayName("존재하지 않는 상품 활성화 시 예외 발생")
        void activateProduct_ProductNotFound() {
            // given
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.activateProduct(TEST_PRODUCT_ID))
                    .isInstanceOf(ProductException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("품절 처리 테스트")
    class MarkAsOutOfStockTest {

        @Test
        @DisplayName("품절 처리 성공")
        void markAsOutOfStock_Success() {
            // given
            testProduct.activate(); // ACTIVE 상태로 변경
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            productService.markAsOutOfStock(TEST_PRODUCT_ID);

            // then
            assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
            assertThat(testProduct.isSellable()).isFalse();
            verify(eventPublisher).publishProductStatusChanged(
                    eq(TEST_PRODUCT_ID),
                    eq(ProductStatus.ACTIVE),
                    eq(ProductStatus.OUT_OF_STOCK)
            );
        }

        @Test
        @DisplayName("이미 품절 상태면 이벤트 미발행")
        void markAsOutOfStock_AlreadyOutOfStock() {
            // given
            testProduct.markOutOfStock(); // 이미 품절 상태
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));

            // when
            productService.markAsOutOfStock(TEST_PRODUCT_ID);

            // then
            verify(productRepository, never()).save(any(Product.class));
            verify(eventPublisher, never()).publishProductStatusChanged(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("품절 해제 테스트")
    class RestoreFromOutOfStockTest {

        @Test
        @DisplayName("품절 해제 성공")
        void restoreFromOutOfStock_Success() {
            // given
            testProduct.markOutOfStock(); // 품절 상태로 변경
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            productService.restoreFromOutOfStock(TEST_PRODUCT_ID);

            // then
            assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(testProduct.isSellable()).isTrue();
            verify(eventPublisher).publishProductStatusChanged(
                    eq(TEST_PRODUCT_ID),
                    eq(ProductStatus.OUT_OF_STOCK),
                    eq(ProductStatus.ACTIVE)
            );
        }

        @Test
        @DisplayName("품절 상태가 아니면 처리 안 함")
        void restoreFromOutOfStock_NotOutOfStock() {
            // given
            testProduct.activate(); // ACTIVE 상태
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));

            // when
            productService.restoreFromOutOfStock(TEST_PRODUCT_ID);

            // then
            verify(productRepository, never()).save(any(Product.class));
            verify(eventPublisher, never()).publishProductStatusChanged(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_Success() {
            // given
            String newName = "수정된 상품명";
            String newDescription = "수정된 설명";
            Price newPrice = Price.of(20000);

            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Product result = productService.updateProduct(
                    TEST_PRODUCT_ID,
                    newName,
                    newDescription,
                    newPrice
            );

            // then
            assertThat(result.getName()).isEqualTo(newName);
            assertThat(result.getDescription()).isEqualTo(newDescription);
            assertThat(result.getPrice()).isEqualTo(newPrice);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("단종된 상품 수정 시 예외 발생")
        void updateProduct_DiscontinuedProduct() {
            // given
            testProduct.discontinue();
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(
                    TEST_PRODUCT_ID,
                    "새 이름",
                    "새 설명",
                    Price.of(20000)
            ))
                    .isInstanceOf(ProductException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.CANNOT_MODIFY_DISCONTINUED_PRODUCT);
        }
    }

    @Nested
    @DisplayName("상품 단종 테스트")
    class DiscontinueProductTest {

        @Test
        @DisplayName("상품 단종 성공")
        void discontinueProduct_Success() {
            // given
            testProduct.activate(); // ACTIVE 상태로 변경
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));
            given(productRepository.save(any(Product.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            productService.discontinueProduct(TEST_PRODUCT_ID);

            // then
            assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
            assertThat(testProduct.isSellable()).isFalse();
            verify(eventPublisher).publishProductStatusChanged(
                    eq(TEST_PRODUCT_ID),
                    eq(ProductStatus.ACTIVE),
                    eq(ProductStatus.DISCONTINUED)
            );
        }

        @Test
        @DisplayName("이미 단종된 상품 단종 시 예외 발생")
        void discontinueProduct_AlreadyDiscontinued() {
            // given
            testProduct.discontinue();
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));

            // when & then
            assertThatThrownBy(() -> productService.discontinueProduct(TEST_PRODUCT_ID))
                    .isInstanceOf(ProductException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_ALREADY_DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("상품 조회 테스트")
    class GetProductTest {

        @Test
        @DisplayName("상품 조회 성공")
        void getProduct_Success() {
            // given
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.of(testProduct));

            // when
            Product result = productService.getProduct(TEST_PRODUCT_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(TEST_PRODUCT_ID);
            assertThat(result.getName()).isEqualTo(TEST_PRODUCT_NAME);
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
        void getProduct_ProductNotFound() {
            // given
            given(productRepository.findById(TEST_PRODUCT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(TEST_PRODUCT_ID))
                    .isInstanceOf(ProductException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProductErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}