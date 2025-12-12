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

## RA Job Visualization Module (Sprint 2)

This project includes a **simplified RA Job Visualization module** integrated into the SMU-Lyle Scientific Hub. The goal of this module is to provide **clear, high-level insight** into Research Assistant job postings while preserving the existing RA Job List, search, sorting, and pagination behavior.

The implementation follows the **approved Sprint 1 requirements** and is evaluated against those requirements for Sprint 2.

All visualizations use **Chart.js** and run as part of the existing Docker-based frontend and backend setup.

---

## Overview of Implemented Features

The RA Job Visualization module is embedded at the top of the RA Job List page and consists of:
- A small **summary section** with key metrics
- A simple **filter and search area**
- A set of **basic visualizations** that update dynamically based on filters

The design intentionally avoids complex interactions in favor of **clarity, readability, and fast updates**, as specified in the Sprint 1 scope.

---

## Summary Section
At the top of the dashboard, the system displays:
- **Total active RA jobs**
- **Number of departments currently hiring**
- **Average posted pay**, when salary data is available

These values update automatically as filters or search terms change. When no jobs match the active filters, a clear **“No jobs found”** message is shown.

---

## Filters and Search
Users can refine the dashboard and visualizations using:
- **Department filter**
- **Job mode/location filter** (Remote / In-person / Hybrid)
- **Status filter** (Open / Pending / Updated)
- A **search bar** for job titles, descriptions, skills, or keywords

A single **Reset Filters** button clears all filters and restores the dashboard to its default state.

---

## Visualizations

### Jobs by Department
A bar chart shows how RA job postings are distributed across departments and labs.
- One bar per department
- Bar height represents the number of RA jobs
- Updates whenever filters change
- Hover tooltips display the department name, job count, and percentage of total jobs
- Displays a clear message when no department data is available

---

### Pay Visualization
The dashboard includes a pay overview to help users understand compensation patterns.
- Bar chart and pie chart showing the distribution of RA positions by pay structure
- Updates dynamically with filters
- Hover tooltips show exact job counts
- Displays a message when no pay information is available
- Charts can be exported as image files (PNG)

This visualization is designed to provide a **quick, high-level view** of RA job pay rather than detailed financial analysis.

---

### Jobs by Location
A location chart provides a simple breakdown of how RA postings are distributed across modes and locations.
- Pie chart showing job counts by location label (including “Unknown” when missing)
- Updates automatically when filters or search input change
- Hover tooltips show job counts and percentage of total
- Displays a clear message when no location data matches the current filters
- Chart can be exported as an image file (PNG)

---

### Keyword Panel
The keyword panel provides a concise overview of commonly occurring terms in RA job postings.

- Displays the **top 5–10 keywords** based on frequency
- Keywords are extracted from job titles, descriptions, and related fields
- A **word cloud visualization** displays keywords, where:
  - Word size reflects relative frequency using a scaled representation
  - Exact frequency counts are shown on hover
- A compact **Top Keywords table** displays the most frequent terms alongside their exact counts
- The panel updates dynamically as filters or search terms change
- A clear empty-state message is shown when no keyword data is available

To improve readability and avoid visual dominance by very high-frequency terms, the word cloud uses a **log-scaled size mapping**. This ensures that less frequent but still relevant keywords remain visible while preserving accurate frequency reporting via tooltips and tabular display.


---

## Simplified Dashboard and View Modes
All visual components are presented on a **single dashboard page**, as required by the Sprint 1 scope.

To support usability on different screen sizes and use cases, users can switch between view modes:
- **Summary only** (default)
- All visualizations
- Individual visualization views (Pay, Department, Location, Keywords)

This allows users to focus on specific information without overwhelming the page.

---

## Export and Usability
- Charts can be exported as **PNG images** for use in reports or presentations
- Clear labels, legends, and hover tooltips are provided
- Accessible color schemes are used for readability
- Empty states and error messages are shown when data is unavailable

---

## Integration and Deployment
The RA Job Visualization module:
- Is fully integrated into the existing RA Job List page
- Does not break or replace existing job list functionality
- Runs using the same **Docker workflow** as prior labs with updated **Docker Compose** functionality
- Uses existing RA job data (no database schema changes were required)

---

## Summary
This Sprint 2 implementation delivers the **simplified RA Job Visualization module** described in the approved Sprint 1 requirements. The focus is on clarity, correctness, and alignment with the defined scope, providing students, faculty, and administrators with an easy-to-understand overview of RA job postings.


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


