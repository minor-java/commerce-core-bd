# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
mvn clean package

# Run locally (default profile)
mvn spring-boot:run

# Run with explicit profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName
```

The server starts on **port 9092** with context path `/api`.

## Architecture Overview

Spring Boot 2.4.4 / Java 8 microservice. No migration tool — schema is managed via Hibernate `ddl-auto: update`. Two profiles: `local` (MySQL) and `produccion` (PostgreSQL), configured in `application-local.yml` and `application-produccion.yml`.

DTOs (request/response objects) live in an external Maven dependency: **`comun-dto:0.0.1-SNAPSHOT`**. If a DTO class is missing from this repo, it is defined there.

### Domain Modules

Each module under `co.com.menor.commerce_core_bd` follows the same layered structure: `model` → `repository` → `service` (interface + impl) → `mapper` → `controller`.

| Module | Responsibility |
|---|---|
| `usuario` | User accounts and login |
| `catalogo` | Products (`Producto`), bar codes (`CodigoBarra`), price history (`PrecioHistorico`) |
| `compra` | Purchase orders (`Compra`, `CompraDetalle`) |
| `venta` | Sales transactions (`Venta`, `VentaDetalle`) |
| `movimiento` | Inventory ledger (`MovimientoInventario`), current stock (`StockActual`), reversals (`Reverso`) |
| `caja` | Cash register sessions (`Caja`, `MovimientoCaja`) |
| `shared` | `MinorExcepcion` + `GlobalExceptionHandler` (`@RestControllerAdvice`) |

### Key Cross-Cutting Patterns

**Transactional workflows** — Service impls coordinate multiple repositories inside a single `@Transactional` method. For example, `VentaServiceImpl.crearVenta` creates the sale header, line items, inventory movements, and cash movements atomically. `CompraServiceImpl` does the same for purchases.

**Inventory costing** — `StockActual` stores a weighted average cost (`costoPromedio`). Every sale/purchase updates this record and appends an immutable row to `MovimientoInventario`. Reversals create a compensating movement linked via `movimientoOrigenId`.

**Dynamic filtering** — `ProductoSpecification`, `CompraSpecification`, and `MovimientoInventarioSpecification` implement `Specification<T>` (JPA Criteria API) for paginated search endpoints that accept arbitrary filter combinations.

**Mappers** — Plain `@Component` classes (not MapStruct despite the dependency). Located in each module's `mapper` package. Two directions per entity: request → entity and entity → response DTO.

**Error handling** — Throw `MinorExcepcion(code, message)` from any service. `GlobalExceptionHandler` catches it and returns a structured error response; unhandled exceptions return a generic 500.
