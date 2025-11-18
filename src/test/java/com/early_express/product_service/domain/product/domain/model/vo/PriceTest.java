package com.early_express.product_service.domain.product.domain.model.vo;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Price Value Object 테스트")
class PriceTest {

    @Test
    @DisplayName("유효한 가격으로 Price 생성")
    void createPrice_withValidAmount() {
        // given
        BigDecimal amount = BigDecimal.valueOf(10000);

        // when
        Price price = Price.of(amount);

        // then
        assertThat(price.getAmount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("long 타입으로 Price 생성")
    void createPrice_withLongAmount() {
        // given
        long amount = 10000L;

        // when
        Price price = Price.of(amount);

        // then
        assertThat(price.getAmount()).isEqualTo(BigDecimal.valueOf(amount));
    }

    @Test
    @DisplayName("0 이하의 가격으로 생성 시 예외 발생")
    void createPrice_withZeroOrNegative_throwsException() {
        // given
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal negative = BigDecimal.valueOf(-1000);

        // when & then
        assertThatThrownBy(() -> Price.of(zero))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("가격은 0보다 커야 합니다");

        assertThatThrownBy(() -> Price.of(negative))
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_PRICE);
    }

    @Test
    @DisplayName("null 가격으로 생성 시 예외 발생")
    void createPrice_withNull_throwsException() {
        // when & then
        assertThatThrownBy(() -> Price.of(null))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining("null일 수 없습니다");
    }

    @Test
    @DisplayName("할인율 적용")
    void applyDiscount() {
        // given
        Price price = Price.of(10000);
        BigDecimal discountRate = BigDecimal.valueOf(20); // 20%

        // when
        Price discountedPrice = price.applyDiscount(discountRate);

        // then
        assertThat(discountedPrice.getAmount()).isEqualTo(BigDecimal.valueOf(8000));
    }

    @Test
    @DisplayName("유효하지 않은 할인율 적용 시 예외 발생")
    void applyDiscount_withInvalidRate_throwsException() {
        // given
        Price price = Price.of(10000);
        BigDecimal negativeRate = BigDecimal.valueOf(-10);
        BigDecimal overRate = BigDecimal.valueOf(101);

        // when & then
        assertThatThrownBy(() -> price.applyDiscount(negativeRate))
                .isInstanceOf(ProductException.class)
                .extracting(e -> ((ProductException) e).getErrorCode())
                .isEqualTo(ProductErrorCode.INVALID_DISCOUNT_RATE);

        assertThatThrownBy(() -> price.applyDiscount(overRate))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("가격 비교 - isGreaterThan")
    void priceComparison_isGreaterThan() {
        // given
        Price price1 = Price.of(10000);
        Price price2 = Price.of(5000);

        // when & then
        assertThat(price1.isGreaterThan(price2)).isTrue();
        assertThat(price2.isGreaterThan(price1)).isFalse();
    }

    @Test
    @DisplayName("가격 비교 - isLessThan")
    void priceComparison_isLessThan() {
        // given
        Price price1 = Price.of(5000);
        Price price2 = Price.of(10000);

        // when & then
        assertThat(price1.isLessThan(price2)).isTrue();
        assertThat(price2.isLessThan(price1)).isFalse();
    }

    @Test
    @DisplayName("동일한 가격은 equals와 hashCode가 같음")
    void priceEquality() {
        // given
        Price price1 = Price.of(10000);
        Price price2 = Price.of(10000);

        // when & then
        assertThat(price1).isEqualTo(price2);
        assertThat(price1.hashCode()).isEqualTo(price2.hashCode());
    }
}