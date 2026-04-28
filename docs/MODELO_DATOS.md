# Modelo de Datos — Sistema de Inventario, Ventas y Caja

Documento de contexto para asistir el desarrollo del sistema. Describe el esquema completo de base de datos, las relaciones entre entidades, los flujos operativos y las reglas de negocio que deben aplicarse a nivel de aplicación.

---

## 1. Visión general

Sistema para un negocio pequeño con tres módulos principales:

- **Inventario:** gestión de productos, compras a proveedores y stock.
- **Ventas:** registro de ventas a clientes con descuento de stock.
- **Caja:** control de apertura/cierre de caja del vendedor con arqueo.

El sistema implementa **dos relaciones polimórficas** clave:
- `MOVIMIENTO_INVENTARIO` puede provenir de un `COMPRA_DETALLE`, `VENTA_DETALLE` o `REVERSO`.
- `MOVIMIENTO_CAJA` puede provenir de una `VENTA` o un `REVERSO`.

**Principio importante:** las relaciones polimórficas y todas las foreign keys NO existen a nivel de base de datos. La integridad referencial se valida exclusivamente a nivel de código de aplicación.

---

## 2. Esquema de base de datos

### 2.1 Catálogo

#### `PRODUCTO`
Entidad central del catálogo.

```sql
CREATE TABLE PRODUCTO (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    presentacion_valor DECIMAL(10,2) NULL,
    presentacion_unidad VARCHAR(10) NULL,
    activo BOOLEAN NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NULL,
    creado_por BIGINT NOT NULL,
    actualizado_por BIGINT NULL,

    CONSTRAINT uq_producto_nombre_presentacion
        UNIQUE (nombre, presentacion_valor, presentacion_unidad)
);
```

- `presentacion_valor` + `presentacion_unidad`: ej. `500` + `g`, o `1` + `L`.
- `activo`: soft delete. Productos inactivos no se pueden vender ni comprar.
- Constraint único evita duplicados de la misma presentación del mismo producto.

#### `CODIGO_BARRA`
Un producto puede tener múltiples códigos de barras (1:N). Útil para presentaciones distintas o códigos secundarios.

```sql
CREATE TABLE CODIGO_BARRA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    codigo VARCHAR(255) NOT NULL UNIQUE,
    tipo VARCHAR(100) NULL,
    principal BOOLEAN NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NULL,
    creado_por BIGINT NOT NULL,
    actualizado_por BIGINT NULL
);
```

- `principal`: marca el código por defecto del producto.
- `codigo` es único globalmente.

#### `STOCK_ACTUAL`
Snapshot del stock y costo promedio actual por producto. Relación 1:1 con `PRODUCTO` (la PK es `producto_id`).

```sql
CREATE TABLE STOCK_ACTUAL (
    producto_id BIGINT PRIMARY KEY,
    stock DECIMAL(14,2) NOT NULL,
    costo_promedio DECIMAL(14,2) NOT NULL,
    fecha_actualizacion DATETIME NOT NULL
);
```

- Existe como tabla separada para no actualizar `PRODUCTO` con cada movimiento.
- `costo_promedio` se recalcula con cada entrada de inventario (ver sección 4.1).
- **Regla:** todo movimiento de inventario debe actualizar esta tabla en la misma transacción.

---

### 2.2 Compras

#### `COMPRA`
Cabecera de la compra a proveedor.

```sql
CREATE TABLE COMPRA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    proveedor VARCHAR(255) NULL,
    total DECIMAL(14,2) NULL,
    observacion VARCHAR(500) NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NULL,
    creado_por BIGINT NOT NULL,
    actualizado_por BIGINT NULL
);
```

- `total`: se guarda explícito por performance e inmutabilidad histórica (no se calcula en consulta).
- `proveedor` es texto libre — no hay tabla de proveedores en este alcance.

#### `COMPRA_DETALLE`
Líneas de la compra (1:N con `COMPRA`).

```sql
CREATE TABLE COMPRA_DETALLE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    compra_id BIGINT NOT NULL,
    cantidad DECIMAL(14,2) NOT NULL,
    costo_unitario DECIMAL(14,2) NOT NULL,
    subtotal DECIMAL(14,2) NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME NULL,
    creado_por BIGINT NOT NULL,
    actualizado_por BIGINT NULL
);
```

