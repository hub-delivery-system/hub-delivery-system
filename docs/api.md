# API 문서

## Swagger UI

각 서비스는 SpringDoc OpenAPI를 통해 Swagger UI를 제공합니다.
모든 요청은 API Gateway(`http://localhost:19010`)를 통해 라우팅됩니다.

| 서비스 | Swagger UI |
|--------|-----------|
| gateway-service | `http://localhost:19010/swagger-ui.html` |
| delivery-service | `http://localhost:19010/delivery-service/swagger-ui.html` |
| slack-ai-service | `http://localhost:19010/slack-ai-service/swagger-ui.html` |

> 개별 서비스 포트로 직접 접근도 가능합니다 (로컬 개발 시).

---

## 주요 API 목록

### user-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/users/signup` | 사용자 가입 신청 |
| POST | `/api/users/login` | 로그인 (Keycloak 토큰 발급) |
| PATCH | `/api/users/{userId}/approve` | 사용자 승인 (MASTER) |

### hub-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/hubs` | 허브 등록 |
| GET | `/api/hubs` | 허브 목록 조회 |
| GET | `/api/hubs/{hubId}` | 허브 단건 조회 |
| PUT | `/api/hubs/{hubId}` | 허브 수정 |
| DELETE | `/api/hubs/{hubId}` | 허브 삭제 |
| POST | `/api/hub-routes` | 허브 간 이동 정보 등록 |
| GET | `/api/hub-routes` | 허브 간 이동 정보 조회 |

### company-product-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/companies` | 업체 등록 |
| GET | `/api/companies/{companyId}` | 업체 조회 |
| POST | `/api/products` | 상품 등록 |
| GET | `/api/products/{productId}` | 상품 조회 |
| PATCH | `/api/products/{productId}/stock` | 재고 조정 |

### order-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/orders` | 주문 생성 |
| GET | `/api/orders/{orderId}` | 주문 조회 |
| DELETE | `/api/orders/{orderId}` | 주문 취소 |

### delivery-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/deliveries/{deliveryId}` | 배송 조회 |
| PATCH | `/api/deliveries/{deliveryId}/status` | 배송 상태 변경 |
| GET | `/api/delivery-managers` | 배송 담당자 목록 조회 |

### slack-ai-service

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/slack/messages` | Slack 메시지 발송 |
| POST | `/api/ai/route-optimize` | AI 경로 최적화 요청 |
