# coupon-service

A lightweight REST microservice for discount coupon management.

## Features
- Create new discount coupons
- Register coupon usage by a user

# Tech stack
- Java 17
- Spring Boot 4
- PostgreSQL
- Liquibase
- OpenAPI

# Database
Project need to have postgreSQL database running, and the connection details should be provided in `application.properties` file.
PostgeSQL version 15 or higher is recommended.
Schema need to be created before running the application, and liquibase will take care of creating tables and inserting initial data.

Password for database can be set in environment variable `DB_PASSWORD` and `DB_USER` or directly in `application.properties` file (not recommended for production).

# API
see: OpenAPI: [openapi-coupon.yaml](src/main/resources/openapi/openapi-coupon.yaml)

## Notes
- Authentication is out of scope
- Built with Java & Spring Boot
