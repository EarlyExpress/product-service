# Product Service

> Early Express 플랫폼의 상품 관리를 담당하는 마이크로서비스

## 📋 개요

Product Service는 상품의 등록, 수정, 삭제, 상태 관리 및 재고 연동을 담당합니다.
DDD(Domain-Driven Design) 아키텍처를 기반으로 설계되었으며, Inventory Service와의 이벤트 기반 통신을 통해 재고 상태를 동기화합니다.

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.5.7, Spring Cloud 2025.0.0 |
| **Language** | Java 21 |
| **Database** | PostgreSQL + pgvector |
| **ORM** | Spring Data JPA, QueryDSL 5.1.0 |
| **Message Queue** | Apache Kafka (Spring Cloud Stream) |
| **Service Discovery** | Netflix Eureka Client |
| **Config** | Spring Cloud Config |
| **Security** | Spring Security, OAuth 2.0 (Keycloak) |
| **Service Communication** | OpenFeign (User Service 연동) |
| **Observability** | Micrometer, Zipkin, Loki, Prometheus |

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Product Service                          │
├─────────────────────────────────────────────────────────────────┤
│  Presentation Layer                                             │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ AllProduct      │ │ ProducerProduct │ │ InternalProduct │   │
│  │ Controller      │ │ Controller      │ │ Controller      │   │
│  │ (공개 조회)      │ │ (생산업체 전용)  │ │ (서비스 간 통신) │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  ProductService (비즈니스 로직 조율)                        │  │
│  └───────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer                                                   │
│  ┌─────────────────┐  ┌─────────────────────────────────────┐  │
│  │ Product (AR)    │  │ Value Objects                       │  │
│  │ - ProductStatus │  │ - Price                             │  │
│  │ - 상품 로직      │  │                                     │  │
│  └─────────────────┘  └─────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │ JPA Entity   │  │ Repository   │  │ UserServiceClient  │    │
│  │ ProductEntity│  │ Impl         │  │ (Feign)            │    │
│  └──────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## 📦 도메인 모델

### Product (Aggregate Root)

상품의 전체 생명주기를 관리하는 핵심 도메인 모델입니다.

```
Product
├── productId (식별자)
├── sellerId (판매자 ID)
├── companyId (업체 ID)
├── name (상품명)
├── description (상품 설명)
├── Price (가격 VO)
│   └── amount (금액)
├── ProductStatus (상태)
├── isSellable (판매 가능 여부)
├── hasEvent (이벤트 적용 여부)
├── minOrderQuantity (최소 주문 수량)
├── maxOrderQuantity (최대 주문 수량)
└── Audit Fields
    ├── createdAt / createdBy
    ├── updatedAt / updatedBy
    └── deletedAt / deletedBy / isDeleted
```

### 상품 상태 흐름 (ProductStatus)

```
                    ┌──────────────────┐
                    │      DRAFT       │ 임시 저장
                    └────────┬─────────┘
                             │ activate()
                             ▼
                    ┌──────────────────┐
              ┌─────│      ACTIVE      │─────┐
              │     │   (판매 중)       │     │
              │     └────────┬─────────┘     │
              │              │               │
     suspend()│              │               │InventoryLowStockEvent
              │              │               │ (from Inventory Service)
              ▼              │               ▼
    ┌─────────────┐          │     ┌─────────────────┐
    │  SUSPENDED  │          │     │  OUT_OF_STOCK   │
    │ (판매 중지)  │──────────┤     │    (품절)        │
    └─────────────┘          │     └────────┬────────┘
              │   activate() │              │
              └──────────────┤              │InventoryRestockedEvent
                             │              │ (from Inventory Service)
                             ▼              │
                    ┌──────────────────┐    │
                    │   DISCONTINUED   │◀───┘ (품절 해제 시 ACTIVE로)
                    │     (단종)        │
                    └──────────────────┘
```

## 🔌 API 엔드포인트

### Public API (모든 사용자)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/product/web/all/products` | 상품 목록 조회 (페이징) |
| `GET` | `/v1/product/web/all/products/{productId}` | 상품 상세 조회 |
| `GET` | `/v1/product/web/all/products/search` | 상품 검색 (키워드) |

