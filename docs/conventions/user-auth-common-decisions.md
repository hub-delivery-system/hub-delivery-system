# 사용자/인증 공통 합의 가이드

이 문서를 보면: 사용자/인증 구현 시 팀이 합의한 기준(패키지 구조, 상태/권한, Keycloak 책임, 로컬 설정)을 한 번에 확인할 수 있습니다.  

언제 다시 보나요: 새 API 추가할 때, 역할/승인 정책이 헷갈릴 때, Keycloak/로컬 실행 이슈가 생길 때.

---

## 한 줄 요약

`로그인 식별자는 slack_id`, `role/소속 확정은 승인 시점`, `인증/토큰은 Keycloak`, `user-service는 상태/정책 검증`.

---

## 한눈에 보기

- 구조: 도메인별 폴더 + 4계층(`presentation/application/domain/infrastructure`)
- 공통: `BaseEntity`, `ApiResponse`, `BaseException`, `UserRole` 재사용
- 로그인: `slack_id + password`
- 상태: `PENDING / APPROVED / REJECTED`
- 권한: `MASTER / HUB_MANAGER / DELIVERY_MANAGER / COMPANY_MANAGER`
- Keycloak: 토큰 발급/비밀번호 검증 담당

---

## 목차

- [1. 패키지/계층 구조 규칙](#1-패키지계층-구조-규칙)
- [2. 공통 모듈 사용 규칙](#2-공통-모듈-사용-규칙)
- [3. 사용자/인증 도메인 합의](#3-사용자인증-도메인-합의)
- [4. Keycloak 적용 합의](#4-keycloak-적용-합의)
- [5. 로컬 실행/환경 변수 규칙](#5-로컬-실행환경-변수-규칙)

---

## 1. 패키지/계층 구조 규칙

### 1-1. 기본 구조

- `presentation`: Controller, Request/Response DTO
- `application`: Service, Use-case
- `domain`: Entity, Repository 인터페이스, 도메인 예외
- `infrastructure`: 외부 연동/영속성 구현

### 1-2. 위치 기준

| 만들 것 | 위치 |
|---|---|
| Controller | `{domain}/presentation/controller` |
| Request DTO | `{domain}/presentation/dto/request` |
| Response DTO | `{domain}/presentation/dto/response` |
| Service | `{domain}/application/service` |
| Entity | `{domain}/domain/entity` |
| Repository 인터페이스 | `{domain}/domain/repository` |
| 외부 API/DB 연동 구현 | `{domain}/infrastructure/*` |
| 도메인 예외 | `{domain}/domain/exception` |

---

## 2. 공통 모듈 사용 규칙

### 2-1. 권한(UserRole)

위치: `common-module/security/UserRole`

- `MASTER`
- `HUB_MANAGER`
- `DELIVERY_MANAGER`
- `COMPANY_MANAGER`

### 2-2. 엔티티/삭제 정책

- 일반 엔티티는 `BaseEntity` 상속
- 감사 필드(`createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deletedAt`, `deletedBy`)는 엔티티에 중복 작성하지 않음
- 삭제는 물리 삭제 대신 `softDelete(...)` 사용
- MSA 규칙상 타 서비스 엔티티와 JPA 연관관계를 직접 맺지 않고 FK 값(`UUID`)만 보관

### 2-3. 예외/응답 정책

- 도메인별 `ErrorCode enum` 사용 (`{DOMAIN}-001` 형식)
- 도메인 예외는 `BaseException` 기반
- 컨트롤러 응답은 `ApiResponse` 포맷 통일

---

## 3. 사용자/인증 도메인 합의

### 3-1. 로그인/식별자

- 로그인 기준: `slack_id + password`
- `username`은 표시용(중복 허용), 로그인 식별자로 사용하지 않음
- `slack_id`는 로그인 ID이므로 유일성 기준으로 관리

### 3-2. 사용자 상태/권한

- 상태 enum: `PENDING`, `APPROVED`, `REJECTED`
- 회원가입 직후 상태: `PENDING`
- `role`은 회원가입 시점이 아니라 승인 시점에 확정

### 3-3. 소속 정보

- 가입 요청: `affiliation_type`, `affiliation_name`
- 승인 시: `hub_id/company_id`, 최종 권한 확정

---

## 4. Keycloak 적용 합의

### 4-1. 책임 분리

- Keycloak 담당: 인증, 비밀번호 검증, 토큰 발급
- user-service 담당: 상태/삭제 여부 검증, 도메인 정책 검증

### 4-2. 매핑 규칙

- Keycloak `username` = 우리 서비스 `slack_id`

---

## 5. 로컬 실행/환경 변수 규칙

- 민감정보(`KEYCLOAK_CLIENT_SECRET`)는 코드/설정 기본값에 하드코딩하지 않음
- `.env.example`는 커밋, 실제 `.env`는 커밋하지 않음
- `application.yml`은 `${ENV_NAME}` 참조 방식 사용

용어 주의:
- `.env.example`의 DB URL/계정은 개인 PC 고정값이 아니라 "팀 공통 로컬 기본 예시값"입니다.
- 개인 환경이 다르면 `.env`에서 각자 값 변경해서 사용합니다.
