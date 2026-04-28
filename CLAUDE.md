# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Test
./mvnw test
./mvnw test -Dtest=ClassName          # single test class
```

## Architecture

Spring Boot 2.4.4 microservice (Java 8, Maven) running on port `9092` with context path `/api`. Two business domains:

- **usuario** — user authentication (entity: `USUARIOS`)
- **catalogo** — product catalog with two sub-entities (`PRODUCTOS`, `CODIGO_BARRAS`)

Each domain follows a strict layered structure: `controller → service (interface + impl) → repository → model`, with MapStruct `mapper` classes converting between JPA entities and DTOs from the shared `co.com.menor.comun_dto` library.

## Profiles & Databases

| Profile | DB | Config file |
|---|---|---|
| `local` (default) | MySQL on `localhost:3306`, database `minor` | `application-local.yml` |
| `produccion` | PostgreSQL on `localhost:5432` | `application-produccion.yml` |

Active profile is injected at build time via the Maven property `@activatedProperties@`.

## Key Dependencies

- **Lombok** — used heavily for `@Data`, `@Builder`, `@RequiredArgsConstructor`; ensure annotation processing is enabled in your IDE
- **MapStruct 1.5.5** — mappers are auto-generated; both `mapstruct-processor` and `lombok` annotation processors are declared in the compiler plugin to avoid ordering issues
- **Spring Data JPA** — repositories extend `JpaRepository`; custom finders use Spring Data query derivation or `@Query`

## API Endpoints

| Controller | Base | Operations |
|---|---|---|
| `UsuarioController` | `/usuario` | `POST /guardar`, `GET /usuarios`, `GET /existe-usuario/{u}`, `GET /consulta-usuario/{u}`, `PUT /actualizar` |
| `ProductoController` | `/producto` | `POST /guardar`, `GET /productos`, `POST /consulta-por-nombre`, `POST /existe-producto`, `PUT /actualizar` |
| `CodigoBarraController` | `/codigo-barras` | CRUD for barcodes linked to products |
