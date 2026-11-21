
# UrbanService — On-Demand Home Services Platform

A full-stack web application inspired by **Urban Company**, built with **React + Vite + Bootstrap 5** on the frontend and **Java 21 + Spring Boot 3.4 + Spring Security + Spring Data JPA** on the backend. It supports multi-role workflows for **Customers**, **Service Providers**, and **Platform Admins** with session + CSRF security and optimistic concurrency control.

---

## Key Features

### Role-Based Access & Security
* **Session + CSRF Authentication:** HTTP-only session cookies (`JSESSIONID`) paired with `CookieCsrfTokenRepository` (`X-XSRF-TOKEN`) and strict CORS configuration.
* **Server-Enforced Roles:** Public registration automatically assigns the `CUSTOMER` role; `PROVIDER` accounts are provisioned exclusively by `ADMIN`.
* **Object-Level Ownership:** Customers can only manage their own addresses and cancel their own bookings; providers can only update jobs assigned to them.

### Workflows by Role
* **Customer (`CUSTOMER`)**
  * Register and sign in securely.
  * Manage multiple saved service addresses (Home, Office, etc.).
  * Browse active service packages across categories and book preferred dates and time slots.
  * Track booking status and cancel `PENDING` or `ASSIGNED` bookings.
* **Service Provider (`PROVIDER`)**
  * View open `PENDING` jobs and assigned bookings.
  * Accept open jobs (`PENDING` → `ASSIGNED`) with slot double-booking prevention.
  * Progress jobs through execution (`ASSIGNED` → `IN_PROGRESS` → `COMPLETED`).
* **Administrator (`ADMIN`)**
  * Create and manage service categories (e.g., *AC & Appliance Repair*, *Home Cleaning*).
  * Publish new service packages with pricing and duration.
  * Onboard and activate service provider accounts.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | React 19, Vite 6, React Router 7, Axios, Bootstrap 5 |
| **Backend** | Java 21, Spring Boot 3.4.3, Spring Security, Spring Data JPA, Jakarta Validation |
| **Database** | H2 (In-Memory for local dev) / MySQL 8+ (Production persistence) |

---

## Project Structure

```text
urban-service-platform/
├── backend/                  # Spring Boot REST API (Port 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/urbanservice/app/
│       │   ├── auth/         # Authentication, CSRF, CurrentUser resolution
│       │   ├── bookings/     # Transactional booking lifecycle & optimistic locking
│       │   ├── catalog/      # Service categories & packages
│       │   ├── common/       # Global API exception & validation handler
│       │   ├── config/       # SecurityConfig & DataInitializer (default admin + seed catalog)
│       │   └── users/        # User accounts & customer address management
│       └── resources/
│           └── application.properties
└── frontend/                 # React + Vite SPA (Port 5173)
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/              # Axios client with automatic X-XSRF-TOKEN interceptor
        ├── auth/             # AuthContext & ProtectedRoute role guards
        └── pages/            # Login, Register, BookService, Addresses, MyBookings, AdminDashboard

```

---

## Getting Started

### Prerequisites

* **Java 21** (JDK 21+)
* **Node.js 20+** and **npm**
* **MySQL 8+** *(optional — configured with H2 in-memory database out of the box)*

### 1. Run the Backend (`Port 8080`)

Open `urban-service-platform/backend` in VS Code (with Extension Pack for Java) or IntelliJ IDEA and run `UrbanServiceApplication.java`, or run via Maven:

```bash
cd urban-service-platform/backend
mvn spring-boot:run

```

Verify the server is up at: `http://localhost:8080/api/health`

### 2. Run the Frontend (`Port 5173`)

```bash
cd urban-service-platform/frontend
npm install
npm run dev -- --host localhost --port 5173 --strictPort

```

Open `http://localhost:5173` in your browser.

---

## Default Seeded Credentials

On first startup, `DataInitializer` seeds a default Admin account and starter service packages:

* **Admin Email:** `admin@urbanservice.local`
* **Admin Password:** `Admin@1234`

> **Tip:** Log in as Admin first to create a `PROVIDER` account, then register a `CUSTOMER` account via `/register` to test the full booking-to-completion lifecycle.

---

## Core API Endpoints

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `GET` | `/api/health` | Public | Application health check |
| `GET` | `/api/auth/csrf` | Public | Fetch CSRF token cookie |
| `POST` | `/api/auth/register` | Public | Register a new customer |
| `POST` | `/api/auth/login` | Public | Authenticate session (`form-urlencoded`) |
| `GET` | `/api/auth/me` | Authenticated | Get current user profile & role |
| `GET` | `/api/catalog/categories` | Public | List active service categories |
| `GET` | `/api/catalog/packages` | Public | List active service packages |
| `GET/POST` | `/api/customer/addresses` | `CUSTOMER` | List or add customer addresses |
| `GET/POST` | `/api/bookings` | Authenticated | View role-scoped bookings or create booking |
| `PATCH` | `/api/bookings/{id}/status` | Authenticated | Transition booking state with optimistic version check |
| `POST` | `/api/admin/providers` | `ADMIN` | Provision a new service provider |

```

```