- `costo_unitario`: lo que se pagó al proveedor por unidad.
- `subtotal` = `cantidad * costo_unitario` (se guarda por performance).

---

### 2.3 Ventas

#### `VENTA`
Cabecera de la venta al cliente.

```sql
CREATE TABLE VENTA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total DECIMAL(14,2) NULL,
    fecha_creacion DATETIME NOT NULL,
    creado_por BIGINT NOT NULL
);
```

- `total`: guardado explícito.
- `creado_por`: el vendedor que registró la venta.

#### `VENTA_DETALLE`
Líneas de la venta (1:N con `VENTA`).

```sql
CREATE TABLE VENTA_DETALLE (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    venta_id BIGINT NOT NULL,
    cantidad DECIMAL(14,2) NOT NULL,
    precio_unitario DECIMAL(14,2) NOT NULL,
    subtotal DECIMAL(14,2) NULL
);
```

- `precio_unitario`: lo que **paga el cliente** (no es el costo del producto).
- El costo del producto en una venta vive en `MOVIMIENTO_INVENTARIO.costo_unitario` (que se obtiene del costo promedio al momento de la venta).
- **Regla de inmutabilidad:** una vez creado un `VENTA_DETALLE`, no se modifica. Solo se reversa.

---

### 2.4 Inventario (polimórfico)

#### `MOVIMIENTO_INVENTARIO`
Registro inmutable de cada movimiento de stock. Tabla central del inventario.

```sql
CREATE TABLE MOVIMIENTO_INVENTARIO (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    referencia_id BIGINT NOT NULL,
    referencia_tipo VARCHAR(20) NOT NULL,
    movimiento_origen_id BIGINT NULL,
    cantidad DECIMAL(14,2) NOT NULL,
    costo_unitario DECIMAL(14,2) NOT NULL,
    costo_total DECIMAL(14,2) NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    creado_por BIGINT NOT NULL,

    INDEX idx_mov_referencia (referencia_tipo, referencia_id),
    INDEX idx_mov_producto_fecha (producto_id, fecha_creacion),
    INDEX idx_mov_origen (movimiento_origen_id)
);
```

**Campos clave:**

- `tipo`: `'ENTRADA'` o `'SALIDA'`.
- `referencia_tipo`: indica el origen del movimiento. Valores válidos:
  - `'COMPRA_DETALLE'` → entrada por compra.
  - `'VENTA_DETALLE'` → salida por venta.
  - `'REVERSO'` → entrada o salida por reverso.
  - `'AJUSTE'` → ajuste manual de inventario (futuro).
- `referencia_id`: id del registro al que apunta (según el tipo).
- `movimiento_origen_id`: para reversos, apunta al `MOVIMIENTO_INVENTARIO` que se está reversando. Permite reconstruir la cadena.
- `costo_unitario`: costo del producto al momento del movimiento.
  - En entradas (compra): costo de compra real.
  - En salidas (venta): costo promedio del momento.
- `costo_total` = `cantidad * costo_unitario`.

**Reglas de uso:**
- Cada `COMPRA_DETALLE` genera **un** `MOVIMIENTO_INVENTARIO` de tipo `ENTRADA`.
- Cada `VENTA_DETALLE` genera **un** `MOVIMIENTO_INVENTARIO` de tipo `SALIDA`.
- Cada `REVERSO` genera **un** `MOVIMIENTO_INVENTARIO` con tipo opuesto al del movimiento original.

#### `REVERSO`
Anula parcial o totalmente un `MOVIMIENTO_INVENTARIO`.

```sql
CREATE TABLE REVERSO (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movimiento_id BIGINT NOT NULL,
    cantidad_reversada DECIMAL(14,2) NOT NULL,
    observacion VARCHAR(500) NULL,
    fecha_creacion DATETIME NOT NULL,
    creado_por BIGINT NOT NULL
);
```

- `movimiento_id`: apunta al `MOVIMIENTO_INVENTARIO` que se reversa.
- `cantidad_reversada`: puede ser parcial (≤ cantidad del movimiento original).
- **No tiene `costo_unitario`** porque se obtiene por JOIN al movimiento original.
- **No tiene `precio_unitario`** porque se deriva del `VENTA_DETALLE` (vía el movimiento original) en caso de reverso de venta.

