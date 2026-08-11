# Smart Clinic Management System - Database Schema Design

## 1. MySQL Database Design

The Smart Clinic Management System uses MySQL for structured and relational data. The database contains information about patients, doctors, appointments, and administrators.

### 1.1 Patients Table

| Column | Data Type | Key | Constraints |
|---|---|---|---|
| patient_id | BIGINT | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| first_name | VARCHAR(50) | | NOT NULL |
| last_name | VARCHAR(50) | | NOT NULL |
| email | VARCHAR(100) | UNIQUE | NOT NULL |
| phone | VARCHAR(20) | UNIQUE | NOT NULL |
| date_of_birth | DATE | | NOT NULL |
| gender | VARCHAR(20) | | |
| address | VARCHAR(255) | | |
| created_at | TIMESTAMP | | NOT NULL |

The `patient_id` uniquely identifies each patient. Email and phone numbers are unique to prevent duplicate patient accounts.

---

### 1.2 Doctors Table

| Column | Data Type | Key | Constraints |
|---|---|---|---|
| doctor_id | BIGINT | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| first_name | VARCHAR(50) | | NOT NULL |
| last_name | VARCHAR(50) | | NOT NULL |
| email | VARCHAR(100) | UNIQUE | NOT NULL |
| phone | VARCHAR(20) | UNIQUE | NOT NULL |
| specialization | VARCHAR(100) | | NOT NULL |
| license_number | VARCHAR(50) | UNIQUE | NOT NULL |
| availability | VARCHAR(255) | | |

The `doctor_id` uniquely identifies each doctor. The license number is unique because every doctor should have a unique professional license.

---

### 1.3 Appointments Table

| Column | Data Type | Key | Constraints |
|---|---|---|---|
| appointment_id | BIGINT | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| patient_id | BIGINT | FOREIGN KEY | NOT NULL |
| doctor_id | BIGINT | FOREIGN KEY | NOT NULL |
| appointment_date | DATE | | NOT NULL |
| appointment_time | TIME | | NOT NULL |
| status | VARCHAR(30) | | NOT NULL |
| reason | VARCHAR(255) | | |
| created_at | TIMESTAMP | | NOT NULL |

Foreign key relationships:

- `patient_id` references `patients(patient_id)`
- `doctor_id` references `doctors(doctor_id)`

An appointment must belong to an existing patient and an existing doctor.

The `status` field can contain values such as:

- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

---

### 1.4 Admin Table

| Column | Data Type | Key | Constraints |
|---|---|---|---|
| admin_id | BIGINT | PRIMARY KEY | NOT NULL, AUTO_INCREMENT |
| first_name | VARCHAR(50) | | NOT NULL |
| last_name | VARCHAR(50) | | NOT NULL |
| username | VARCHAR(50) | UNIQUE | NOT NULL |
| email | VARCHAR(100) | UNIQUE | NOT NULL |
| password | VARCHAR(255) | | NOT NULL |
| role | VARCHAR(30) | | NOT NULL |

The `admin_id` uniquely identifies an administrator. Username and email are unique so that multiple administrator accounts cannot use the same credentials.

---

## 2. Relationships Between MySQL Tables

The main relationships are:

- One patient can have many appointments.
- One doctor can have many appointments.
- Each appointment belongs to one patient.
- Each appointment belongs to one doctor.
- Administrators manage the clinic system and its users.

The relationships can be represented as:

```text
Patients
   |
   | 1
   |
   | N
Appointments
   |
   | N
   |
   | 1
Doctors

Admin
   |
   | manages
   |
   +--------------------+
                        |
                     Patients
                     Doctors
                  Appointments
