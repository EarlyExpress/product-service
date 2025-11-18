package com.early_express.product_service.domain.product.domain.model.vo;

import com.early_express.product_service.domain.product.domain.exception.ProductErrorCode;
import com.early_express.product_service.domain.product.domain.exception.ProductException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 가격 Value Object
 * - 불변 객체
 * - 가격 검증 로직 포함
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Price {

    private BigDecimal amount;

    private Price(BigDecimal amount) {
        validate(amount);
        this.amount = amount;
    }

    public static Price of(BigDecimal amount) {
        return new Price(amount);
    }

    public static Price of(long amount) {
        return new Price(BigDecimal.valueOf(amount));
    }

    private void validate(BigDecimal amount) {
        if (amount == null) {
            throw new ProductException(
                    ProductErrorCode.INVALID_PRICE,
                    "가격은 null일 수 없습니다."
            );
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductException(
                    ProductErrorCode.INVALID_PRICE,
                    "가격은 0보다 커야 합니다. 입력값: " + amount
            );
        }
    }

    /**
     * 할인 적용
     */
    public Price applyDiscount(BigDecimal discountRate) {
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 ||
                discountRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ProductException(ProductErrorCode.INVALID_DISCOUNT_RATE);
        }

        BigDecimal discountAmount = amount.multiply(discountRate)
                .divide(BigDecimal.valueOf(100));
        BigDecimal discountedPrice = amount.subtract(discountAmount);

        return Price.of(discountedPrice);
    }

    /**
     * 가격 비교
     */
    public boolean isGreaterThan(Price other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