### Producer API (생산업체 전용)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/v1/product/web/producer/products` | 상품 등록 |
| `PUT` | `/v1/product/web/producer/products/{productId}` | 상품 수정 |
| `DELETE` | `/v1/product/web/producer/products/{productId}` | 상품 삭제 (단종) |
| `GET` | `/v1/product/web/producer/products` | 내 상품 목록 조회 |
| `PUT` | `/v1/product/web/producer/products/{productId}/activate` | 상품 활성화 |
| `PUT` | `/v1/product/web/producer/products/{productId}/suspend` | 상품 일시중지 |

### Internal API (서비스 간 통신)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/product/internal/products/{productId}/validate` | 상품 존재 확인 |
| `GET` | `/v1/product/internal/products/{productId}` | 상품 정보 조회 |
| `POST` | `/v1/product/internal/products/validate-bulk` | 대량 상품 검증 |
| `GET` | `/v1/product/internal/sellers/{sellerId}/products` | 판매자별 상품 목록 |

### 상품 등록 요청 예시

```json
POST /v1/product/web/producer/products
X-User-Id: seller-uuid

{
  "hubId": "hub-uuid",
  "companyId": "company-uuid",
  "name": "프리미엄 노트북",
  "description": "고성능 비즈니스 노트북",
  "price": 1500000,
  "minOrderQuantity": 1,
  "maxOrderQuantity": 10
}
```

### 응답 예시

```json
{
  "productId": "product-uuid",
  "sellerId": "seller-uuid",
  "name": "프리미엄 노트북",
  "description": "고성능 비즈니스 노트북",
  "price": 1500000,
  "status": "DRAFT",
  "isSellable": false,
  "hasEvent": false,
  "minOrderQuantity": 1,
  "maxOrderQuantity": 10,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": null
}
```

### 대량 상품 검증 요청/응답

```json
POST /v1/product/internal/products/validate-bulk
{
  "productIds": ["product-1", "product-2", "product-3"]
}
```

```json
{
  "allValid": false,
  "validProductIds": ["product-1", "product-2"],
  "invalidProductIds": ["product-3"],
  "errors": {
    "product-3": "상품을 찾을 수 없습니다."
  }
}
```

## ⚙️ 환경 설정

### 필수 환경 변수

```bash
# Application
APP_PORT=4012
APP_NAME=product-service
APP_PROFILE=dev

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=default_db
DB_USERNAME=postgres
DB_PASSWORD=postgres123!

# Eureka
EUREKA_DEFAULT_ZONE=https://www.pinjun.xyz/eureka1/eureka/,https://www.pinjun.xyz/eureka2/eureka/
EUREKA_INSTANCE_HOSTNAME=192.168.0.42

# Config Server
CONFIG_SERVER_URL=https://www.pinjun.xyz/config

# Kafka
KAFKA_BOOTSTRAP_SERVERS=61.254.69.188:9092,61.254.69.188:9093,61.254.69.188:9094
KAFKA_CONSUMER_GROUP_ID=product-service-group

# Keycloak (OAuth 2.0)
KEYCLOAK_ISSUER_URI=https://www.pinjun.xyz/keycloak/realms/codefactory
KEYCLOAK_CLIENT_ID=user
KEYCLOAK_CLIENT_SECRET=user-password

# User Service (Feign)
USER_SERVICE_URL=http://user-service:8081

# Observability
ZIPKIN_ENABLED=true
ZIPKIN_BASE_URL=https://www.pinjun.xyz/zipkin
LOKI_ENABLED=true
LOKI_URL=https://www.pinjun.xyz/loki/api/v1/push
PROMETHEUS_PUSHGATEWAY_ENABLED=true
PROMETHEUS_PUSHGATEWAY_URL=https://www.pinjun.xyz/prometheus/pushgateway
```

## 🚀 실행 방법

### 로컬 개발 환경

```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 2. Gradle 빌드
./gradlew clean build

# 3. 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 직접 실행
java -jar build/libs/product-service-0.0.1-SNAPSHOT.jar
```

### Docker 실행

```bash
docker build -t product-service .
docker run -p 4012:4012 --env-file .env product-service
```

## 📨 Kafka 이벤트

Product Service는 **토픽 분리 패턴**을 사용하여 이벤트를 발행/수신합니다.

