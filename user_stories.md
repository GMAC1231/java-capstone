# Clinic Management System — User Stories

## Project Overview

The Clinic Management System allows administrators, doctors, and patients to manage clinic operations through role-based access. The system supports doctor management, patient registration and login, appointment booking, appointment tracking, and prescription management.

---

## Admin User Stories

### Admin Login

As an administrator, I want to log in using my username and password so that I can securely access the admin dashboard.

**Acceptance Criteria**

* The administrator can submit a username and password.
* The system verifies the credentials against the admin database.
* A valid login returns an authentication token.
* Invalid credentials return an appropriate error message.
* The admin password is not exposed in API responses.

### View Doctors

As an administrator, I want to view all registered doctors so that I can manage clinic staff.

**Acceptance Criteria**

* The admin dashboard displays a list of doctors.
* Each doctor card shows name, specialty, email, phone number, and available times.
* The doctor list is loaded from the backend API.

### Add Doctor

As an administrator, I want to add a new doctor so that the doctor can use the clinic system.

**Acceptance Criteria**

* The admin can enter the doctor's name, specialty, email, password, phone number, and available times.
* Required fields are validated.
* The phone number must contain exactly 10 digits.
* The password must contain at least 6 characters.
* Duplicate doctor emails are rejected.
* A successful request saves the doctor in the database.

### Update Doctor

As an administrator, I want to update doctor information so that clinic records remain accurate.

**Acceptance Criteria**

* The system verifies that the doctor exists.
* The admin can update the doctor's contact information, specialty, password, and availability.
* Invalid doctor IDs return an error.
* Successful updates are saved to the database.

### Delete Doctor

As an administrator, I want to delete a doctor so that inactive staff members are removed from the system.

**Acceptance Criteria**

* Only an authenticated admin can delete a doctor.
* The system verifies that the doctor exists.
* Related appointments are deleted before the doctor is removed.
* A success or error message is returned.

### Filter Doctors

As an administrator, I want to filter doctors by name, specialty, and available time so that I can quickly find the required doctor.

**Acceptance Criteria**

* Doctors can be searched by partial name.
* Specialty filtering is case-insensitive.
* Availability can be filtered by AM or PM.
* Combined filters are supported.
* An empty result displays a “No doctors found” message.

---

## Doctor User Stories

### Doctor Login

As a doctor, I want to log in using my email and password so that I can access my appointments.

**Acceptance Criteria**

* The doctor submits an email and password.
* The system validates the credentials.
* A successful login returns a JWT token.
* Invalid credentials return an unauthorized response.
* The password is excluded from API responses.

### View Appointments

As a doctor, I want to view my appointments for a selected date so that I can manage my schedule.

**Acceptance Criteria**

* Only an authenticated doctor can access appointment records.
* Appointments can be loaded for the current date or another selected date.
* The appointment table displays patient details.
* Results are ordered by appointment time.
* If no appointments are found, an appropriate message is displayed.

### Search Patients in Appointments

As a doctor, I want to search appointments by patient name so that I can quickly find a patient.

**Acceptance Criteria**

* Partial patient names are supported.
* Search is case-insensitive.
* Results are limited to the logged-in doctor's appointments.
* Search can be combined with a selected appointment date.

### Add Prescription

As a doctor, I want to save a prescription for an appointment so that the patient receives medication instructions.

**Acceptance Criteria**

* Only an authenticated doctor can save prescriptions.
* The prescription includes patient name, appointment ID, medication, dosage, and optional notes.
* Required fields are validated.
* The prescription is stored in MongoDB.
* A successful save returns HTTP 201 Created.

### View Prescription

As a doctor, I want to view prescriptions by appointment ID so that I can review previously issued medication instructions.

**Acceptance Criteria**

* Only an authenticated doctor can retrieve prescriptions.
* Prescriptions are searched using the appointment ID.
* The response includes the prescription data.
* If no prescription exists, the system returns an empty result or appropriate message.

---

## Patient User Stories

### Patient Registration

As a patient, I want to create an account so that I can book appointments.

**Acceptance Criteria**