---

### 2.5 Caja (polimórfico)

#### `CAJA`
Apertura/cierre de caja del vendedor. Solo puede haber una caja `ABIERTA` por usuario a la vez.

```sql
CREATE TABLE CAJA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto_inicial DECIMAL(14,2) NOT NULL,
    total_ingresos DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_egresos DECIMAL(14,2) NOT NULL DEFAULT 0,
    saldo_esperado DECIMAL(14,2) NULL,
    monto_cierre_real DECIMAL(14,2) NULL,
    diferencia DECIMAL(14,2) NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_apertura DATETIME NOT NULL,
    fecha_cierre DATETIME NULL,
    creado_por BIGINT NOT NULL
);
```

- `estado`: `'ABIERTA'` o `'CERRADA'`.
- `creado_por`: el vendedor (mismo usuario que abre y cierra).
- `monto_inicial`: efectivo con que se abre la caja.
- `monto_cierre_real`: efectivo contado al cierre (lo ingresa el vendedor).
- `saldo_esperado`: lo que debería haber según el sistema (calculado al cerrar).
- `diferencia` = `monto_cierre_real - saldo_esperado` (positivo = sobrante, negativo = faltante).
- `total_ingresos` y `total_egresos`: se actualizan con cada `MOVIMIENTO_CAJA` o se calculan al cerrar.

#### `MOVIMIENTO_CAJA`
Cada entrada o salida de dinero asociada a una caja.

```sql
CREATE TABLE MOVIMIENTO_CAJA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    caja_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    monto DECIMAL(14,2) NOT NULL,
    referencia_tipo VARCHAR(20) NOT NULL,
    referencia_id BIGINT NULL,
    observacion VARCHAR(500) NULL,
    fecha_creacion DATETIME NOT NULL,
    creado_por BIGINT NOT NULL,

    INDEX idx_movcaja_caja (caja_id, fecha_creacion),
    INDEX idx_movcaja_referencia (referencia_tipo, referencia_id)
);
```

**Campos clave:**

- `tipo`: `'INGRESO'` o `'EGRESO'`.
- `metodo_pago`: `'EFECTIVO'`, `'TARJETA'`, `'TRANSFERENCIA'`, etc.
  - **Solo los movimientos en `EFECTIVO` afectan el arqueo físico de caja.**
- `referencia_tipo`: `'VENTA'` o `'REVERSO'`.
- `referencia_id`: id de la venta o reverso correspondiente.

**Importante:** las compras NO generan `MOVIMIENTO_CAJA`. La compra a proveedor es responsabilidad de un usuario administrativo distinto y no toca el efectivo del vendedor.

---

## 3. Relaciones del modelo

### Relaciones con FK directa (lógicas, no existen en BD)

| Origen | Destino | Cardinalidad | Vía |
|---|---|---|---|
| `CODIGO_BARRA` | `PRODUCTO` | N:1 | `producto_id` |
| `STOCK_ACTUAL` | `PRODUCTO` | 1:1 | `producto_id` (PK) |
| `COMPRA_DETALLE` | `COMPRA` | N:1 | `compra_id` |
| `COMPRA_DETALLE` | `PRODUCTO` | N:1 | `producto_id` |
| `VENTA_DETALLE` | `VENTA` | N:1 | `venta_id` |
| `VENTA_DETALLE` | `PRODUCTO` | N:1 | `producto_id` |
| `MOVIMIENTO_INVENTARIO` | `PRODUCTO` | N:1 | `producto_id` |
| `MOVIMIENTO_INVENTARIO` | `MOVIMIENTO_INVENTARIO` | N:1 | `movimiento_origen_id` (auto-referencia) |
| `REVERSO` | `MOVIMIENTO_INVENTARIO` | N:1 | `movimiento_id` |
| `MOVIMIENTO_CAJA` | `CAJA` | N:1 | `caja_id` |

### Relaciones polimórficas

| Origen | Destino | Vía |
|---|---|---|
| `MOVIMIENTO_INVENTARIO` | `COMPRA_DETALLE` / `VENTA_DETALLE` / `REVERSO` | `referencia_tipo` + `referencia_id` |
| `MOVIMIENTO_CAJA` | `VENTA` / `REVERSO` | `referencia_tipo` + `referencia_id` |

