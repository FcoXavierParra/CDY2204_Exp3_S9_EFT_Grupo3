# Plataforma de Cursos en Línea — CDY2204 (Evaluación Final Transversal)

Solución **Cloud Native** que integra todo lo trabajado en el curso: microservicios Spring Boot, IDaaS (Azure AD B2C) para **frontend y backend**, colas **RabbitMQ**, almacenamiento **AWS S3**, base de datos **Oracle Cloud**, **API Manager** (AWS API Gateway) y despliegue con **CI/CD + Trivy**.

## Caso
Plataforma donde **estudiantes** se inscriben a cursos y acceden a su material, e **instructores** gestionan cursos. Las inscripciones se procesan de forma **asíncrona mediante colas**.

## Arquitectura (microservicios)

```
Frontend (HTML+JS+MSAL, login Azure B2C)
        │  Authorization: Bearer <JWT B2C>
        ▼
bff-service (8080)  ── Backend for Frontend; orquesta las colas
   ├─ POST /api/bff/inscripciones/publicar   → PRODUCTOR → inscripciones.queue
   ├─ POST /api/bff/inscripciones/consumir   → CONSUMIDOR (pull) → llama al core
   ├─ GET/POST /api/bff/cursos               → proxy al core
   └─ GET /api/bff/matriculas                → proxy al core
        │  RestClient (propaga el JWT)                 │
        ▼                                              ▼
cursos-service (8081) ── dominio Curso/Matrícula · Oracle Cloud · AWS S3
                                              RabbitMQ (Docker): inscripciones.queue + DLQ
```

- **Productor y consumidor** en Java (Spring AMQP) dentro del BFF; RabbitMQ en Docker.
- **2 endpoints de cola**: `publicar` (produce) y `consumir` (consume) — requisito EFT.
- El **consumidor** persiste la matrícula en **Oracle Cloud** (tabla `MATRICULA`) vía el core.
- **S3**: material de los cursos (`materiales/{codigo}/...`).
- **Roles** (claim `extension_rol`): `INSTRUCTOR` (gestiona cursos) y `ESTUDIANTE` (se inscribe, ve material).

## Servicios

| Servicio | Puerto | Rol |
|---|---|---|
| `bff-service` | 8080 | API para el frontend + orquesta colas RabbitMQ. Sirve el frontend estático. |
| `cursos-service` | 8081 | Dominio (cursos/matrículas), Oracle Cloud, S3. |
| `rabbitmq` | 5672 / 15672 | Broker de colas (consola web en 15672). |

## Ejecución local

```bash
cp .env.example .env               # completa Azure, Oracle, AWS
# coloca el wallet de Oracle descomprimido en ./wallet
docker compose up --build
```
- Frontend: `http://localhost:8080/`
- Swagger BFF: `http://localhost:8080/swagger-ui.html` · core: `http://localhost:8081/swagger-ui.html`
- Consola RabbitMQ: `http://localhost:15672` (guest/guest)

## Endpoints principales

| Método | Ruta (BFF) | Rol | Descripción |
|---|---|---|---|
| GET | `/api/bff/cursos` | ESTUDIANTE/INSTRUCTOR | Listar cursos |
| POST | `/api/bff/cursos` | INSTRUCTOR | Crear curso |
| POST | `/api/bff/inscripciones/publicar` | ESTUDIANTE | **Produce** un mensaje en la cola |
| POST | `/api/bff/inscripciones/consumir` | ESTUDIANTE/INSTRUCTOR | **Consume** de la cola → persiste en Oracle |
| GET | `/api/bff/matriculas` | ESTUDIANTE/INSTRUCTOR | Matrículas persistidas (Oracle) |

Core (`cursos-service`): `/api/cursos`, `/api/cursos/{id}/material` (S3), `/api/matriculas`.

## IDaaS (Azure AD B2C)
Reutiliza el tenant de las experiencias previas. Crear/usar 2 usuarios con `extension_rol` = `instructor` y `estudiante`. Registrar el **origen del frontend** como **Redirect URI SPA** en la app.

## CI/CD
`.github/workflows/deploy.yml`: Build & Test → **Trivy** → Build & Push (2 imágenes) → Deploy en EC2 (rabbitmq + cursos + bff + wallet). Secrets: DockerHub, Oracle (`ORACLE_WALLET_B64`, `ORACLE_TNS_ALIAS`, `ORACLE_USER`, `ORACLE_PASSWORD`), Azure, AWS, EC2; variable `DEPLOY_EC2=true`.

> ⚠️ El wallet, `.env` y credenciales NUNCA se versionan (ver `.gitignore`).
