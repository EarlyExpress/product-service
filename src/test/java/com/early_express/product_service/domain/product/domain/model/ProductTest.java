package com.early_express.product_service.domain.product.domain.model;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 도메인 모델 테스트")
class ProductTest {

    @Test
    @DisplayName("유효한 정보로 상품 생성")
    void createProduct_withValidInfo() {
        // given
        String productId = "PROD-001";
        String sellerId = "SELLER-001";
        String name = "테스트 상품";
        String description = "테스트 설명";
        Price price = Price.of(10000);
        Integer minOrderQuantity = 1;
        Integer maxOrderQuantity = 100;

        // when
        Product product = Product.create(
                productId, sellerId, name, description, price,
                minOrderQuantity, maxOrderQuantity
        );

        // then
        assertThat(product.getProductId()).isEqualTo(productId);
        assertThat(product.getSellerId()).isEqualTo(sellerId);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.isSellable()).isFalse();
        assertThat(product.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("상품명이 null이거나 빈 문자열인 경우 예외 발생")
    void createProduct_withInvalidName_throwsException() {
        // given
        String productId = "PROD-001";
        String sellerId = "SELLER-001";
        Price price = Price.of(10000);

        // when & then
        assertThatThrownBy(() -> Product.create(
                productId, sellerId, null, "설명", price, 1, 100
        ))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("상품명은 필수입니다");

        assertThatThrownBy(() -> Product.create(
                productId, sellerId, "", "설명", price, 1, 100
        ))
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_PRODUCT_NAME);
    }

    @Test
    @DisplayName("상품명이 100자를 초과하는 경우 예외 발생")
    void createProduct_withTooLongName_throwsException() {
        // given
        String productId = "PROD-001";
        String sellerId = "SELLER-001";
        String tooLongName = "a".repeat(101);
        Price price = Price.of(10000);

        // when & then
        assertThatThrownBy(() -> Product.create(
                productId, sellerId, tooLongName, "설명", price, 1, 100
        ))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("100자 이하");
    }

    @Test
    @DisplayName("최소 주문 수량이 최대 주문 수량보다 큰 경우 예외 발생")
    void createProduct_withInvalidOrderQuantity_throwsException() {
        // given
        String productId = "PROD-001";
        String sellerId = "SELLER-001";
        Price price = Price.of(10000);

        // when & then
        assertThatThrownBy(() -> Product.create(
                productId, sellerId, "상품", "설명", price, 100, 10
        ))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("최소 주문 수량")
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_MIN_MAX_ORDER_QUANTITY);
    }

    @Test
    @DisplayName("상품 정보 수정")
    void updateProduct() {
        // given
        Product product = createTestProduct();
        String newName = "수정된 상품명";
        String newDescription = "수정된 설명";
        Price newPrice = Price.of(15000);

        // when
        product.update(newName, newDescription, newPrice);

        // then
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getDescription()).isEqualTo(newDescription);
        assertThat(product.getPrice()).isEqualTo(newPrice);
    }

    @Test
    @DisplayName("상품 활성화")
    void activateProduct() {
        // given
        Product product = createTestProduct();

        // when
        product.activate();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.isSellable()).isTrue();
    }

    @Test
    @DisplayName("상품 판매 중지")
    void suspendProduct() {
        // given
        Product product = createTestProduct();
        product.activate();

        // when
        product.suspend();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.SUSPENDED);
        assertThat(product.isSellable()).isFalse();
    }

    @Test
    @DisplayName("상품 단종")
    void discontinueProduct() {
        // given
        Product product = createTestProduct();
        product.activate();

        // when
        product.discontinue();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        assertThat(product.isSellable()).isFalse();
        assertThat(product.isDiscontinued()).isTrue();
    }

    @Test
    @DisplayName("이미 단종된 상품은 단종 불가")
    void discontinueProduct_alreadyDiscontinued_throwsException() {
        // given
        Product product = createTestProduct();
        product.discontinue();

        // when & then
        assertThatThrownBy(product::discontinue)
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ALREADY_DISCONTINUED);
    }

    @Test
    @DisplayName("단종된 상품은 수정 불가")
    void updateProduct_discontinued_throwsException() {
        // given
        Product product = createTestProduct();
        product.discontinue();

        // when & then
        assertThatThrownBy(() -> product.update("새 이름", "새 설명", Price.of(20000)))
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.CANNOT_MODIFY_DISCONTINUED_PRODUCT);
    }

    @Test
    @DisplayName("주문 수량 검증 - 최소 수량 미달")
    void validateOrderQuantity_belowMinimum_throwsException() {
        // given
        Product product = createTestProduct();
        int quantity = 0; // 최소 주문 수량: 1

        // when & then
        assertThatThrownBy(() -> product.validateOrderQuantity(quantity))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("최소 주문 수량")
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.ORDER_QUANTITY_BELOW_MINIMUM);
    }

    @Test
    @DisplayName("주문 수량 검증 - 최대 수량 초과")
    void validateOrderQuantity_exceedsMaximum_throwsException() {
        // given
        Product product = createTestProduct();
        int quantity = 101; // 최대 주문 수량: 100

        // when & then
        assertThatThrownBy(() -> product.validateOrderQuantity(quantity))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("최대 주문 수량")
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.ORDER_QUANTITY_EXCEEDS_MAXIMUM);
    }

    @Test
    @DisplayName("유효한 주문 수량 검증 통과")
    void validateOrderQuantity_valid() {
        // given
        Product product = createTestProduct();
        int quantity = 50; // 1 <= 50 <= 100

        // when & then
        assertThatNoException().isThrownBy(() -> product.validateOrderQuantity(quantity));
    }

    @Test
    @DisplayName("품절 처리")
    void markOutOfStock() {
        // given
        Product product = createTestProduct();
        product.activate();

        // when
        product.markOutOfStock();

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        assertThat(product.isSellable()).isFalse();
    }

    @Test
    @DisplayName("이벤트 상태 변경")
    void setEventStatus() {
        // given
        Product product = createTestProduct();

        // when
        product.setEventStatus(true);

        // then
        assertThat(product.isHasEvent()).isTrue();
    }

    @Test
    @DisplayName("소프트 삭제")
    void deleteProduct() {
        // given
        Product product = createTestProduct();
        String deletedBy = "ADMIN-001";

        // when
        product.delete(deletedBy);

        // then
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getDeletedBy()).isEqualTo(deletedBy);
        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("소프트 삭제 복구")
    void restoreProduct() {
        // given
        Product product = createTestProduct();
        product.delete("ADMIN-001");

        // when
        product.restore();

        // then
        assertThat(product.isDeleted()).isFalse();
        assertThat(product.getDeletedBy()).isNull();
        assertThat(product.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("소유자 확인")
    void isOwnedBy() {
        // given
        Product product = createTestProduct();
        String sellerId = "SELLER-001";
        String otherUserId = "SELLER-002";

        // when & then
        assertThat(product.isOwnedBy(sellerId)).isTrue();
        assertThat(product.isOwnedBy(otherUserId)).isFalse();
    }

    @Test
    @DisplayName("판매 가능 여부 확인")
    void canBeSold() {
        // given
        Product product = createTestProduct();

        // when & then
        assertThat(product.canBeSold()).isFalse(); // DRAFT 상태

        product.activate();
        assertThat(product.canBeSold()).isTrue(); // ACTIVE 상태

        product.suspend();
        assertThat(product.canBeSold()).isFalse(); // SUSPENDED 상태
    }

    private Product createTestProduct() {
        return Product.create(
                "PROD-001",
                "SELLER-001",
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );
    }
}