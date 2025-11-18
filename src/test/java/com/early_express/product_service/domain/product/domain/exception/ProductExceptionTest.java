package com.early_express.product_service.domain.product.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductException 단위 테스트")
class ProductExceptionTest {

    @Test
    @DisplayName("ErrorCode만으로 예외 생성")
    void createException_withErrorCodeOnly() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_NOT_FOUND;

        // when
        ProductException exception = new ProductException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("ErrorCode와 커스텀 메시지로 예외 생성")
    void createException_withErrorCodeAndCustomMessage() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.INVALID_PRICE;
        String customMessage = "가격은 0원보다 커야 합니다. 입력값: -1000";

        // when
        ProductException exception = new ProductException(errorCode, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("ErrorCode와 원인 예외로 예외 생성")
    void createException_withErrorCodeAndCause() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_CREATION_FAILED;
        RuntimeException cause = new RuntimeException("Database connection failed");

        // when
        ProductException exception = new ProductException(errorCode, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo("상품 생성에 실패했습니다.");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ErrorCode, 커스텀 메시지, 원인 예외로 예외 생성")
    void createException_withErrorCodeAndMessageAndCause() {
        // given
        ProductErrorCode errorCode = ProductErrorCode.PRODUCT_UPDATE_FAILED;
        String customMessage = "상품 수정 중 데이터베이스 오류 발생";
        RuntimeException cause = new RuntimeException("Connection timeout");

        // when
        ProductException exception = new ProductException(errorCode, customMessage, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("HTTP 상태 코드가 올바르게 매핑되는지 확인")
    void errorCode_httpStatusMapping() {
        // given & when & then
        assertThat(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND)
                .getErrorCode().getStatus()).isEqualTo(404);

        assertThat(new ProductException(ProductErrorCode.INVALID_PRICE)
                .getErrorCode().getStatus()).isEqualTo(400);

        assertThat(new ProductException(ProductErrorCode.NOT_PRODUCT_OWNER)
                .getErrorCode().getStatus()).isEqualTo(403);

        assertThat(new ProductException(ProductErrorCode.PRODUCT_ALREADY_EXISTS)
                .getErrorCode().getStatus()).isEqualTo(409);

        assertThat(new ProductException(ProductErrorCode.PRODUCT_CREATION_FAILED)
                .getErrorCode().getStatus()).isEqualTo(500);

        assertThat(new ProductException(ProductErrorCode.INVENTORY_SERVICE_UNAVAILABLE)
                .getErrorCode().getStatus()).isEqualTo(503);
    }
}