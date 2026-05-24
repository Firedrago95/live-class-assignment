# 알림 발송 시스템
- 이벤트 발생 시 사용자에게 유실없이 알림을 발송하기 위한 비동기 알림 시스템입니다.
- 실제 메시지 브로커 없이 RDBMS와 인터페이스 아웃박스 패턴을 활용하여 운영환경으로 전환 가능합니다.

## 기술스택
* **Language & Framework:** Java 21, Spring Boot 4.0.6
* **Database:** PostgreSQL (`SKIP LOCKED`를 활용한 워커 동시성 제어)
* **Test:** JUnit5, Mockito, MockMvc, Testcontainers
* **Deployment:** Docker

## 요구사항 해석 및 가정
- 과제의 핵심을 분산 시스템 환경에서 발생할 수 있는 동시성과 데이터 정합성 문제를 메시지 브로커 없이 애플리케이션 및 DB 레벨에서 어떻게 제어하고 방어할 것인가로 이해했습니다. 
- 이를 위해 락(Lock) 경합 최소화와 트랜잭션 범위 분리에 가장 큰 우선순위를 두었습니다.

## 설계 결정과 이유
### 1. 알림 최초 등록
- 알림 발송 요청을 수신하고 데이터 정합성을 보장합니다.
  - **Transaction Outbox Pattern:** 비즈니스 트랜잭션과 알림 이벤트 저장을 하나의 트랜잭션으로 묶어, 시스템 장애 시에도 알림 데이터 유실을 차단했습니다.
  - **DB 유니크 제약(멱등성 보장):** (`source_event_id`, `receiver_id`, `type`)에 유니크 제약조건을 설정하여, 동일한 이벤트에 대한 중복 알림 등록을 차단했습니다.
  - **관심사 분리(CQRS):** `NotificationApplicationService`, `NotificationQueryService`를 분리하여, 읽기와 쓰기 트랜잭션을 최적화했습니다.

### 2. 비동기 처리 구조 및 재시도 정책
- 접수된 알림을 비동기로 처리하고 장애 상황에 대응합니다.
  - **FOR UPDATE SKIP LOCKED(동시성 제어):** 메시지 브로커 없이 PostgreSQL의 락 메커니즘을 활용하여, 다중 인스턴스가 동일한 이벤트를 중복처리 하지 않으면서도 성능 저하 없이 병렬 처리가 가능하도록 설계했습니다.
  - **트랜잭션 분리:** 트랜잭션을 PROCESSING 상태 변경과 결과 반영(SUCCESS/FAILED) 단위로 분리했습니다. 외부 API 호출은 트랜잭션 밖에서 수행하여 DB 커넥션 점유 시간을 최소화하고, 장애 시 시스템 전체 마비를 방지합니다.
  - **PROCESSING 좀비 이벤트 처리:** 서버 문제로 `PROCESSING` 상태에 멈춘 이벤트역시 5분 타임아웃 조건을 통해 워커가 자동으로 복구하도록 쿼리를 설계하였습니다.
  - **지수 백오프 및 지터(재시도 전략):** 일시적인 외부 장애에 대비해 지수 백오프와 지터를 적용하여 외부 시스템 부하를 방지하는 재시도 로직을 구현했습니다.
  - **스레드 풀 최적화:** 스프링 `@Scheduled`의 기본 싱글 스레드 동작 방식으로 인해 발생할 수 있는 스케줄러 간 블로킹 및 성능 병목을 예방하고자 `spring.task.scheduling.pool.size=5` 설정을 도입했습니다. 무한한 스레드 생성으로 인한 DB 커넥션 풀 고갈을 방지합니다.

### 3. 다중 기기 동시성 제어
  - **다중 기기 읽음 처리:** `Notification` 엔티티에 `@Version`을 활용한 낙관적 락을 적용하여, 사용자가 여러 기기에서 동시에 읽음 처리 요청을 보내더라도 락 대기 없이 데이터 정합성을 유지하고 예외(409 Conflict)를 튕겨내도록 구현했습니다.

## 데이터 모델 (DB 스키마 및 ERD 설명)
### 1. `notifications` (알림 데이터)
사용자에게 보여지는 알림 내역을 저장합니다.
- `id` (BIGSERIAL, PK): 알림 식별자
- `source_event_id` (VARCHAR, UK): 원본 이벤트 식별자
- `receiver_id` (VARCHAR, UK): 수신자 식별자
- `type` (VARCHAR, UK): 알림 타입 (예: PAYMENT_SUCCESS)
- `channel` (VARCHAR): 발송 채널 (EMAIL, IN_APP)
- `is_read` (BOOLEAN): 읽음 여부 (기본값 false)
- `version` (BIGINT): 다중 기기 읽음 처리를 위한 낙관적 락 버전
- `created_at` (TIMESTAMP): 생성 일시 (JPA Auditing 자동 관리)
- `updated_at` (TIMESTAMP): 수정 일시 (JPA Auditing 자동 관리)
- **복합 유니크 제약:** `(source_event_id, receiver_id, type)`를 묶어 멱등성을 보장합니다.