---

## 4. Flujos operativos

### 4.1 Compra de mercadería

**Trigger:** usuario administrativo registra una compra a proveedor.

**Pasos (en una sola transacción):**
1. Insertar `COMPRA` con `total` y observaciones.
2. Por cada producto comprado:
   - Insertar `COMPRA_DETALLE` con `cantidad`, `costo_unitario`, `subtotal`.
   - Insertar `MOVIMIENTO_INVENTARIO`:
     - `tipo = 'ENTRADA'`
     - `referencia_tipo = 'COMPRA_DETALLE'`
     - `referencia_id = compra_detalle.id`
     - `producto_id`, `cantidad`, `costo_unitario` (igual al de compra)
     - `costo_total = cantidad * costo_unitario`
   - Actualizar `STOCK_ACTUAL`:
     - **Recalcular costo promedio ponderado:**
       ```
       nuevo_costo_promedio = (stock_actual * costo_promedio + cantidad * costo_unitario)
                              / (stock_actual + cantidad)
       ```
     - Sumar `cantidad` al `stock`.

**No genera `MOVIMIENTO_CAJA`.** La compra se paga por otro medio (transferencia, crédito) gestionado fuera del módulo de caja.

### 4.2 Venta al cliente

**Precondición:** debe existir una `CAJA` con `estado='ABIERTA'` para el usuario vendedor.

**Pasos (en una sola transacción):**
1. Validar caja abierta del usuario.
2. Validar stock disponible para todos los productos.
3. Insertar `VENTA` con `total` y `creado_por`.
4. Por cada producto vendido:
   - Insertar `VENTA_DETALLE` con `cantidad`, `precio_unitario`, `subtotal`.
   - Insertar `MOVIMIENTO_INVENTARIO`:
     - `tipo = 'SALIDA'`
     - `referencia_tipo = 'VENTA_DETALLE'`
     - `referencia_id = venta_detalle.id`
     - `producto_id`, `cantidad`
     - `costo_unitario = STOCK_ACTUAL.costo_promedio` del momento
     - `costo_total = cantidad * costo_unitario`
   - Actualizar `STOCK_ACTUAL`: restar `cantidad` del `stock` (el costo promedio NO cambia en salidas).
5. Insertar `MOVIMIENTO_CAJA`:
   - `caja_id` de la caja abierta del usuario.
   - `tipo = 'INGRESO'`
   - `metodo_pago` según cómo pagó el cliente.
   - `monto = venta.total`
   - `referencia_tipo = 'VENTA'`
   - `referencia_id = venta.id`

### 4.3 Reverso de compra (devolución a proveedor)

**Pasos (en una sola transacción):**
1. Validar que `cantidad_reversada` ≤ cantidad disponible (cantidad original − reversos previos).
2. Insertar `REVERSO`:
   - `movimiento_id` = id del `MOVIMIENTO_INVENTARIO` original (entrada por compra).
   - `cantidad_reversada`
3. Insertar nuevo `MOVIMIENTO_INVENTARIO`:
   - `tipo = 'SALIDA'` (opuesto al original)
   - `referencia_tipo = 'REVERSO'`
   - `referencia_id = reverso.id`
   - `movimiento_origen_id` = id del movimiento original
   - `producto_id`, `cantidad = cantidad_reversada`
   - `costo_unitario` = costo del movimiento original
4. Actualizar `STOCK_ACTUAL`: restar `cantidad_reversada` del `stock`.

**No genera `MOVIMIENTO_CAJA`.**

### 4.4 Reverso de venta (devolución del cliente)

**Precondición:** debe existir una `CAJA` con `estado='ABIERTA'` para el usuario.

**Pasos (en una sola transacción):**
1. Validar caja abierta.
2. Validar que `cantidad_reversada` ≤ cantidad disponible.
3. Insertar `REVERSO`:
   - `movimiento_id` = id del `MOVIMIENTO_INVENTARIO` original (salida por venta).
   - `cantidad_reversada`
4. Insertar nuevo `MOVIMIENTO_INVENTARIO`:
   - `tipo = 'ENTRADA'` (opuesto al original)
   - `referencia_tipo = 'REVERSO'`
   - `referencia_id = reverso.id`
   - `movimiento_origen_id` = id del movimiento original
   - `producto_id`, `cantidad = cantidad_reversada`
   - `costo_unitario` = costo del movimiento original
