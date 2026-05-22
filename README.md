# 알림 발송 시스템
- 이벤트 발생 시 사용자에게 유실없이 알림을 발송하기 위한 비동기 알림 시스템입니다.
- 실제 메시지 브로커 없이 RDBMS와 인터페이스 아웃박스 패턴을 활용하여 운영환경으로 전환 가능합니다.

## 기술스택
* **Language & Framework:** Java 21, Spring Boot 4.0.6
* **Database:** PostgreSQL (`SKIP LOCKED`를 활용한 워커 동시성 제어)
* **Test:** JUnit5, Mockito, MockMvc, Testcontainers, Jacoco
* **Deployment:** Docker

## 아키텍쳐 및 구현 전략 
* **트랜잭션 아웃박스 패턴:** 비지니스 로직과 알림 이벤트 발행을 단일 트랜잭션으로 묶어 유실을 막습니다.
* **폴링 워커:** `@Scheduled`를 활용한 비동기 발송 처리, kafka 전환가능한 인터페이스 분리 구조
