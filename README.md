# Maplewood Scheduler

A comprehensive school scheduling system built with Spring Boot, React, and SQLite, designed to automatically generate master schedules while respecting complex academic constraints.

## Quick Start

### Prerequisites
- Docker (with Compose v2)
- Internet connection for pulling base images and packages

### Running the Application
```bash
./run.sh
```

Then access:
- **Frontend UI**: http://localhost:5173
- **Backend API**: http://localhost:8080

### Stopping the Application
```bash
./stop.sh
```

## Technical Architecture

### Overview
The Maplewood Scheduler is a full-stack application designed to solve complex school scheduling problems. It combines automated schedule generation with an intuitive interface for both administrators and students.

**Architecture Stack:**
- **Backend**: Spring Boot 3.3.5 with Java 21
- **Database**: SQLite with JDBC
- **Frontend**: React 18 with TypeScript and Vite
- **Containerization**: Docker with multi-stage builds
- **Build Tools**: Maven (backend), npm (frontend)

### Backend Design

#### Core Services
1. **MasterScheduleService** (`backend/src/main/java/com/maplewood/scheduler/service/MasterScheduleService.java`)
   - Implements the core scheduling algorithm
   - Handles constraint satisfaction (time slots, teacher availability, room capacity)
   - Manages conflict resolution and optimization

2. **StudentService** (`backend/src/main/java/com/maplewood/scheduler/service/StudentService.java`)
   - Manages student enrollment and course progress
   - Handles prerequisite validation
   - Tracks academic progress and requirements

3. **ScheduleService** (`backend/src/main/java/com/maplewood/scheduler/service/ScheduleService.java`)
   - Manages individual student schedules
   - Handles schedule viewing and modifications

#### API Controllers
- **MetaController**: Provides metadata endpoints (semesters, courses, etc.)
- **MasterScheduleController**: Handles schedule generation and management
- **StudentController**: Manages student operations and enrollment
- **ScheduleController**: Handles schedule viewing and modifications

#### Scheduling Algorithm Approach

The system implements a constraint-based scheduling algorithm with the following key features:

**Time Constraints:**
- 7 available time slots: 9:00, 10:00, 11:00, 13:00, 14:00, 15:00, 16:00
- Lunch break: 12:00-13:00 (no classes scheduled)
- Maximum 2 consecutive hours per subject
- 60-minute session duration

**Resource Management:**
- Teacher availability tracking
- Classroom capacity and assignment
- Course prerequisite enforcement
- Conflict detection and resolution

**Optimization Strategy:**
- Greedy assignment with backtracking
- Priority-based course scheduling
- Load balancing across time slots
- Minimization of schedule conflicts

### Frontend Design

#### Component Architecture
1. **App.tsx**: Main application container with routing logic
2. **ScheduleGrid**: Visual schedule display with time-slot grid
3. **StudentPlanner**: Course enrollment and planning interface
4. **StudentPlanning**: Academic progress tracking
5. **StudentScheduleGrid**: Individual student schedule view

#### State Management
- React hooks for local component state
- API service layer for backend communication
- Real-time schedule updates and validation

#### User Interface Features
- **Tab-based Navigation**: Schedule view, Student management, Planning tools
- **Interactive Grid**: Drag-and-drop schedule interface
- **Real-time Validation**: Immediate feedback on scheduling conflicts
- **Responsive Design**: Works across desktop and mobile devices

### Database Schema

The SQLite database includes the following key entities:

- **Students**: Student information and academic status
- **Courses**: Course catalog with prerequisites and requirements
- **Sections**: Specific course offerings with time/teacher assignments
- **Semesters**: Academic term management
- **Teachers**: Instructor information and availability
- **Classrooms**: Physical space management and capacity
- **Enrollments**: Student-section relationships
- **Section_Meetings**: Time slot assignments for sections

### Containerization Strategy

#### Multi-Stage Docker Builds
**Backend Container** (`docker/backend.Dockerfile`):
- Build stage: Maven with Eclipse Temurin 21 for compilation
- Runtime stage: Minimal JRE image for production
- Database bundling: SQLite file included in container
- Environment configuration: Flexible database and CORS settings

**Frontend Container** (`docker/frontend.Dockerfile`):
- Build stage: Node.js Alpine for npm build process
- Runtime stage: Nginx Alpine for static file serving
- Optimized build: Vite production build with tree-shaking
- Nginx configuration: Optimized for SPA routing

#### Docker Compose Orchestration
- Service isolation: Separate containers for frontend/backend
- Port mapping: Backend (8080), Frontend (5173)
- Dependency management: Frontend waits for backend startup
- Volume persistence: Database data persistence options
- Environment configuration: CORS and database path settings

## Development Approach

### Design Decisions

1. **Technology Selection**
   - **Spring Boot**: Chosen for rapid development, excellent ecosystem, and built-in features
   - **SQLite**: Lightweight, embedded database perfect for development and small deployments
   - **React + TypeScript**: Type safety and modern development experience
   - **Docker**: Consistent deployment across environments

2. **Scheduling Algorithm**
   - **Constraint Satisfaction**: Systematic approach to handling complex scheduling rules
   - **Greedy with Backtracking**: Balance between performance and solution quality
   - **Modular Design**: Easy to extend with additional constraints

3. **API Design**
   - **RESTful endpoints**: Standard HTTP methods for predictable API behavior
   - **JSON responses**: Consistent data format across all endpoints
   - **Error handling**: Comprehensive error responses with helpful messages

4. **Database Design**
   - **Normalized schema**: Reduces data redundancy while maintaining query performance
   - **Foreign key constraints**: Ensures data integrity
   - **Indexing strategy**: Optimized for common query patterns

### Scalability Considerations

- **Stateless backend**: Enables horizontal scaling
- **Database optimization**: Prepared statements and connection pooling
- **Frontend optimization**: Code splitting and lazy loading
- **Caching strategy**: Ready for Redis integration for session management

### Testing Strategy

- **Unit tests**: Service layer business logic validation
- **Integration tests**: API endpoint testing with test database
- **Frontend testing**: Component testing with React Testing Library
- **End-to-end testing**: Complete workflow validation

## Deployment Notes

### Production Considerations
- Replace SQLite with PostgreSQL or MySQL for production use
- Implement proper authentication and authorization
- Add monitoring and logging (e.g., Prometheus, ELK stack)
- Configure load balancing and auto-scaling
- Implement backup and disaster recovery procedures

### Environment Configuration
The application supports environment-based configuration:
- Database path: Configurable via `APP_DB_PATH`
- CORS origins: Configurable via `APP_CORS_ORIGINS`
- Port configuration: Adjustable through Docker Compose

### Security Features
- CORS configuration for cross-origin requests
- Input validation on all API endpoints
- SQL injection prevention through prepared statements
- XSS protection in React components

## Future Enhancements

### Planned Features
- **User Authentication**: Role-based access control (admin, teacher, student)
- **Advanced Scheduling**: Multi-semester planning and optimization
- **Reporting System**: Analytics and schedule utilization reports
- **Mobile App**: Native mobile application for students and teachers
- **Integration APIs**: LMS and SIS system integrations

### Performance Optimizations
- **Database Migration**: PostgreSQL for improved concurrent performance
- **Caching Layer**: Redis for session management and frequently accessed data
- **API Optimization**: GraphQL for flexible data fetching
- **Frontend Optimization**: Service worker for offline functionality

---

> **Database Location**: The SQLite database is bundled at `backend/src/main/resources/maplewood_school.sqlite` and copied to `/app/data` in the container. For custom databases, replace the file or use volume mounting to `/app/data/maplewood_school.sqlite`.