5. Actualizar `STOCK_ACTUAL`: sumar `cantidad_reversada`. **El costo promedio NO se recalcula** (la mercadería retorna al costo con que salió).
6. Calcular monto a devolver:
   - Obtener `precio_unitario` desde `VENTA_DETALLE` (vía JOIN al movimiento original).
   - `monto_devolucion = cantidad_reversada * precio_unitario`
7. Insertar `MOVIMIENTO_CAJA`:
   - `caja_id` de la caja abierta.
   - `tipo = 'EGRESO'`
   - `metodo_pago` según cómo se devuelve (normalmente igual al método de pago original).
   - `monto = monto_devolucion`
   - `referencia_tipo = 'REVERSO'`
   - `referencia_id = reverso.id`

**Devolución parcial por línea:** como cada `VENTA_DETALLE` tiene su propio `MOVIMIENTO_INVENTARIO`, se puede reversar solo una línea de la venta sin afectar las demás.

### 4.5 Apertura y cierre de caja

**Apertura:**
1. Verificar que el usuario no tenga otra caja `ABIERTA`.
2. Insertar `CAJA`:
   - `monto_inicial` = efectivo con que abre.
   - `estado = 'ABIERTA'`
   - `fecha_apertura = NOW()`
   - `creado_por` = usuario.

**Cierre:**
1. Calcular `saldo_esperado` (solo efectivo):
   ```sql
   saldo_esperado = monto_inicial 
                  + SUM(monto WHERE tipo='INGRESO' AND metodo_pago='EFECTIVO')
                  - SUM(monto WHERE tipo='EGRESO' AND metodo_pago='EFECTIVO')
   ```
2. Calcular `total_ingresos` y `total_egresos` (todos los métodos de pago).
3. Calcular `diferencia = monto_cierre_real - saldo_esperado`.
4. Actualizar `CAJA`:
   - `estado = 'CERRADA'`
   - `fecha_cierre = NOW()`
   - `monto_cierre_real`, `saldo_esperado`, `diferencia`, `total_ingresos`, `total_egresos`.

---

## 5. Reglas de negocio críticas

### Inmutabilidad
- `VENTA_DETALLE` y `COMPRA_DETALLE` no se modifican después de creados. Solo se reversan.
- `MOVIMIENTO_INVENTARIO` es inmutable. Las correcciones se hacen creando un nuevo movimiento (reverso + nuevo).

### Validaciones de reverso
- No se puede reversar más cantidad que la del movimiento original menos los reversos previos:
  ```
  SUM(REVERSO.cantidad_reversada WHERE movimiento_id = X) ≤ MOVIMIENTO_INVENTARIO.cantidad
  ```
- Reverso de venta requiere caja abierta del usuario.
- Reverso de compra NO requiere caja abierta.

### Caja
- Un usuario solo puede tener UNA caja `ABIERTA` a la vez.
- Para registrar una venta o reverso de venta, el usuario debe tener su caja abierta.
- Solo los movimientos en `EFECTIVO` afectan el arqueo físico.

### Stock
- Antes de registrar una venta, validar que `STOCK_ACTUAL.stock ≥ cantidad solicitada`.
- `STOCK_ACTUAL` siempre debe estar sincronizado con la suma de movimientos. Toda operación que afecte stock debe actualizar esta tabla en la misma transacción.

### Costo promedio ponderado
- Solo se recalcula en **entradas por compra**.
- Se mantiene en salidas (ventas, reverso de compra) y en entradas por reverso de venta.
- Fórmula:
  ```
  nuevo_costo = (stock_anterior * costo_anterior + cantidad_entrada * costo_entrada)
              / (stock_anterior + cantidad_entrada)
  ```

### Transaccionalidad
Todas las operaciones que afectan múltiples tablas deben ejecutarse dentro de una transacción de base de datos:
- Compra: `COMPRA` + `COMPRA_DETALLE[]` + `MOVIMIENTO_INVENTARIO[]` + `STOCK_ACTUAL[]`.
- Venta: `VENTA` + `VENTA_DETALLE[]` + `MOVIMIENTO_INVENTARIO[]` + `STOCK_ACTUAL[]` + `MOVIMIENTO_CAJA`.
- Reverso de venta: `REVERSO` + `MOVIMIENTO_INVENTARIO` + `STOCK_ACTUAL` + `MOVIMIENTO_CAJA`.
- Reverso de compra: `REVERSO` + `MOVIMIENTO_INVENTARIO` + `STOCK_ACTUAL`.

