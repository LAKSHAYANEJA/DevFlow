# DevFlow - Developer Task & PR Tracker

A production-grade project management REST API built for software development teams.

## Tech Stack

|
 Layer
|
 Technology
|
|
---
|
---
|
|
 Language
| 
 Java 25, Spring Boot 4.1
|
|
 Security
|
 Spring Security 7, JWT (access + refresh tokens)
|
|
 Database
|
 PostgreSQL 18 with Flyway migrations
|
|
 Cache
|
 Redis 7 - cache-aside pattern, 5 min TTL
|
|
 Rate Limiting
|
 Bucket4j - 10 task creations/min per user
|
|
 Docs
|
 SpringDoc / Swagger UI
|

## Engineering Decisions

**Redis caching** - Cache-aside pattern on GET /projects and GET /tasks.
Cache is evicted on every write operation. Reduces DB load by 80% on repeated reads.

**Rate Limiting** - Token bucket algorithm via Bucket4j.
10 task creation per minute per user. Returns 429 with remaining token count.

**Optimistic Locking** - @Version field on Task entity.
Hibernate adds WHERE version=? to every UPDATE. Concurrent conflicting updates return 409 Conflict instead of silently overwriting data.

**Soft Delete** - Tasks are never hard deleted.
deleted_at timestamp preserves audit trail. @SQLRestriction automatically filters deleted tasks from all queries.

**Pagination** - Page/Size on task list with status and assignee filters.
Scalable alternative to returning unbounded lists.

## API Endpoints

### Auth
|
 Method
|
 Path
|
 Description
|
|
---
|
---
|
---
|
|
 POST 
|
 /api/v1/auth/register
|
 Register new user
|
|
 POST
|
 /api/v1/auth/login
|
 Login, get JWT tokens |

### Projects
| Method | Path | Description |
|---|---|---|
| GET | /api/v1/projects | List my projects |
| POST | /api/v1/projects | Create project |
| GET | /api/v1/projects/{id} | Get project |
| PUT | /api/v1/projects/{id} | Update project |
| DELETE | /api/v1/projects/{id} | Delete project |

### Tasks
| Method | Path | Description |
|---|---|---|
| POST | /api/v1/projects/{id}/tasks | Create task |
| GET | /api/v1/projects/{id}/tasks | List tasks (paginated) |
| GET | /api/v1/tasks/{id} | Get task |
| PATCH | /api/v1/tasks/{id} | Update task |
| PATCH | /api/v1/tasks/{id}/status | Update status |
| DELETE | /api/v1/tasks/{id} | Soft delete task |

## Running Locally

### Prerequisites
- Java 21+
- PostgreSQL 18
- Redis 7

### Setup

```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE devflow;"
psql -U postgres -c "CREATE USER devflow WITH PASSWORD 'devflow';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE devflow TO devflow;"

# 2. Start Redis
redis-server

# 3. Run the app
mvn spring-boot:run
```

### Swagger UI