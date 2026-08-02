# Clinic Management System — Schema Design

## Overview

The Clinic Management System uses two database technologies:

* **MySQL** for relational clinic data
* **MongoDB** for prescription documents

The MySQL database is named `cms`. Spring Data JPA and Hibernate manage the relational tables. Spring Data MongoDB manages the `prescriptions` collection.

---

# MySQL Schema

## Entity Relationship Overview

```text
Admin

Doctor 1 ───────< Appointment >─────── 1 Patient
  |
  └──────< Doctor Available Times
```

A doctor can have many appointments.

A patient can have many appointments.

Each appointment belongs to exactly one doctor and one patient.

A doctor can have multiple availability entries.

---

## `admin` Table

Stores administrator login information.

| Column     | Type    | Constraints                 | Description             |
| ---------- | ------- | --------------------------- | ----------------------- |
| `id`       | BIGINT  | Primary key, auto-increment | Unique administrator ID |
| `username` | VARCHAR | Not null                    | Administrator username  |
| `password` | VARCHAR | Not null                    | Administrator password  |

### JPA Mapping

```java
@Entity
public class Admin
```

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

### Security Consideration

The password field is write-only in JSON:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
```

---

## `doctor` Table

Stores doctor information and login credentials.

| Column      | Type         | Constraints                    | Description         |
| ----------- | ------------ | ------------------------------ | ------------------- |
| `id`        | BIGINT       | Primary key, auto-increment    | Unique doctor ID    |
| `name`      | VARCHAR(100) | Not null                       | Doctor's full name  |
| `specialty` | VARCHAR(50)  | Not null                       | Medical specialty   |
| `email`     | VARCHAR      | Not null, valid email          | Doctor login email  |
| `password`  | VARCHAR      | Not null, minimum 6 characters | Doctor password     |
| `phone`     | VARCHAR(10)  | Not null                       | Doctor phone number |

### Validation Rules

* Name: 3–100 characters
* Specialty: 3–50 characters
* Email: valid email format
* Password: at least 6 characters
* Phone: exactly 10 digits

### Security Consideration

The password is excluded from JSON responses using:

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
```

---

## `doctor_available_times` Table

Stores the doctor's available appointment time slots.

This table is automatically created by JPA because `Doctor.availableTimes` uses `@ElementCollection`.

| Column            | Type    | Constraints | Description            |
| ----------------- | ------- | ----------- | ---------------------- |
| `doctor_id`       | BIGINT  | Foreign key | References `doctor.id` |
| `available_times` | VARCHAR | —           | Available time slot    |

Example values:

```text
09:00 - 10:00
10:00 - 11:00
14:00 - 15:00
```

### Relationship

```text
doctor.id
   |
   └── doctor_available_times.doctor_id
```

Deleting a doctor should also remove associated availability records.

---

## `patient` Table

Stores patient profile and login information.

| Column     | Type         | Constraints                    | Description                 |
| ---------- | ------------ | ------------------------------ | --------------------------- |
| `id`       | BIGINT       | Primary key, auto-increment    | Unique patient ID           |
| `name`     | VARCHAR(100) | Not null                       | Patient's full name         |
| `email`    | VARCHAR      | Not null, valid email          | Patient login email         |
| `password` | VARCHAR      | Not null, minimum 6 characters | Patient password            |
| `phone`    | VARCHAR(10)  | Not null                       | Patient phone number        |
| `address`  | VARCHAR(255) | Not null                       | Patient residential address |

### Validation Rules

* Name: 3–100 characters
* Email: valid email format
* Password: minimum 6 characters
* Phone: exactly 10 digits
* Address: maximum 255 characters

### Security Consideration

The password field is write-only in JSON responses.

---

## `appointment` Table

Stores scheduled meetings between doctors and patients.

| Column             | Type     | Constraints                 | Description                     |
| ------------------ | -------- | --------------------------- | ------------------------------- |
| `id`               | BIGINT   | Primary key, auto-increment | Unique appointment ID           |
| `doctor_id`        | BIGINT   | Foreign key, not null       | References `doctor.id`          |
| `patient_id`       | BIGINT   | Foreign key, not null       | References `patient.id`         |
| `appointment_time` | DATETIME | Not null, future date       | Appointment start date and time |
| `status`           | INTEGER  | Not null                    | Appointment status              |

