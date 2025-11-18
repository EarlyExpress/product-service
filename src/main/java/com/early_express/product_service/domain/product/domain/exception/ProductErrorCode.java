package com.early_express.product_service.domain.product.domain.exception;

import com.early_express.product_service.global.presentation.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Product 도메인 전용 에러 코드
 * 코드 네이밍: PRODUCT_XXX
 * HTTP 상태: 주로 400(Bad Request), 404(Not Found), 409(Conflict)
 */
@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // ===== 404 Not Found =====
    PRODUCT_NOT_FOUND("PRODUCT_001", "상품을 찾을 수 없습니다.", 404),
    CATEGORY_NOT_FOUND("PRODUCT_002", "카테고리를 찾을 수 없습니다.", 404),
    DISCOUNT_POLICY_NOT_FOUND("PRODUCT_003", "할인 정책을 찾을 수 없습니다.", 404),

    // ===== 400 Bad Request - Validation =====
    INVALID_PRODUCT_NAME("PRODUCT_101", "상품명은 1자 이상 100자 이하여야 합니다.", 400),
    INVALID_PRICE("PRODUCT_102", "가격은 0보다 커야 합니다.", 400),
    INVALID_QUANTITY("PRODUCT_103", "수량은 0 이상이어야 합니다.", 400),
    INVALID_DISCOUNT_RATE("PRODUCT_104", "할인율은 0~100 사이여야 합니다.", 400),
    INVALID_PRODUCT_STATUS("PRODUCT_105", "유효하지 않은 상품 상태입니다.", 400),
    INVALID_WEIGHT("PRODUCT_106", "무게는 0보다 커야 합니다.", 400),
    INVALID_DIMENSIONS("PRODUCT_107", "크기 정보가 올바르지 않습니다.", 400),
    EMPTY_IMAGES("PRODUCT_108", "최소 1개 이상의 상품 이미지가 필요합니다.", 400),
    INVALID_MIN_MAX_ORDER_QUANTITY("PRODUCT_109", "최소 주문 수량은 최대 주문 수량보다 작아야 합니다.", 400),
    INVALID_CATEGORY_HIERARCHY("PRODUCT_110", "카테고리 계층 구조가 올바르지 않습니다.", 400),

    // ===== 400 Bad Request - Business Logic =====
    PRODUCT_ALREADY_DISCONTINUED("PRODUCT_201", "이미 단종된 상품입니다.", 400),
    PRODUCT_NOT_SELLABLE("PRODUCT_202", "판매 불가능한 상품입니다.", 400),
    INSUFFICIENT_STOCK("PRODUCT_203", "재고가 부족합니다.", 400),
    ORDER_QUANTITY_BELOW_MINIMUM("PRODUCT_204", "최소 주문 수량을 충족하지 못했습니다.", 400),
    ORDER_QUANTITY_EXCEEDS_MAXIMUM("PRODUCT_205", "최대 주문 수량을 초과했습니다.", 400),
    DISCOUNT_POLICY_EXPIRED("PRODUCT_206", "할인 기간이 종료되었습니다.", 400),
    DISCOUNT_POLICY_NOT_STARTED("PRODUCT_207", "할인 기간이 시작되지 않았습니다.", 400),

    // ===== 403 Forbidden =====
    NOT_PRODUCT_OWNER("PRODUCT_301", "해당 상품의 소유자가 아닙니다.", 403),
    CANNOT_MODIFY_DISCONTINUED_PRODUCT("PRODUCT_302", "단종된 상품은 수정할 수 없습니다.", 403),
    CANNOT_DELETE_PRODUCT_WITH_ACTIVE_ORDERS("PRODUCT_303", "진행 중인 주문이 있는 상품은 삭제할 수 없습니다.", 403),

    // ===== 409 Conflict =====
    PRODUCT_ALREADY_EXISTS("PRODUCT_401", "이미 존재하는 상품입니다.", 409),
    DUPLICATE_PRODUCT_NAME("PRODUCT_402", "중복된 상품명입니다.", 409),
    CATEGORY_HAS_PRODUCTS("PRODUCT_403", "하위 상품이 존재하는 카테고리는 삭제할 수 없습니다.", 409),
    CATEGORY_HAS_SUBCATEGORIES("PRODUCT_404", "하위 카테고리가 존재하는 카테고리는 삭제할 수 없습니다.", 409),

    // ===== 500 Internal Server Error =====
    PRODUCT_CREATION_FAILED("PRODUCT_501", "상품 생성에 실패했습니다.", 500),
    PRODUCT_UPDATE_FAILED("PRODUCT_502", "상품 수정에 실패했습니다.", 500),
    PRODUCT_DELETE_FAILED("PRODUCT_503", "상품 삭제에 실패했습니다.", 500),
    IMAGE_UPLOAD_FAILED("PRODUCT_504", "이미지 업로드에 실패했습니다.", 500),

    // ===== 503 Service Unavailable - External Service =====
    INVENTORY_SERVICE_UNAVAILABLE("PRODUCT_601", "재고 서비스에 연결할 수 없습니다.", 503),
    SELLER_SERVICE_UNAVAILABLE("PRODUCT_602", "판매자 서비스에 연결할 수 없습니다.", 503);

    private final String code;
    private final String message;
    private final int status;
}