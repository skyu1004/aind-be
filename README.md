# aind-be — Todo Portal Backend

Spring Boot 4.1 · Java 17 · Maven · Spring Data JPA · H2 (in-memory). 프론트엔드: [aind-fe](https://github.com/skyu1004/aind-fe)

## 실행

```bash
./mvnw spring-boot:run     # http://localhost:8080
```

- 헬스체크: http://localhost:8080/actuator/health
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:tododb`, user `sa`)

## API

| Method | Path | Body | 응답 |
|--------|------|------|------|
| GET | `/api/todos` | – | 200, 최신순 목록 |
| POST | `/api/todos` | `{ "title": "..." }` | 201 |
| PUT | `/api/todos/{id}` | `{ "title": "...", "completed": true }` | 200 |
| DELETE | `/api/todos/{id}` | – | 204 |

제목은 앞뒤 공백 제거 후 1–200자. 모든 에러는 RFC 9457 ProblemDetail(`{status, title, detail}`)로 반환됩니다.

## 테스트

```bash
./mvnw verify    # 단위 + @WebMvcTest + @DataJpaTest + 통합 테스트, JaCoCo 라인 80% 게이트
```
