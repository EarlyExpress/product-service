package com.early_express.product_service.domain.product.infrastructure.persistence.entity;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductEntity 변환 테스트")
class ProductEntityTest {

    @Test
    @DisplayName("Domain → Entity 변환")
    void fromDomain() {
        // given
        Product product = createTestProduct();

        // when
        ProductEntity entity = ProductEntity.fromDomain(product);

        // then
        assertThat(entity.getProductId()).isEqualTo(product.getProductId());
        assertThat(entity.getSellerId()).isEqualTo(product.getSellerId());
        assertThat(entity.getName()).isEqualTo(product.getName());
        assertThat(entity.getDescription()).isEqualTo(product.getDescription());
        assertThat(entity.getPrice()).isEqualTo(product.getPrice().getAmount());
        assertThat(entity.getStatus()).isEqualTo(product.getStatus());
        assertThat(entity.isSellable()).isEqualTo(product.isSellable());
        assertThat(entity.isHasEvent()).isEqualTo(product.isHasEvent());
        assertThat(entity.getMinOrderQuantity()).isEqualTo(product.getMinOrderQuantity());
        assertThat(entity.getMaxOrderQuantity()).isEqualTo(product.getMaxOrderQuantity());
    }

    @Test
    @DisplayName("Domain의 ID가 없으면 자동 생성")
    void fromDomain_withNullId_autoGenerateId() {
        // given
        Product product = Product.create(
                null,  // ID가 null
                "SELLER-001",
                "COMPANY-001",
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );

        // when
        ProductEntity entity = ProductEntity.fromDomain(product);

        // then
        assertThat(entity.getProductId()).isNotNull();
        assertThat(entity.getProductId()).isNotBlank();
        assertThat(entity.getProductId()).hasSize(36); // UUID 표준 길이
    }

    @Test
    @DisplayName("Domain의 ID가 빈 문자열이면 자동 생성")
    void fromDomain_withBlankId_autoGenerateId() {
        // given
        Product product = Product.create(
                "",  // ID가 빈 문자열
                "SELLER-001",
                "COMPANY-001",
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );

        // when
        ProductEntity entity = ProductEntity.fromDomain(product);

        // then
        assertThat(entity.getProductId()).isNotNull();
        assertThat(entity.getProductId()).isNotBlank();
        assertThat(entity.getProductId()).hasSize(36); // UUID 표준 길이
    }

    @Test
    @DisplayName("Entity → Domain 변환")
    void toDomain() {
        // given
        ProductEntity entity = createTestEntity();

        // when
        Product product = entity.toDomain();

        // then
        assertThat(product.getProductId()).isEqualTo(entity.getProductId());
        assertThat(product.getSellerId()).isEqualTo(entity.getSellerId());
        assertThat(product.getName()).isEqualTo(entity.getName());
        assertThat(product.getDescription()).isEqualTo(entity.getDescription());
        assertThat(product.getPrice().getAmount()).isEqualTo(entity.getPrice());
        assertThat(product.getStatus()).isEqualTo(entity.getStatus());
        assertThat(product.isSellable()).isEqualTo(entity.isSellable());
        assertThat(product.isHasEvent()).isEqualTo(entity.isHasEvent());
    }

    @Test
    @DisplayName("Domain → Entity → Domain 변환 후 데이터 일치")
    void domainToEntityToDomain_dataConsistency() {
        // given
        Product originalProduct = createTestProduct();

        // when
        ProductEntity entity = ProductEntity.fromDomain(originalProduct);
        Product convertedProduct = entity.toDomain();

        // then
        assertThat(convertedProduct.getProductId()).isEqualTo(originalProduct.getProductId());
        assertThat(convertedProduct.getName()).isEqualTo(originalProduct.getName());
        assertThat(convertedProduct.getPrice()).isEqualTo(originalProduct.getPrice());
        assertThat(convertedProduct.getStatus()).isEqualTo(originalProduct.getStatus());
    }

    @Test
    @DisplayName("Domain 변경사항을 Entity에 반영")
    void updateFromDomain() {
        // given
        Product product = createTestProduct();
        ProductEntity entity = ProductEntity.fromDomain(product);

        // when
        product.update("수정된 상품명", "수정된 설명", Price.of(20000));
        product.activate();
        entity.updateFromDomain(product);

        // then
        assertThat(entity.getName()).isEqualTo("수정된 상품명");
        assertThat(entity.getDescription()).isEqualTo("수정된 설명");
        assertThat(entity.getPrice()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(entity.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(entity.isSellable()).isTrue();
    }

    @Test
    @DisplayName("Domain의 삭제 상태를 Entity에 반영")
    void updateFromDomain_withDeletedStatus() {
        // given
        Product product = createTestProduct();
        ProductEntity entity = ProductEntity.fromDomain(product);

        // when
        product.delete("ADMIN-001");
        entity.updateFromDomain(product);

        // then
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedBy()).isEqualTo("ADMIN-001");
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Domain의 복구 상태를 Entity에 반영")
    void updateFromDomain_withRestoredStatus() {
        // given
        Product product = createTestProduct();
        product.delete("ADMIN-001");
        ProductEntity entity = ProductEntity.fromDomain(product);
        entity.updateFromDomain(product);

        // when
        product.restore();
        entity.updateFromDomain(product);

        // then
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedBy()).isNull();
        assertThat(entity.getDeletedAt()).isNull();
    }

    private Product createTestProduct() {
        return Product.create(
                "PROD-001",
                "SELLER-001",
                "COMPANY-001",
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );
    }

    private ProductEntity createTestEntity() {
        return ProductEntity.builder()
                .productId("PROD-001")
                .sellerId("SELLER-001")
                .name("테스트 상품")
                .description("테스트 설명")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatus.DRAFT)
                .isSellable(false)
                .hasEvent(false)
                .minOrderQuantity(1)
                .maxOrderQuantity(100)
                .build();
    }
}