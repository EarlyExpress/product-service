package com.early_express.product_service.domain.product.domain.model.vo;

/**
 * 상품 상태
 */
public enum ProductStatus {
    /**
     * 임시 저장 (판매자가 작성 중)
     */
    DRAFT,

    /**
     * 판매 중
     */
    ACTIVE,

    /**
     * 판매 중지 (일시적)
     */
    SUSPENDED,

    /**
     * 품절
     */
    OUT_OF_STOCK,

    /**
     * 단종
     */
    DISCONTINUED;

    /**
     * 판매 가능 상태인지 확인
     */
    public boolean isSellable() {
        return this == ACTIVE;
    }

    /**
     * 수정 가능 상태인지 확인
     */
    public boolean isModifiable() {
        return this != DISCONTINUED;
    }
}