* The patient provides name, email, password, phone number, and address.
* Required fields are validated.
* Email must use a valid format.
* The phone number must contain exactly 10 digits.
* The password must contain at least 6 characters.
* Duplicate email or phone records are rejected.
* A successful registration saves the patient.

### Patient Login

As a patient, I want to log in using my email and password so that I can access booking and appointment features.

**Acceptance Criteria**

* The system validates the patient's email and password.
* A successful login returns a token.
* The role is stored as `loggedPatient`.
* Invalid credentials return an error message.

### Browse Doctors

As a patient, I want to view all doctors so that I can choose a suitable healthcare provider.

**Acceptance Criteria**

* Doctor cards show name, specialty, contact information, and availability.
* Unauthenticated patients can browse doctors.
* Doctor passwords are never exposed.

### Filter Doctors

As a patient, I want to search and filter doctors so that I can find the most suitable provider.

**Acceptance Criteria**

* Doctors can be searched by partial name.
* Doctors can be filtered by specialty.
* Doctors can be filtered by AM or PM availability.
* Multiple filters can be applied together.

### Book Appointment

As a logged-in patient, I want to book an available appointment so that I can meet with a doctor.

**Acceptance Criteria**

* The patient must be authenticated.
* The doctor must exist.
* The appointment time must be in the future.
* The requested time must match the doctor's available schedule.
* The time slot must not already be booked.
* A successful booking returns HTTP 201 Created.

### Update Appointment

As a patient, I want to update my appointment so that I can change the scheduled time or doctor.

**Acceptance Criteria**

* The patient must be authenticated.
* The appointment must exist.
* The new appointment details must pass validation.
* The updated time must be available.
* A success or error response is returned.

### Cancel Appointment

As a patient, I want to cancel my appointment so that the clinic knows I will not attend.

**Acceptance Criteria**

* The patient must be authenticated.
* The appointment must exist.
* Only the patient who booked the appointment can cancel it.
* The appointment is removed from the database after successful validation.

### View Appointments

As a patient, I want to view my appointments so that I can track scheduled and completed visits.

**Acceptance Criteria**

* The token is used to identify the patient.
* A patient cannot access another patient's appointments.
* Appointments are returned as Appointment DTO objects.
* Sensitive entity data is excluded.

### Filter Appointments

As a patient, I want to filter appointments by doctor and condition so that I can review relevant records.

**Acceptance Criteria**

* Appointments can be filtered by doctor name.
* Appointments can be filtered as past or future.
* Past appointments use status `1`.
* Future appointments use status `0`.
* Doctor and condition filters can be combined.

---

## Security User Stories

### Token Validation

As the system, I want to validate JWT tokens so that protected resources are accessible only to authenticated users.

**Acceptance Criteria**

* Tokens contain the user's identifier as the subject.
* Tokens expire after seven days.
* Tokens are signed using a configured secret key.
* The token role must match the requested role.
* Expired or invalid tokens are rejected.

### Role-Based Access

As the system, I want to restrict features by role so that users can only perform permitted actions.

**Acceptance Criteria**

* Only admins can add, update, and delete doctors.
* Only doctors can create and retrieve prescriptions.
* Only patients can book, update, and cancel appointments.
* Dashboard access is validated before rendering protected views.

---

## Non-Functional User Stories

### Validation

As the system, I want to validate submitted data so that stored records remain accurate and complete.

**Acceptance Criteria**

* Validation errors return HTTP 400 Bad Request.
* Field-specific validation messages are returned.
* Null, length, email, phone, and future-date constraints are enforced.

### Maintainability

As a developer, I want the frontend and backend to use modular components so that the project is easier to maintain.

**Acceptance Criteria**

* Common headers and footers are reusable components.
* API communication is separated into service modules.
* Doctor cards and patient rows are reusable UI components.
* Backend logic is separated into controllers, services, repositories, DTOs, and models.

### Continuous Integration

As a developer, I want automated checks on every push and pull request so that code quality problems are found early.

**Acceptance Criteria**

* HTML, CSS, and JavaScript lint workflows run automatically.
* Java Checkstyle runs automatically.
* Maven compilation runs automatically.
* The Dockerfile is checked using Hadolint.
