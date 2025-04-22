# Car Workshop Maintenance System

## Project Overview
The Car Workshop Maintenance System is a comprehensive web application built using Spring Boot that helps users track and manage car maintenance records. The application allows car owners to register, add their vehicles, and keep detailed records of maintenance activities and oil changes.

## Features

### User Management
- User registration and authentication with Spring Security
- Role-based access control (Users and Administrators)
- Personalized user profiles with personal information management

### Car Management
- Add, edit, and remove cars from your personal fleet
- Track detailed car information including:
  - Make, model, year, and license plate
  - Subtype and current mileage
  - Maintenance schedule and history
  - Oil change intervals and history

### Maintenance Tracking
- Record and manage maintenance activities for each vehicle
- Track maintenance dates, description, cost, and who performed the service
- Record mileage at each maintenance interval
- View maintenance history chronologically

### Oil Change Management
- Track oil change schedules and history
- Record oil type, filter information, and services performed
- Calculate next recommended oil change based on mileage or time intervals
- Receive notifications for upcoming oil changes

### Admin Features
- User management capabilities
- System-wide statistics and reporting
- Ability to manage all vehicles and maintenance records

## Technical Details

### Technology Stack
- **Backend**: Java with Spring Boot 3.4.3
- **Security**: Spring Security with role-based authentication
- **Database**: PostgreSQL for production (previously MySQL, H2)
- **Frontend**: Thymeleaf templates with Bootstrap
- **Build Tool**: Maven

### Architecture
The application follows a standard Spring MVC architecture:
- **Domain Layer**: Entity models (Car, User, Maintenance, OilChange)
- **Repository Layer**: JPA repositories for data access
- **Service Layer**: Business logic implementation
- **Controller Layer**: Request handling and view preparation
- **View Layer**: Thymeleaf templates for the user interface

### Deployment
The application is configured for deployment to Heroku, making it accessible from anywhere.

## Installation and Setup

### Prerequisites
- Java 17 or higher
- Maven
- PostgreSQL database

### Configuration
1. Clone the repository
2. Configure the database connection in `application.properties`
3. Run the application using Maven: `mvn spring-boot:run`

### Database Configuration
The application uses PostgreSQL in production. Configure your database connection in the application properties file:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/carworkshop
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Usage Guide

### User Registration and Login
1. Navigate to the register page
2. Fill in your personal details and create a username/password
3. Log in with your credentials

### Adding a Car
1. Click on "Add New Car" from your dashboard
2. Fill in the car details including make, model, year, and license plate
3. Add maintenance history or start fresh

### Recording Maintenance
1. Select a car from your dashboard
2. Click on "Add Maintenance Record"
3. Fill in the details of the maintenance performed
4. Submit to save the record

### Tracking Oil Changes
1. Select a car from your dashboard
2. Navigate to "Oil Changes"
3. Add a new oil change record with date, mileage, and oil type
4. View recommendations for your next oil change

## License
This project is part of a Backend Programming course at Haaga-Helia University of Applied Sciences.

## Acknowledgements
- Spring Boot and the Spring Framework team
- PostgreSQL database
- Bootstrap for frontend styling
