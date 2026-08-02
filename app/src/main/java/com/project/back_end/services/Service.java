package com.project.back_end.services;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repositories.AdminRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService
    ) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    /**
     * Validates a token for the specified user role.
     */
    public ResponseEntity<Map<String, String>> validateToken(
            String token,
            String user
    ) {
        Map<String, String> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put("message", "Token is required.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        if (user == null || user.isBlank()) {
            response.put("message", "User role is required.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            boolean valid = tokenService.validateToken(token, user);

            if (!valid) {
                response.put(
                        "message",
                        "Token is invalid or expired."
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            response.put(
                    "message",
                    "Token is invalid or expired."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

    /**
     * Validates administrator credentials and returns a token.
     */
    public ResponseEntity<Map<String, String>> validateAdmin(
            Admin receivedAdmin
    ) {
        Map<String, String> response = new HashMap<>();

        if (
            receivedAdmin == null ||
            receivedAdmin.getUsername() == null ||
            receivedAdmin.getPassword() == null
        ) {
            response.put(
                    "message",
                    "Username and password are required."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        Admin storedAdmin = adminRepository.findByUsername(
                receivedAdmin.getUsername().trim()
        );

        if (
            storedAdmin == null ||
            !storedAdmin.getPassword().equals(
                    receivedAdmin.getPassword()
            )
        ) {
            response.put("message", "Invalid credentials.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        String token = tokenService.generateToken(
                storedAdmin.getUsername(),
                "admin"
        );

        response.put("token", token);
        response.put("message", "Admin login successful.");

        return ResponseEntity.ok(response);
    }

    /**
     * Selects the appropriate doctor filter based on supplied values.
     */
    public Map<String, Object> filterDoctor(
            String name,
            String specialty,
            String time
    ) {
        boolean hasName = hasValue(name);
        boolean hasSpecialty = hasValue(specialty);
        boolean hasTime = hasValue(time);

        if (hasName && hasSpecialty && hasTime) {
            return doctorService
                    .filterDoctorsByNameSpecilityandTime(
                            name.trim(),
                            specialty.trim(),
                            time.trim()
                    );
        }

        if (hasName && hasTime) {
            return doctorService.filterDoctorByNameAndTime(
                    name.trim(),
                    time.trim()
            );
        }

        if (hasName && hasSpecialty) {
            return doctorService.filterDoctorByNameAndSpecility(
                    name.trim(),
                    specialty.trim()
            );
        }

        if (hasSpecialty && hasTime) {
            return doctorService.filterDoctorByTimeAndSpecility(
                    specialty.trim(),
                    time.trim()
            );
        }

        if (hasName) {
            return doctorService.findDoctorByName(name.trim());
        }

        if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(
                    specialty.trim()
            );
        }

        if (hasTime) {
            return doctorService.filterDoctorsByTime(time.trim());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());

        return response;
    }

    /**
     * Validates that the selected doctor exists and that the
     * appointment time matches one of the doctor's available slots.
     *
     * @return 1 when valid, 0 when unavailable, -1 when doctor
     *         does not exist.
     */
    public int validateAppointment(Appointment appointment) {
        if (
            appointment == null ||
            appointment.getDoctor() == null ||
            appointment.getDoctor().getId() == null ||
            appointment.getAppointmentTime() == null
        ) {
            return 0;
        }

        Long doctorId = appointment.getDoctor().getId();

        Optional<Doctor> doctorOptional =
                doctorRepository.findById(doctorId);

        if (doctorOptional.isEmpty()) {
            return -1;
        }

        LocalDate appointmentDate =
                appointment.getAppointmentTime().toLocalDate();

        LocalTime requestedTime =
                appointment.getAppointmentTime()
                        .toLocalTime()
                        .withSecond(0)
                        .withNano(0);

        List<String> availableSlots =
                doctorService.getDoctorAvailability(
                        doctorId,
                        appointmentDate
                );

        boolean available = availableSlots.stream()
                .map(this::extractStartTime)
                .filter(time -> time != null)
                .anyMatch(time -> time.equals(requestedTime));

        return available ? 1 : 0;
    }

    /**
     * Returns true when no patient exists with the same email
     * or phone number.
     */
    public boolean validatePatient(Patient patient) {
        if (
            patient == null ||
            patient.getEmail() == null ||
            patient.getPhone() == null
        ) {
            return false;
        }

        Patient existingPatient =
                patientRepository.findByEmailOrPhone(
                        patient.getEmail().trim(),
                        patient.getPhone().trim()
                );

        return existingPatient == null;
    }

    /**
     * Validates patient credentials and returns an authentication
     * token when successful.
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(
            Login login
    ) {
        Map<String, String> response = new HashMap<>();

        if (
            login == null ||
            login.getIdentifier() == null ||
            login.getPassword() == null
        ) {
            response.put(
                    "message",
                    "Email and password are required."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        Patient patient = patientRepository.findByEmail(
                login.getIdentifier().trim()
        );

        if (
            patient == null ||
            !patient.getPassword().equals(login.getPassword())
        ) {
            response.put("message", "Invalid credentials.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        String token = tokenService.generateToken(
                patient.getEmail(),
                "patient"
        );

        response.put("token", token);
        response.put("message", "Patient login successful.");

        return ResponseEntity.ok(response);
    }

    /**
     * Filters the logged-in patient's appointments by condition,
     * doctor name, or both.
     */
    public ResponseEntity<Map<String, Object>> filterPatient(
            String condition,
            String name,
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put(
                    "message",
                    "Authorization token is required."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        try {
            String email = tokenService.extractEmailFromToken(token);

            if (email == null || email.isBlank()) {
                response.put("message", "Invalid token.");

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                response.put("message", "Patient not found.");

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            boolean hasCondition = hasValue(condition);
            boolean hasName = hasValue(name);

            if (hasCondition && hasName) {
                return patientService
                        .filterByDoctorAndCondition(
                                condition.trim(),
                                name.trim(),
                                patient.getId()
                        );
            }

            if (hasCondition) {
                return patientService.filterByCondition(
                        condition.trim(),
                        patient.getId()
                );
            }

            if (hasName) {
                return patientService.filterByDoctor(
                        name.trim(),
                        patient.getId()
                );
            }

            return patientService.getPatientAppointment(
                    patient.getId(),
                    token
            );

        } catch (Exception exception) {
            response.put(
                    "message",
                    "Unable to filter patient appointments."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    private boolean hasValue(String value) {
        return value != null &&
                !value.isBlank() &&
                !"null".equalsIgnoreCase(value);
    }

    /**
     * Extracts the beginning time from availability strings such as:
     * 09:00 - 10:00
     * 09:00-10:00
     * 09:00 AM - 10:00 AM
     */
    private LocalTime extractStartTime(String timeSlot) {
        if (timeSlot == null || timeSlot.isBlank()) {
            return null;
        }

        String startTime = timeSlot.split("-")[0].trim();

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(startTime, formatter)
                        .withSecond(0)
                        .withNano(0);
            } catch (DateTimeParseException ignored) {
                // Try the next time format.
            }
        }

        return null;
    }
}