### 이벤트 흐름도

```
┌─────────────────┐                              ┌───────────────────┐
│ Product Service │                              │ Inventory Service │
└────────┬────────┘                              └─────────┬─────────┘
         │                                                 │
         │  ┌─────────────────────────────────────────┐   │
         │  │       Topic: product-created            │   │
         │  └─────────────────────────────────────────┘   │
         │                      │                         │
         │ ProductCreatedEvent  │                         │
         │──────────────────────┼────────────────────────▶│
         │                      │                         │ 재고 초기화
         │                      │                         │
         │  ┌─────────────────────────────────────────┐   │
         │  │       Topic: product-deleted            │   │
         │  └─────────────────────────────────────────┘   │
         │                      │                         │
         │ ProductDeletedEvent  │                         │
         │──────────────────────┼────────────────────────▶│
         │                      │                         │ 재고 비활성화
         │                      │                         │
         │  ┌─────────────────────────────────────────┐   │
         │  │       Topic: inventory-low-stock        │   │
         │  └─────────────────────────────────────────┘   │
         │                      │                         │
         │◀─────────────────────┼─────────────────────────│
         │InventoryLowStockEvent│                         │
         │ (품절 처리)           │                         │
         │                      │                         │
         │  ┌─────────────────────────────────────────┐   │
         │  │       Topic: inventory-restocked        │   │
         │  └─────────────────────────────────────────┘   │
         │                      │                         │
         │◀─────────────────────┼─────────────────────────│
         │InventoryRestockedEvt │                         │
         │ (품절 해제)           │                         │
```

### 발행 이벤트 (Publisher)

| Topic | Event | 설명 | 발행 시점 |
|-------|-------|------|----------|
| `product-created` | `ProductCreatedEvent` | 상품 생성 | 상품 등록 시 |
| `product-updated` | `ProductUpdatedEvent` | 상품 수정 | 상품 정보 변경 시 |
| `product-deleted` | `ProductDeletedEvent` | 상품 삭제(단종) | 상품 단종 시 |
| `product-status-changed` | `ProductStatusChangedEvent` | 상태 변경 | 상품 상태 전이 시 |

```json
// ProductCreatedEvent 예시
{
  "eventId": "uuid",
  "eventType": "PRODUCT_CREATED",
  "source": "product-service",
  "productId": "product-uuid",
  "sellerId": "seller-uuid",
  "hubId": "hub-uuid",
  "name": "프리미엄 노트북",
  "createdAt": "2025-01-15T10:30:00"
}
```

```json
// ProductDeletedEvent 예시
{
  "eventId": "uuid",
  "eventType": "PRODUCT_DELETED",
  "source": "product-service",
  "productId": "product-uuid",
  "sellerId": "seller-uuid",
  "deletedAt": "2025-01-15T15:00:00"
}
```

```json
// ProductStatusChangedEvent 예시
{
  "eventId": "uuid",
  "eventType": "PRODUCT_STATUS_CHANGED",
  "source": "product-service",
  "productId": "product-uuid",
  "oldStatus": "DRAFT",
  "newStatus": "ACTIVE",
  "changedAt": "2025-01-15T11:00:00"
}
```

### 수신 이벤트 (Consumer)

| Topic | Event | 설명 | 처리 |
|-------|-------|------|------|
| `inventory-low-stock` | `InventoryLowStockEvent` | 재고 부족 알림 | `ProductService.markAsOutOfStock()` |
| `inventory-restocked` | `InventoryRestockedEvent` | 재입고 알림 | `ProductService.restoreFromOutOfStock()` |

```json
// InventoryLowStockEvent 예시
{
  "eventId": "uuid",
  "eventType": "INVENTORY_LOW_STOCK",
  "source": "inventory-service",
  "inventoryId": "inventory-uuid",
  "productId": "product-uuid",
  "hubId": "hub-uuid",
  "currentQuantity": 5,
  "safetyStock": 10,
  "detectedAt": "2025-01-15T14:00:00"
}
```