---

## 6. Consultas frecuentes

### Kardex de un producto
```sql
SELECT 
    m.fecha_creacion,
    m.tipo,
    m.cantidad,
    m.costo_unitario,
    m.costo_total,
    m.referencia_tipo,
    m.referencia_id
FROM MOVIMIENTO_INVENTARIO m
WHERE m.producto_id = ?
ORDER BY m.fecha_creacion DESC;
```

### Movimientos de una venta específica
```sql
SELECT m.*
FROM MOVIMIENTO_INVENTARIO m
JOIN VENTA_DETALLE vd ON vd.id = m.referencia_id
WHERE m.referencia_tipo = 'VENTA_DETALLE'
  AND vd.venta_id = ?;
```

### Margen de una venta
```sql
SELECT 
    v.id,
    v.total AS ingreso,
    SUM(m.costo_total) AS costo,
    v.total - SUM(m.costo_total) AS margen
FROM VENTA v
JOIN VENTA_DETALLE vd ON vd.venta_id = v.id
JOIN MOVIMIENTO_INVENTARIO m ON m.referencia_id = vd.id 
                              AND m.referencia_tipo = 'VENTA_DETALLE'
WHERE v.id = ?
GROUP BY v.id, v.total;
```

### Arqueo de caja (efectivo)
```sql
SELECT 
    c.monto_inicial,
    SUM(CASE WHEN mc.tipo='INGRESO' AND mc.metodo_pago='EFECTIVO' 
             THEN mc.monto ELSE 0 END) AS ingresos_efectivo,
    SUM(CASE WHEN mc.tipo='EGRESO' AND mc.metodo_pago='EFECTIVO' 
             THEN mc.monto ELSE 0 END) AS egresos_efectivo,
    c.monto_inicial 
    + SUM(CASE WHEN mc.tipo='INGRESO' AND mc.metodo_pago='EFECTIVO' 
               THEN mc.monto ELSE 0 END)
    - SUM(CASE WHEN mc.tipo='EGRESO' AND mc.metodo_pago='EFECTIVO' 
               THEN mc.monto ELSE 0 END) AS saldo_esperado
FROM CAJA c
LEFT JOIN MOVIMIENTO_CAJA mc ON mc.caja_id = c.id
WHERE c.id = ?
GROUP BY c.id, c.monto_inicial;
```

### Reversos de un movimiento
```sql
SELECT * 
FROM REVERSO
WHERE movimiento_id = ?;
```

### Cadena completa de un movimiento (movimiento + reverso)
```sql
SELECT m.*, m_origen.referencia_tipo AS tipo_origen, m_origen.referencia_id AS id_origen
FROM MOVIMIENTO_INVENTARIO m
LEFT JOIN MOVIMIENTO_INVENTARIO m_origen ON m_origen.id = m.movimiento_origen_id
WHERE m.id = ?;
```

---

## 7. Convenciones de nombres

- **Tablas:** SINGULAR, MAYÚSCULAS con `_` (ej: `MOVIMIENTO_INVENTARIO`).
- **Campos:** snake_case minúsculas (ej: `fecha_creacion`).
- **Foreign keys lógicas:** `<entidad>_id` (ej: `producto_id`, `venta_id`).
- **Fechas:** `fecha_creacion`, `fecha_actualizacion` en todas las tablas operativas.
- **Auditoría:** `creado_por`, `actualizado_por` con id del usuario.

---

## 8. Lo que NO está modelado (fuera de alcance)

- Usuarios y roles (se asume que existen y `creado_por` referencia su id).
- Proveedores como entidad (se guarda como string en `COMPRA.proveedor`).
- Clientes como entidad (las ventas son anónimas).
- Categorías de productos.
- Múltiples sucursales o bodegas.
- Impuestos / IVA.
- Descuentos en ventas.
- Múltiples métodos de pago en una misma venta (pagos mixtos).
