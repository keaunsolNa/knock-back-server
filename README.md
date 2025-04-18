# ☁️ knock_back_server

> **KNOCK** - 영화 및 공연예술 개봉일 리마인드 서비스의 **비즈니스 로직 및 API 서버**

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Heroku](https://img.shields.io/badge/Deploy-Heroku-430098?logo=heroku&logoColor=white)](https://heroku.com)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-7.x-orange?logo=elasticsearch)](https://www.elastic.co/elasticsearch/)

---

## 🔍 소개

`knock_back_server`는 [KNOCK](https://github.com/keaunsolNa/Knock) 서비스의 **중앙 API 서버**로,  
유저 인증, 컨텐츠 구독, 푸시 알림, 콘텐츠 검색 등의 주요 비즈니스 로직을 처리합니다.  
`knock_crawling` 서비스에서 수집한 데이터를 기반으로, 사용자에게 유의미한 정보를 제공하고,  
PWA 클라이언트와의 통신을 담당합니다.

---

## 🛠️ 기술 스택

| 영역          | 스택 / 기술                 |
|---------------|-----------------------------|
| 언어          | Java 17                     |
| 프레임워크    | Spring Boot, Spring Security|
| 인증          | JWT (HttpOnly + Secure 쿠키)|
| 알림 시스템   | Firebase Cloud Messaging    |
| 데이터 저장   | Elasticsearch (Bonsai)      |
| 배포          | Heroku (Web Dyno)           |

---

## 🧩 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 SSO 로그인 | 카카오 / 구글 / 네이버 OAuth2 로그인 (JWT 발급) |
| 📅 구독 / 알림 | 개봉일 디데이 기준 푸시 알림 예약, 구독/취소 기능 |
| 🔎 콘텐츠 검색 | 영화 및 공연예술 Elasticsearch 기반 검색 |
| 🧠 추천 알고리즘 | 사용자 관심 기반 추천 콘텐츠 제공 |
| 📬 푸시 발송 | Firebase를 통한 디바이스 기반 푸시 발송 |

---

## 🧭 연동 구성도

```plaintext
graph TD
    A[KNOCK 사용자] --> B[프론트엔드 (Next.js)]
    B --> C[백엔드 API 서버 (Spring Boot)]
    D[크롤러 서버 (Python)] --> E[Elasticsearch]
    C --> E
```

---

## 🗂️ 디렉터리 구조

```plaintext
knock-back-server/
├── src/
│   ├── main/
│   │   ├── java/org/knock/
│   │   │   ├── controller/      # REST API Controller
│   │   │   ├── service/         # 비즈니스 로직 처리
│   │   │   ├── repository/      # Elasticsearch Repository
│   │   │   ├── config/          # Spring Security, Web 설정
│   │   └── resources/
│   │       ├── application.yml
├── build.gradle
├── Procfile                     # Heroku용 실행 설정
└── README.md
```

---

## ⚙️ 실행 방법 (로컬 개발 환경 기준)

### 1. 환경 변수 설정

Heroku에 설정된 값과 동일한 환경을 `.env` 또는 시스템 환경 변수로 등록합니다.

### 2. 실행

```bash
./gradlew bootRun
```

---

## ☁️ 배포 정보

- **플랫폼**: Heroku (Web Dyno)
- **스토리지**: Elasticsearch (Bonsai Addon)
- **외부 연동**:
  - `knock_crawling`: 콘텐츠 크롤링
  - FCM: 푸시 알림 전송
  - OAuth2: SSO 인증 (카카오, 구글, 네이버)

---

## 🔗 관련 서비스

- 🛎️ [KNOCK 메인 레포지토리](https://github.com/keaunsolNa/Knock)
- 🤖 [KNOCK 크롤링 서버 레포지토리](https://github.com/keaunsolNa/knock_crawling)
- 📄 [KNOCK 소개 페이지 (Notion)](https://www.notion.so/1d0eb6c84ddd80da9dece7e09ec68c77)

---

## 🧑‍💻 개발자

| 이름   | 역할               | GitHub |
|--------|--------------------|--------|
| 나큰솔 | 백엔드 / API 서버 개발 | [@keaunsolNa](https://github.com/keaunsolNa) |

---

## 📄 라이선스

```
MIT License

Copyright (c) 2025 keaunsolNa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