```json
// InventoryRestockedEvent 예시
{
  "eventId": "uuid",
  "eventType": "INVENTORY_RESTOCKED",
  "source": "inventory-service",
  "inventoryId": "inventory-uuid",
  "productId": "product-uuid",
  "hubId": "hub-uuid",
  "restockedQuantity": 100,
  "currentQuantity": 105,
  "restockedAt": "2025-01-15T16:00:00"
}
```

### Kafka 설정

```yaml
spring:
  kafka:
    topic:
      # 발행 토픽
      product-created: product-created
      product-updated: product-updated
      product-deleted: product-deleted
      product-status-changed: product-status-changed
      # 수신 토픽
      inventory-low-stock: inventory-low-stock
      inventory-restocked: inventory-restocked
    consumer:
      group-id: product-service-group
      enable-auto-commit: false  # 수동 ACK
```

## 🔗 서비스 간 통신

### User Service (Feign Client)

```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    UserInfoResponse getUserInfo(@PathVariable String userId);
}
```

```json
// UserInfoResponse
{
  "userId": "user-uuid",
  "username": "seller01",
  "hubId": "hub-uuid",
  "companyId": "company-uuid",
  "role": "SELLER"
}
```

## 🔐 보안

- OAuth 2.0 Resource Server (Keycloak 연동)
- `X-User-Id` 헤더를 통한 사용자 식별
- Producer API는 판매자 권한 검증
- Internal API는 서비스 간 통신 전용 (Gateway 미노출)

## 📈 모니터링

| 도구 | 용도 | 엔드포인트 |
|------|------|-----------|
| **Actuator** | 헬스체크/메트릭 | `/actuator/health`, `/actuator/prometheus` |
| **Zipkin** | 분산 추적 | Push to Zipkin Server |
| **Loki** | 로그 수집 | Push via Logback Appender |
| **Prometheus** | 메트릭 수집 | Push to Pushgateway |

## 📁 프로젝트 구조

```
src/main/java/com/early_express/product_service/
├── domain/product/
│   ├── application/
│   │   └── service/
│   │       └── ProductService.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Product.java (Aggregate Root)
│   │   │   └── vo/
│   │   │       ├── Price.java
│   │   │       └── ProductStatus.java
│   │   ├── exception/
│   │   ├── messaging/
│   │   │   ├── ProductEventPublisher.java (Interface)
│   │   │   └── dto/
│   │   │       ├── ProductCreatedEventData.java
│   │   │       ├── ProductUpdatedEventData.java
│   │   │       ├── ProductDeletedEventData.java
│   │   │       └── ProductStatusChangedEventData.java
│   │   └── repository/
│   ├── infrastructure/
│   │   ├── client/user/
│   │   │   ├── UserServiceClient.java
│   │   │   └── dto/
│   │   │       └── UserInfoResponse.java
│   │   ├── messaging/
│   │   │   ├── inventory/
│   │   │   │   ├── consumer/
│   │   │   │   │   └── InventoryEventConsumer.java
│   │   │   │   └── event/
│   │   │   │       ├── InventoryLowStockEvent.java
│   │   │   │       └── InventoryRestockedEvent.java
│   │   │   └── product/
│   │   │       ├── producer/
│   │   │       │   └── KafkaProductEventPublisher.java
│   │   │       └── event/
│   │   │           ├── ProductCreatedEvent.java
│   │   │           ├── ProductUpdatedEvent.java
│   │   │           ├── ProductDeletedEvent.java
│   │   │           └── ProductStatusChangedEvent.java
│   │   └── persistence/
│   │       └── entity/
│   │           └── ProductEntity.java
│   └── presentation/
│       ├── web/
│       │   ├── AllProductController.java
│       │   ├── ProducerProductController.java
│       │   └── dto/
│       │       ├── request/
│       │       │   ├── CreateProductRequest.java
│       │       │   └── UpdateProductRequest.java
│       │       └── response/
│       │           └── ProductResponse.java
│       └── internal/
│           ├── InternalProductController.java
│           └── dto/
│               ├── request/
│               │   └── ValidateProductsRequest.java
│               └── response/
│                   ├── InternalProductResponse.java
│                   └── ProductValidationResponse.java
└── global/
    ├── common/
    ├── config/
    ├── infrastructure/
    │   └── event/base/
    │       └── BaseEvent.java
    └── presentation/
        └── dto/
            └── PageResponse.java
```