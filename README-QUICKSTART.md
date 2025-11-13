# Maplewood Scheduler — Quickstart

This is a minimal full‑stack app (Spring Boot + SQLite backend, React + TS frontend), containerized with Docker, runnable via a single `run.sh` script.

## Prereqs
- Docker (w/ Compose v2)
- Internet to pull base images and Maven/NPM packages

## Start
```bash
./run.sh
```
Then open:
- Backend API: http://localhost:8080/api/semesters
- Frontend UI: http://localhost:5173

## Stop
```bash
./stop.sh
```

> The SQLite DB is bundled under `backend/src/main/resources/maplewood_school.sqlite`. The container copies it to `/app/data`. You can replace it with your own DB by rebuilding the image or volume‑mounting to `/app/data/maplewood_school.sqlite`.
