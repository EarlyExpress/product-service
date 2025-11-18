package com.early_express.product_service.domain.product.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductErrorCode 단위 테스트")
class ProductErrorCodeTest {

    @Test
    @DisplayName("PRODUCT_NOT_FOUND 에러 코드 정보 확인")
    void productNotFound_errorCode() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_NOT_FOUND;

        // when & then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT_001");
        assertThat(errorCode.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
        assertThat(errorCode.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("INVALID_PRICE 에러 코드 정보 확인")
    void invalidPrice_errorCode() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.INVALID_PRICE;

        // when & then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT_102");
        assertThat(errorCode.getMessage()).isEqualTo("가격은 0보다 커야 합니다.");
        assertThat(errorCode.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("NOT_PRODUCT_OWNER 에러 코드 정보 확인")
    void notProductOwner_errorCode() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.NOT_PRODUCT_OWNER;

        // when & then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT_301");
        assertThat(errorCode.getMessage()).isEqualTo("해당 상품의 소유자가 아닙니다.");
        assertThat(errorCode.getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("PRODUCT_ALREADY_EXISTS 에러 코드 정보 확인")
    void productAlreadyExists_errorCode() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_ALREADY_EXISTS;

        // when & then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT_401");
        assertThat(errorCode.getMessage()).isEqualTo("이미 존재하는 상품입니다.");
        assertThat(errorCode.getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("INVENTORY_SERVICE_UNAVAILABLE 에러 코드 정보 확인")
    void inventoryServiceUnavailable_errorCode() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.INVENTORY_SERVICE_UNAVAILABLE;

        // when & then
        assertThat(errorCode.getCode()).isEqualTo("PRODUCT_601");
        assertThat(errorCode.getMessage()).isEqualTo("재고 서비스에 연결할 수 없습니다.");
        assertThat(errorCode.getStatus()).isEqualTo(503);
    }
}