package com.early_express.product_service.domain.product.infrastructure.persistence.repository;

import com.early_express.product_service.domain.product.domain.model.Product;
import com.early_express.product_service.domain.product.domain.model.vo.Price;
import com.early_express.product_service.domain.product.domain.model.vo.ProductStatus;
import com.early_express.product_service.domain.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("ProductRepository 통합 테스트")
class ProductRepositoryImplTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 저장 - 신규")
    void save_newProduct() {
        // given
        Product product = createTestProduct(null, "SELLER-001");

        // when
        Product saved = productRepository.save(product);

        // then
        assertThat(saved.getProductId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트 상품");
        assertThat(saved.getSellerId()).isEqualTo("SELLER-001");
    }

    @Test
    @DisplayName("상품 저장 - 업데이트 (더티 체킹)")
    void save_updateProduct() {
        // given
        Product product = createTestProduct(null, "SELLER-001");
        Product saved = productRepository.save(product);

        // when - 도메인 모델 수정
        Product updated = productRepository.findById(saved.getProductId()).get();
        updated.update("수정된 상품명", "수정된 설명", Price.of(20000));

        // 저장 (더티 체킹)
        Product result = productRepository.save(updated);

        // then
        assertThat(result.getProductId()).isEqualTo(saved.getProductId());
        assertThat(result.getName()).isEqualTo("수정된 상품명");
        assertThat(result.getDescription()).isEqualTo("수정된 설명");
        assertThat(result.getPrice().getAmount()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("ID로 상품 조회")
    void findById() {
        // given
        Product product = createTestProduct(null, "SELLER-001");
        Product saved = productRepository.save(product);

        // when
        Optional<Product> found = productRepository.findById(saved.getProductId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getProductId()).isEqualTo(saved.getProductId());
        assertThat(found.get().getName()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findById_notFound() {
        // when
        Optional<Product> found = productRepository.findById("NOT-EXIST");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("판매자별 상품 조회")
    void findBySellerId() {
        // given
        productRepository.save(createTestProduct(null, "SELLER-001"));
        productRepository.save(createTestProduct(null, "SELLER-001"));
        productRepository.save(createTestProduct(null, "SELLER-002"));

        // when
        List<Product> products = productRepository.findBySellerId("SELLER-001");

        // then
        assertThat(products).hasSize(2);
        assertThat(products).allMatch(p -> p.getSellerId().equals("SELLER-001"));
    }

    @Test
    @DisplayName("상태별 상품 조회")
    void findByStatus() {
        // given
        Product product1 = createTestProduct(null, "SELLER-001");
        product1.activate();
        productRepository.save(product1);

        Product product2 = createTestProduct(null, "SELLER-002");
        productRepository.save(product2);

        // when
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        List<Product> draftProducts = productRepository.findByStatus(ProductStatus.DRAFT);

        // then
        assertThat(activeProducts).hasSize(1);
        assertThat(activeProducts.get(0).getStatus()).isEqualTo(ProductStatus.ACTIVE);

        assertThat(draftProducts).hasSize(1);
        assertThat(draftProducts.get(0).getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    @DisplayName("상품명으로 검색")
    void findByNameContaining() {
        // given
        Product product1 = createTestProduct(null, "SELLER-001");
        productRepository.save(product1);

        Product product2 = Product.create(null, "SELLER-002", "COMPANY-001", "노트북", "설명", Price.of(1000000), 1, 10);
        productRepository.save(product2);

        // when
        List<Product> found = productRepository.findByNameContaining("테스트");

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).contains("테스트");
    }

    @Test
    @DisplayName("페이징 조회")
    void findAllWithPaging() {
        // given
        for (int i = 0; i < 15; i++) {
            productRepository.save(createTestProduct(null, "SELLER-00" + (i % 3)));
        }

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = productRepository.findAllWithPaging(pageable);

        // then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("판매자별 페이징 조회")
    void findBySellerIdWithPaging() {
        // given
        for (int i = 0; i < 15; i++) {
            productRepository.save(createTestProduct(null, "SELLER-001"));
        }
        for (int i = 0; i < 5; i++) {
            productRepository.save(createTestProduct(null, "SELLER-002"));
        }

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = productRepository.findBySellerIdWithPaging("SELLER-001", pageable);

        // then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getContent()).allMatch(p -> p.getSellerId().equals("SELLER-001"));
    }

    @Test
    @DisplayName("소프트 삭제")
    void delete() {
        // given
        Product product = createTestProduct(null, "SELLER-001");
        Product saved = productRepository.save(product);
        String productId = saved.getProductId();

        // when
        productRepository.delete(productId);

        // then
        Optional<Product> found = productRepository.findById(productId);
        assertThat(found).isEmpty(); // 소프트 삭제로 조회 안됨
    }

    @Test
    @DisplayName("삭제된 상품은 목록 조회에서 제외")
    void findAll_excludeDeleted() {
        // given
        Product product1 = createTestProduct(null, "SELLER-001");
        Product saved1 = productRepository.save(product1);

        Product product2 = createTestProduct(null, "SELLER-002");
        productRepository.save(product2);

        // when
        productRepository.delete(saved1.getProductId());
        List<Product> all = productRepository.findAll();

        // then
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSellerId()).isEqualTo("SELLER-002");
    }

    @Test
    @DisplayName("상품 존재 여부 확인")
    void existsById() {
        // given
        Product product = createTestProduct(null, "SELLER-001");
        Product saved = productRepository.save(product);

        // when
        boolean exists = productRepository.existsById(saved.getProductId());
        boolean notExists = productRepository.existsById("NOT-EXIST");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("삭제된 상품은 존재하지 않는 것으로 판단")
    void existsById_deletedProduct() {
        // given
        Product product = createTestProduct(null, "SELLER-001");
        Product saved = productRepository.save(product);

        // when
        productRepository.delete(saved.getProductId());
        boolean exists = productRepository.existsById(saved.getProductId());

        // then
        assertThat(exists).isFalse();
    }

    private Product createTestProduct(String productId, String sellerId) {
        return Product.create(
                productId,
                sellerId,
                "COMPANY-001",
                "테스트 상품",
                "테스트 설명",
                Price.of(10000),
                1,
                100
        );
    }
}