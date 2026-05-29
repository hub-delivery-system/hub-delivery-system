# 서비스 구성 및 실행 방법

## 서비스 목록

| 서비스 | 포트    | 설명 |
|--------|-------|------|
| eureka-server | 19090 | 서비스 디스커버리 (Eureka) |
| gateway-service | 19010 | API 게이트웨이, JWT 검증 및 라우팅 |
| user-service | 19020 | 사용자 가입/승인, Keycloak 프로비저닝 |
| hub-service | 19030 | 허브 및 허브 간 이동 정보 관리 |
| company-product-service | 19040 | 업체 및 상품/재고 관리 |
| order-service | 19050 | 주문 생성/취소, 재고 연동, Kafka 이벤트 발행 |
| delivery-service | 19060 | 배송 생성/경로 배정/상태 관리, Kafka 이벤트 수신 |
| slack-ai-service | 19070 | Slack 알림, AI 기반 배송 경로 최적화 |

### 공통 인프라

| 서비스 | 포트 | 설명 |
|--------|------|------|
| PostgreSQL | 5432 | 주 데이터베이스 |
| Redis | 6379 | 캐시 |
| Kafka | 9092 | 메시지 브로커 |
| Zookeeper | 2181 | Kafka 코디네이션 |
| Keycloak | 8080 | 인증 서버 |
| Zipkin | 9411 | 분산 추적 |

---

## 로컬 실행 방법

### 사전 요구사항

- Docker Desktop 설치
- Java 17
- Gradle

### 1. 환경 변수 설정

프로젝트 루트의 `.env.example`을 복사하여 `.env` 파일을 생성하고 값을 채웁니다.

```bash
cp .env.example .env
```

### 2. 전체 스택 실행 (Docker Compose)

```bash
docker-compose up --build
```

서비스 시작 순서는 `docker-compose.yml`의 `depends_on` 및 `healthcheck`에 의해 자동으로 제어됩니다.

```
Layer 1: PostgreSQL, Redis, Zipkin, Zookeeper, Kafka
Layer 2: Keycloak
Layer 3: eureka-server
Layer 4: gateway-service
Layer 5: 비즈니스 서비스 (user, hub, company-product, order, delivery, slack-ai)
```

### 3. 특정 서비스만 로컬 실행

인프라만 Docker로 띄우고 특정 서비스를 IDE에서 실행하려면:

```bash
# 인프라만 실행
docker-compose up postgres redis kafka zookeeper keycloak zipkin eureka-server

# IDE에서 원하는 서비스 실행 (application.yml의 로컬 프로필 사용)
```

### 4. 서비스 접근

| URL | 설명 |
|-----|------|
| `http://localhost:19010` | API Gateway 진입점 |
| `http://localhost:19090` | Eureka Dashboard |
| `http://localhost:8080` | Keycloak Admin Console |
| `http://localhost:9411` | Zipkin UI |
