# Product Management System

## Overview
A robust web application for managing products, leveraging modern technologies and frameworks:
- **Spring Boot** for building a scalable backend.
- **Postgres** for database storage.
- **HTMX** for dynamic, server-driven UI updates.
- **Thymeleaf** for server-side templating.
- **Shoelace** for clean, responsive UI components.
- **Flyway** for database migrations.
- **JdbcClient** for efficient database operations without using Hibernate or JPA.
- **@Scheduled annotation** for scheduling background jobs.

## Features
- Dynamic product listing and management.
- Integration with an external API to fetch product data.
- Scheduled tasks to populate product data if none exists.
- Modular and clean architecture.

## Technologies
### Backend
- **Spring Boot**: Backend framework.
- **JdbcClient**: Direct database interaction.
- **Flyway**: Manage database schema changes.
- **Scheduled Jobs**: `@Scheduled` annotation for periodic tasks.

### Frontend
- **HTMX**: Minimal JS for dynamic UI updates.
- **Shoelace**: Web components for styling.
- **Thymeleaf**: Server-side rendering.

### Database
- **Postgres**: Relational database.

## API Endpoints
### ProductController
| HTTP Method | Endpoint        | Description                  |
|-------------|-----------------|------------------------------|
| GET         | `/test`         | Displays a list of products. |
| GET         | `/test/table`   | Returns a table fragment.    |
| POST        | `/test/product` | Adds a new product.          |

## Scheduled Tasks
- **Product Fetching**:
    - Endpoint: `https://famme.no/products.json`
    - Saves data to `products` and `variants` tables if no data exists.

## Database Schema
### Products Table
| Column      | Type          | Description       |
|-------------|---------------|-------------------|
| id          | BIGINT        | Primary key.      |
| title       | TEXT          | Product title.    |
| vendor      | TEXT          | Vendor name.      |
| type        | TEXT          | Product type.     |
| created_at  | TIMESTAMP     | Creation date.    |
| updated_at  | TIMESTAMP     | Last update date. |

### Variants Table
| Column     | Type      | Description                |
|------------|-----------|----------------------------|
| id         | BIGINT    | Primary key.               |
| product_id | BIGINT    | Foreign key to `products`. |
| title      | TEXT      | Variant title.             |
| sku        | TEXT      | SKU code.                  |
| price      | DOUBLE    | Price.                     |
| available  | BOOLEAN   | Availability status.       |
| option1    | TEXT      | First option.              |
| option2    | TEXT      | Second option.             |
| created_at | TIMESTAMP | Creation date.             |
| updated_at | TIMESTAMP | Last update date.          |

## Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/jay-rudani/simple-frontend-backend
   cd simple-frontend-backend
   ```

2. **Configure the Database**
    - Update `application.yml`:
      ```yaml
      spring:
        datasource:
          url: jdbc:postgresql://localhost:5432/products_db
          username: your-username
          password: your-password
        flyway.enabled: true
      ```

3. **Start the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the Application**
    - Open [http://localhost:8080/test](http://localhost:8080/test) to view products.
