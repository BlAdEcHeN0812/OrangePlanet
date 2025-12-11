# ZJU Helper (Web)

A Spring Boot web application for Zhejiang University students to view their course schedule.

## Features

- **Course Schedule**: View your class schedule in a web browser.
- **ZJU Passport Login**: Secure login via ZJU Unified Identity Authentication.

## Architecture

The project is a Spring Boot application serving a static frontend:

- **Backend**: Spring Boot 2.7 (Java 11)
- **Frontend**: HTML/CSS/JS (served from `src/main/resources/static`)
- **Database**: H2 In-Memory Database

## Getting Started

1.  **Prerequisites**: Java 11+, Maven.
2.  **Build**: `mvn clean package`
3.  **Run**: `java -jar target/zjuhelper-1.0-SNAPSHOT.jar`
4.  **Access**: Open [http://localhost:8080](http://localhost:8080) in your browser.

