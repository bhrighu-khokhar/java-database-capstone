# Smart Clinic Management System - Database Schema Design

## MySQL Database Design

The Smart Clinic Management System uses MySQL for structured and relational data such as patients, doctors, appointments, and administrators.

### Table: patients

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `email`: VARCHAR(100), Not Null, Unique
- `phone`: VARCHAR(20), Not Null, Unique
- `date_of_birth`: DATE, Not Null
- `gender`: VARCHAR(20)
- `address`: VARCHAR(255)
- `created_at`: TIMESTAMP, Not Null

The `id` uniquely identifies each patient. Email and phone are unique to prevent duplicate patient accounts.

---

### Table: doctors

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `email`: VARCHAR(100), Not Null, Unique
- `phone`: VARCHAR(20), Not Null, Unique
- `specialization`: VARCHAR(100), Not Null
- `license_number`: VARCHAR(50), Not Null, Unique
- `working_hours`: VARCHAR(255)

The `license_number` is unique because each doctor must have a unique professional license.

---

### Table: appointments

- `id`: BIGINT, Primary Key, Auto Increment
- `doctor_id`: BIGINT, Foreign Key → doctors(id), Not Null
- `patient_id`: BIGINT, Foreign Key → patients(id), Not Null
- `appointment_time`: DATETIME, Not Null
- `status`: VARCHAR(20), Not Null
- `reason`: VARCHAR(255)
- `created_at`: TIMESTAMP, Not Null

Possible appointment statuses include:

- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

Each appointment belongs to exactly one patient and one doctor.

A patient can have many appointments, and a doctor can have many appointments.

The system should prevent overlapping appointments for the same doctor through application-level validation before creating a new appointment.

Past appointments should be retained so that the clinic can maintain appointment history and doctors can refer to previous consultations.

---

### Table: admin

- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `username`: VARCHAR(50), Not Null, Unique
- `email`: VARCHAR(100), Not Null, Unique
- `password`: VARCHAR(255), Not Null
- `role`: VARCHAR(30), Not Null

The `username` and `email` fields are unique to prevent duplicate administrator accounts.

Passwords should be stored using secure hashing rather than plain text.

---

### MySQL Relationships

The main relationships between the tables are:

- One patient can have many appointments.
- One doctor can have many appointments.
- Each appointment belongs to one patient.
- Each appointment belongs to one doctor.
- Administrators manage patients, doctors, and appointments.

The appointment table connects patients and doctors through foreign keys.

```text
patients
    |
    | 1
    |
    | N
appointments
    |
    | N
    |
    | 1
doctors

admin
  |
  | manages
  |
  +---- patients
  +---- doctors
  +---- appointments
```
