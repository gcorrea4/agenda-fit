# Agenda Fit API — Projeto Diamante

API de grade de aulas de uma academia em Spring Boot. O administrador cadastra as aulas; o aluno
se inscreve e só mexe nas próprias inscrições. Criado a partir do
[projeto base](https://github.com/joaocarloslima/agenda-fit) e compatível com o
[app](https://github.com/joaocarloslima/agenda-fit-app).

Stack: Java 25, Spring Boot 4.1, Spring Data JPA, Spring HATEOAS, Spring Security (OAuth2 Resource Server + JWT), springdoc e H2 em memória.

## Equipe

| Nome | RM |
|------|----|
| Gabriel Correa Souza | 567903 |

## Como rodar

Abra a pasta no IntelliJ como projeto Maven e execute `AgendaFitApplication`,
ou rode `mvn spring-boot:run`. A API sobe em http://localhost:8080 sem nenhuma configuração extra.

- Swagger UI: http://localhost:8080/swagger-ui.html (botão **Authorize** recebe o token do `/auth/login`)
- Exemplos de requisições: `test.http`

## Usuários do DataLoader

| E-mail | Senha | Role |
|--------|-------|------|
| admin@fit.com | admin123 | ADMIN |
| ana@fit.com | ana123 | USER |
| bruno@fit.com | bruno123 | USER |

## Rotas

| Método | Rota | Acesso | Respostas |
|--------|------|--------|-----------|
| POST | `/auth/register` | livre | 201 usuário · 400 · 409 e-mail em uso |
| POST | `/auth/login` | livre | 200 `{token, expiresIn}` · 401 |
| GET | `/classes`, `/classes/example`, `/classes/search` | livre | 200 página de aulas |
| GET | `/classes/{id}` | livre | 200 aula · 404 |
| POST | `/classes` | ADMIN | 201 aula + Location · 400 · 401 · 403 |
| PUT | `/classes/{id}` | ADMIN | 200 aula · 400 · 401 · 403 · 404 |
| DELETE | `/classes/{id}` | ADMIN | 204 · 401 · 403 · 404 |
| POST | `/classes/{id}/enrollments` | logado | 201 inscrição · 401 · 404 · 409 |
| GET | `/enrollments/me` | logado | 200 página de inscrições · 401 |
| DELETE | `/enrollments/{id}` | dono ou ADMIN | 204 · 401 · 403 · 404 |

Todos os erros (inclusive 401 e 403) saem no formato `{"error": "..."}`.

## O que foi implementado

- **Paginação**: as listagens recebem `Pageable` (`page`, `size`), com padrão de 10 por página ordenado por `startsAt` (`@PageableDefault`), e devolvem o bloco `page`.
- **Query by Example** (`GET /classes/example`): probe com `modality`, `level` e `instructor` e um `ExampleMatcher` que ignora nulos e caixa.
- **Specification** (`GET /classes/search`): uma `Specification` por filtro (`title` contém, `modality`, `level`, `startsAfter`, `startsBefore`, `availableOnly`), combinadas com `and` só quando informadas. O `availableOnly` conta as inscrições com uma subquery (`GymClassSpecifications`).
- **HATEOAS**: aulas e inscrições como `EntityModel` montados por `RepresentationModelAssembler`; páginas como `PagedModel` com `first/prev/next/last` mantendo os filtros, em `_embedded.classes` e `_embedded.enrollments`.
- **Links condicionais**, consultando o `SecurityContext` a cada requisição:
  - `enroll`: logado, com vaga e ainda não inscrito
  - `update`, `delete` e `create` (na página): ADMIN
  - `cancel`: dono da inscrição ou ADMIN
- **JWT**: `POST /auth/login` emite token HS256 (`NimbusJwtEncoder` com o `jwt.secret`) com `sub` (e-mail), `name`, `role` e `exp` de 1 hora; o Resource Server valida com um `JwtDecoder` do mesmo secret. Sessão `STATELESS` e CSRF desligado.
- **Roles e dono**: `hasRole("ADMIN")` para gerenciar aulas; aluno lista e cancela só as próprias inscrições; 409 para aula lotada ou inscrição repetida.
- **Swagger**: título "Agenda Fit API", `@Tag`, `@Operation`, `@ApiResponse` (400, 401, 403, 404, 409) e `@Parameter` nos filtros.