### Status Values

| Value | Meaning             |
| ----- | ------------------- |
| `0`   | Scheduled or future |
| `1`   | Completed or past   |

### Relationships

```text
appointment.doctor_id → doctor.id
appointment.patient_id → patient.id
```

### JPA Mapping

```java
@ManyToOne
@NotNull
private Doctor doctor;
```

```java
@ManyToOne
@NotNull
private Patient patient;
```

### Derived Fields

The following values are calculated in the model or DTO and are not stored as separate columns:

| Field                 | Calculation                     |
| --------------------- | ------------------------------- |
| `appointmentDate`     | `appointmentTime.toLocalDate()` |
| `appointmentTimeOnly` | `appointmentTime.toLocalTime()` |
| `endTime`             | `appointmentTime.plusHours(1)`  |

These helper methods use `@Transient` in the entity.

---

# MongoDB Schema

## Database

```text
cms
```

## Collection

```text
prescriptions
```

The collection name is defined using:

```java
@Document(collection = "prescriptions")
```

---

## Prescription Document

Example structure:

```json
{
  "_id": "6807dd712725f013281e7201",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "_class": "com.project.back_end.models.Prescription"
}
```

## Fields

| Field           | Type            | Required            | Description                  |
| --------------- | --------------- | ------------------- | ---------------------------- |
| `_id`           | ObjectId/String | Auto-generated      | Unique prescription ID       |
| `patientName`   | String          | Yes                 | Patient's full name          |
| `appointmentId` | Long            | Yes                 | Related MySQL appointment ID |
| `medication`    | String          | Yes                 | Prescribed medication        |
| `dosage`        | String          | Yes                 | Dosage instructions          |
| `doctorNotes`   | String          | No                  | Additional notes             |
| `_class`        | String          | Generated by Spring | Java document type           |

### Validation Rules

* `patientName`: 3–100 characters
* `appointmentId`: required
* `medication`: 3–100 characters
* `dosage`: 3–20 characters
* `doctorNotes`: maximum 200 characters

---

# Cross-Database Relationship

MongoDB prescriptions reference MySQL appointments through:

```text
prescriptions.appointmentId → appointment.id
```

This relationship is logical rather than a database-enforced foreign key.

The application is responsible for confirming that the appointment exists before using or saving a prescription.

---

# Recommended Constraints

The current models support the required assignment behavior. The following constraints are recommended for stronger data integrity:

```sql
ALTER TABLE admin
ADD CONSTRAINT uk_admin_username UNIQUE (username);
```

```sql
ALTER TABLE doctor
ADD CONSTRAINT uk_doctor_email UNIQUE (email);
```

```sql
ALTER TABLE patient
ADD CONSTRAINT uk_patient_email UNIQUE (email);
```

```sql
ALTER TABLE patient
ADD CONSTRAINT uk_patient_phone UNIQUE (phone);
```

Recommended indexes:

```sql
CREATE INDEX idx_appointment_doctor_time
ON appointment (doctor_id, appointment_time);
```

```sql
CREATE INDEX idx_appointment_patient
ON appointment (patient_id);
```

```sql
CREATE INDEX idx_doctor_specialty
ON doctor (specialty);
```

MongoDB index:

```javascript
db.prescriptions.createIndex({ appointmentId: 1 })
```

---

# Table Creation

Hibernate creates and updates the MySQL tables using:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Expected tables:

```text
admin
appointment
doctor
doctor_available_times
patient
```

The MongoDB collection is created automatically after the first prescription is inserted.

---

# Data Flow

## Appointment Booking

```text
Patient
   ↓
AppointmentController
   ↓
Service.validateAppointment()
   ↓
DoctorService.getDoctorAvailability()
   ↓
AppointmentService.bookAppointment()
   ↓
appointment table
```

## Prescription Creation

```text
Doctor
   ↓
PrescriptionController
   ↓
Token validation
   ↓
PrescriptionService.savePrescription()
   ↓
MongoDB prescriptions collection
```

---

# Security Notes

* Passwords should not be returned in JSON.
* JWT tokens contain the username or email as the subject.
* Admin tokens use the admin username.
* Doctor and patient tokens use email addresses.
* Tokens expire after seven days.
* Database passwords and JWT secrets should be supplied through environment variables.
* Production passwords should be hashed using a password encoder rather than stored as plain text.
