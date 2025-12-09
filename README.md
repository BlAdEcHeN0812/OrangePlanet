# ZJU Helper

A Java utility application for Zhejiang University students.

## Features

- **Course Schedule**: View your class schedule.
- **Grades & Exams**: Query grades and exam arrangements.
- **Learning at ZJU**: Access assignments, download attachments, view courseware.
- **Attendance**: Automatic detection of attendance checks.
- **Zhiyun**: View class replays.
- **Email**: Access ZJU email.

## Architecture

The project is structured as a standard Maven project:

- `src/main/java/com/orangeplanet/zjuhelper`
    - `api`: Interfaces for interacting with ZJU services (Passport, Dean's Office, Canvas, etc.).
    - `model`: Data models for courses, users, grades, etc.
    - `service`: Business logic and data aggregation.
    - `util`: Utilities for HTTP requests and HTML parsing.

## Getting Started

1.  **Prerequisites**: Java 17+, Maven.
2.  **Build**: `mvn clean install`
3.  **Run**: `java -jar target/zjuhelper-1.0-SNAPSHOT.jar`

## First Step

The first step in development is to implement the **Authentication Service** (`AuthService`) to handle login via ZJU Passport (Unified Identity Authentication). This is required for all other features.
