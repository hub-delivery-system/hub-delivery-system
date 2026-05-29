# 기술 스택

## Backend

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 17 | 언어 |
| Spring Boot | 3.3.5 | 애플리케이션 프레임워크 |
| Spring Data JPA | - | ORM |
| Spring Security | - | 인증/인가 |
| Spring Cloud Gateway | 2023.0.3 | API 게이트웨이 |
| Spring Cloud Netflix Eureka | 2023.0.3 | 서비스 디스커버리 |
| Spring Cloud OpenFeign | 2023.0.3 | 동기 서비스 간 HTTP 호출 |
| Resilience4j | - | 서킷 브레이커 |
| QueryDSL | 5.x | 동적 쿼리 |

## 데이터베이스 / 캐시

| 기술 | 버전 | 용도 |
|------|------|------|
| PostgreSQL | 15 | 주 데이터베이스 |
| Redis | 7 | 캐시, 조회 성능 최적화 |

## 메시징

| 기술 | 버전 | 용도 |
|------|------|------|
| Apache Kafka | 7.5.0 (Confluent) | 비동기 이벤트 스트리밍 |
| Zookeeper | 7.5.0 (Confluent) | Kafka 클러스터 코디네이션 |

## 인증 / 보안

| 기술 | 용도 |
|------|------|
| Keycloak | OAuth2/OIDC 인증 서버, JWT 발급 |
| Spring OAuth2 Resource Server | JWT 토큰 검증 (JWKS) |

## 관측성

| 기술 | 용도 |
|------|------|
| Zipkin | 분산 추적 |
| Micrometer + Brave | 트레이싱 브릿지 |
| Micrometer Prometheus | 메트릭 수집 |
| Spring Actuator | 헬스 체크, 메트릭 노출 |

## 외부 연동

| 기술 | 용도 |
|------|------|
| Slack API | 배송 알림 메시지 발송 |
| AI API | 경로 최적화 및 배송 제안 |
| Naver Directions API | 허브 간 경로 거리/시간 계산 |

## API 문서

| 기술 | 용도 |
|------|------|
| SpringDoc OpenAPI (Swagger UI) | REST API 명세 자동 생성 |

## 빌드 / 인프라

| 기술 | 용도 |
|------|------|
| Gradle (Multi-module) | 빌드 도구 |
| Docker | 컨테이너 |
| Docker Compose | 로컬 환경 전체 스택 실행 |
