# SMU Scientific Hub

This portal aims to provide an open platform that supports and facilitates collaborations between SMU and local industry. Industry can publish a challenge, and our platform will recommend SMU researcher(s), based on AI, Machine Learning, Deep Learning, Natural Language Processing, Knowledge Graph, and Data Mining techniques.

## Project Overview
This is a frontend-backend separated project, with each running on different ports.

- Frontend Port: `9038`
- Backend Port: `9039`

### How to Run the Project
To run the project, open both the backend and frontend projects and manage them using the sbt shell.

Steps
1. Open the project with the `build.sbt` file.

2. In the sbt shell, enter the following command:

```bash
run
```

This will start the frontend and backend on their respective ports. You can access the landing page by navigating to `http://localhost:9038`.

## Contribution Guidelines
For contributors, please follow these guidelines when making changes to the code:

- Create a new branch named after yourself.
- Make modifications in your own branch.
- Submit your changes via a pull request for review and merging.

By following this process, we ensure that all code changes are tracked and reviewed properly before being merged into the main project.


## Local Development (Docker Setup)

This repo is set up to run the CSHub backend + frontend in Docker, using a **local MySQL instance**.

### Prerequisites

- Docker Desktop installed and running
- Java is not required locally (JDK runs inside Docker)
- Local MySQL running on `localhost:3306`
  - Database: `old_cshub`
  - Schema initialized from the SQL files in `db_init/`

You should be able to:

```bash
mysql -u root -p
USE old_cshub;
SHOW TABLES;
```

---

## JDK Base Image Change

The original Dockerfiles used:

```dockerfile
FROM openjdk:8-jdk-slim
```

This tag was removed from Docker Hub.  
We now use:

```dockerfile
FROM eclipse-temurin:8-jdk
```

This is still **Java 8**, just a supported modern distribution.  
No code changes required.

---

## Required Local Config Files (`application.conf`)

Play applications require `application.conf` at runtime.  
These files are **NOT tracked in git** and are ignored by `.gitignore`:

- `backend/conf/application.conf`  
- `frontend/conf/application.conf`  

If missing, containers will crash with:

> `resource not found on classpath: application.conf`

### Fix:

Copy them from your previous working project or the zip:

```bash
old_project/backend/conf/application.conf   → backend/conf/application.conf
old_project/frontend/conf/application.conf  → frontend/conf/application.conf
```

Ensure `.gitignore` keeps them out of version control:

```gitignore
backend/conf/application.conf
frontend/conf/application.conf
```

---

## .env — Database Password

Create a `.env` file in the **project root**:

```env
MYSQL_PASSWORD=your_root_password_here
```

This must match the password used when connecting:

```bash
mysql -u root -p
```

`docker-compose.yml` injects this value into the backend:

```yaml
environment:
  DB_URL: "jdbc:mysql://host.docker.internal:3306/old_cshub?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
  DB_USER: "root"
  DB_PASS: "${MYSQL_PASSWORD}"
```

`.env` is already in `.gitignore`.

---

# Docker Commands & Workflow

All commands assume you are in the repository root:

```bash
cd path/to/Sci-Hub-SWE
```

---

## 1. Build and Run (first time or after code changes)

```bash
docker compose up --build
```

This will:

- Build backend & frontend images  
- Start containers  
  - Backend → 9037  
  - Frontend → 9036  

When running, go to:

http://localhost:9036  

Stop with **Ctrl + C**.

---

## 2. Restart Without Rebuilding

```bash
docker compose up
```

Stop with **Ctrl + C**.

---

## 3. Stop and Remove Containers

```bash
docker compose down
```

---

## 4. Useful Extras

Check running services:

```bash
docker compose ps
```

Follow logs:

```bash
docker compose logs -f backend
docker compose logs -f frontend
```

If a service fails (e.g., missing `application.conf`, DB auth issues),  
check its logs first.


