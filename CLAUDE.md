# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.5 application named "flik" built with Java 21 and Gradle. It's a comprehensive web application with the following key components:

- **Framework**: Spring Boot with Spring Security, Spring Batch, and Spring WebFlux
- **Database**: MySQL with Spring Data JPA and JDBC support
- **Authentication**: OAuth2 client integration
- **Build Tool**: Gradle with wrapper
- **Package Structure**: `yunrry.flik` as the root package

## Development Commands

### Build and Run
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run in development mode (with DevTools)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "yunrry.flik.FlikApplicationTests"

# Run tests with continuous build
./gradlew test --continuous
```

### Code Quality
```bash
# Check for compile errors
./gradlew compileJava

# Clean build artifacts
./gradlew clean
```

## Architecture

### Core Components
- **Main Application**: `FlikApplication.java` - Standard Spring Boot entry point
- **Configuration**: `application.properties` - Currently minimal with just application name
- **Dependencies**: Includes reactive web, batch processing, security, and database access layers

### Key Technologies Stack
- **Web Layer**: Spring Web + Spring WebFlux (reactive)
- **Security**: Spring Security + OAuth2 Client
- **Data Layer**: Spring Data JPA + Spring Data JDBC + MySQL
- **Batch Processing**: Spring Batch
- **Development**: Spring Boot DevTools, Lombok
- **Testing**: JUnit 5, Spring Boot Test, Reactor Test

### Directory Structure
- `src/main/java/yunrry/flik/` - Main application code
- `src/main/resources/` - Configuration files and static resources
- `src/test/java/yunrry/flik/` - Test classes
- `gradle/` - Gradle wrapper configuration

This project appears to be in early stages with basic Spring Boot scaffolding in place, ready for development of a web application with batch processing and OAuth2 authentication capabilities.