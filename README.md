# Tienda de libros en línea (DSY2205)

Actividad sumativa de la **Experiencia 1** (*Programando nuestro BackEnd*): backend **sin interfaz gráfica** (solo APIs REST), orientado al caso de **gestión de pedidos de una tienda en línea**. Los productos de venta son **libros** (catálogo, stock y precio).

## Caso de negocio 

- **Productos:** mínimo cinco; los datos iniciales incluyen **seis** libros en database/schema.sql.
- **Roles:** la API usa **ADMIN** y **CLIENTE** (JWT). En `rol` también existe **SOPORTE**, pero el flujo principal es ADMIN/CLIENTE.
- **Pago:** sin pasarela; los pedidos quedan en **`PAGADO_SIMULADO`**.
- **Microservicios:** el enunciado pide **dos de tres** bloques (login; catálogo/compra; gestión de productos). Aquí: **`ms-usuarios`** (usuarios + login) y **`ms-libros`** (catálogo, pedidos y CRUD admin).



## Microservicios

| Servicio      | Puerto por defecto | Descripción |
|---------------|--------------------|-------------|
| `ms-usuarios` | 8081               | Login JWT, CRUD de usuarios (roles ADMIN/CLIENTE) |
| `ms-libros`   | 8082               | Catálogo de libros (GET público), CRUD admin, pedidos con pago simulado |

## Requisitos de entorno

- JDK 17+
- Maven 3.9+
- Oracle Database (usuario/esquema con permisos DDL según tu entorno). Por defecto se asume **Oracle Database Free** en local (PDB `FREEPDB1`). **Oracle XE** suele usar el PDB `XEPDB1`; en ese caso define `ORACLE_URL` con `/XEPDB1`.

## Base de datos

1. Crea un usuario de esquema (ej. `TIENDA_LIBROS`) **en el PDB correcto** (`FREEPDB1` en Database Free, `XEPDB1` en muchas instalaciones XE) o usa uno existente.
2. Ejecuta el script database/schema.sql en SQL Developer o SQL\*Plus.
3. Usuarios de prueba (contraseña **`password`** para todos):

   - `admin@tienda.cl` — rol **ADMIN**
   - `cliente1@mail.cl`, `cliente2@mail.cl` — rol **CLIENTE**

## Variables de entorno (ambos microservicios)

Ajusta según tu instancia Oracle:

```bash
export ORACLE_URL="jdbc:oracle:thin:@localhost:1521/FREEPDB1"
export ORACLE_USER="TIENDA_LIBROS"
export ORACLE_PASSWORD=""
export JWT_SECRET=""
```

**Importante:** `JWT_SECRET` debe ser **idéntico** en `ms-usuarios` y `ms-libros` para que el token emitido en login sea válido en el servicio de libros.

Puertos opcionales:

```bash
export SERVER_PORT=8081   # solo al arrancar ms-usuarios
```

## Arranque

Terminal 1 — usuarios:

```bash
cd ms-usuarios
mvn spring-boot:run
```

Terminal 2 — libros:

```bash
cd ms-libros
mvn spring-boot:run
```



## Flujo Postman (resumen)

1. `POST http://localhost:8081/api/auth/login` con `{"email":"admin@tienda.cl","password":"password"}` → copiar `token`.
2. En `ms-libros`, usar cabecera `Authorization: Bearer <token>`.
3. `GET http://localhost:8082/api/libros` — catálogo (sin token).
4. `POST http://localhost:8082/api/libros` — crear libro (solo ADMIN, con token).
5. `POST http://localhost:8082/api/pedidos` — crear pedido (CLIENTE o ADMIN, con token); estado `PAGADO_SIMULADO`.

Colección de ejemplo: [`postman/TiendaLibros.postman_collection.json`](postman/TiendaLibros.postman_collection.json).