### 2. `outbox_events` (비동기 발송 큐)
알림 발송의 유실 방지 및 재시도를 관리하는 테이블입니다.
- `id` (BIGSERIAL, PK): 이벤트 식별자
- `aggregate_id` (BIGINT): 연관된 Notification ID
- `payload` (TEXT): 외부 발송용 메시지 페이로드
- `status` (VARCHAR): 상태 (PENDING, PROCESSING, SUCCESS, FAILED, DEAD_LETTER)
- `retry_count` (INT): 누적 재시도 횟수
- `failure_reason` (TEXT): 마지막 실패 사유 기록
- `next_retry_at` (TIMESTAMP): 다음 재시도 예정 시각
- `created_at` (TIMESTAMP): 생성 일시 (JPA Auditing 자동 관리)
- `updated_at` (TIMESTAMP): 수정 일시 (JPA Auditing 자동 관리)
- **인덱스:** 워커 폴링 성능 최적화를 위해 `(status, next_retry_at)`, `(status, updated_at)` 인덱스를 적용하여 풀 스캔을 방지합니다.

## 제약 사항 및 고도화 방안
- **실패 이력 관리**: 현재는 마지막 실패 사유만 저장합니다. 향후 별도의 `OutboxEventErrorLog` 테이블을 분리하여, 회차별 실패 이력을 모두 보존하는 구조로 확장할 수 있습니다.
- **중복 발송 방지 (At-Least-Once 보장)**: 본 시스템은 '최소 한 번 발송'을 보장합니다. 외부 API 호출 성공 후 DB 반영 단계에서 실패할 경우 중복 발송 가능성이 있습니다. 이를 위해 수신 시스템 측에 `OutboxEvent ID`를 멱등키로 제공하는 방식을 적용할 수 있습니다
- **상태 관리의 확장성**: 현재는 일시적 장애와 영구적 장애를 `FAILED` 상태로 통합 관리하고 있습니다. 향후 `DEAD_LETTER` 상태로 격리하여 영구적 실패 건에 대한 자동 재시도 루프를 차단하고 수동 조치를 분리할 수 있습니다.

## 주요 API 명세
- 상세 요청/응답은 REST Docs 문서를 참고해 주세요.
* **알림 발송/조회 (Public)**
  - `POST /api/v1/notifications`: 알림 발송 요청 (202 Accepted)
  - `GET /api/v1/users/{userId}/notifications`: 수신자 알림 목록 조회 (페이징 및 읽음/안읽음 상태 필터링 지원)
  - `PATCH /api/v1/notifications/{notificationId}/read`: 알림 읽음 처리 (낙관적 락 적용, 409 Conflict 대응)
  - `GET /api/v1/notifications/outbox/{eventId}/status`: 특정 알림 발송 이벤트의 현재 처리 상태(PENDING, SUCCESS, FAILED 등) 조회
* **운영/관리 (Admin)**
  - `GET /api/v1/admin/notifications/failed`: 실패한 알림 이벤트 목록 조회
  - `POST /api/v1/admin/notifications/retry/{eventId}`: 실패한 알림 수동 재시도

## 테스트 전략
* **통합 테스트 (동시성 증명)**: `Testcontainers`를 활용하여 실제 PostgreSQL 환경을 구축하여, 로직을 검증했습니다.
* **단위 테스트 (도메인 무결성)**: 서비스 계층과 도메인 계층은 `Mock`을 활용하여 비즈니스 로직을 검증했습니다.

## 실행 방법
```bash
# 전체 실행 (DB + 앱)
docker compose up --build -d

# 종료
docker compose down
```

## 테스트 실행 및 API 문서 (REST Docs)
- **주의:** Docker가 실행 중인 상태에서 진행해야 합니다. (TestContainers 사용)
```bash
# 테스트 실행 및 문서 생성
./gradlew test

# HTML 문서 빌드 후 확인
./gradlew asciidoctor
open build/docs/asciidoc/index.html
```

## AI 활용내역
- **아키텍처 설계 방향성 논의:** 메시지 브로커 없는 환경에서 유실을 막기 위한 'Transactional Outbox Pattern' 도입 및 아키텍처 설계의 페어 프로그래밍 파트너로 활용했습니다.
