package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repositories.AppointmentRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            TokenService tokenService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    /**
     * Books a new appointment.
     *
     * @return 1 when successful and 0 when unsuccessful.
     */
    public int bookAppointment(Appointment appointment) {
        try {
            Map<String, String> validationErrors =
                    validateAppointment(appointment);

            if (!validationErrors.isEmpty()) {
                return 0;
            }

            appointmentRepository.save(appointment);
            return 1;

        } catch (Exception exception) {
            System.err.println(
                    "Error booking appointment: " +
                    exception.getMessage()
            );

            return 0;
        }
    }

    /**
     * Updates an existing appointment.
     */
    public ResponseEntity<Map<String, String>> updateAppointment(
            Appointment appointment
    ) {
        Map<String, String> response = new HashMap<>();

        if (appointment == null || appointment.getId() == null) {
            response.put("message", "Appointment ID is required.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        Optional<Appointment> existingAppointmentOptional =
                appointmentRepository.findById(appointment.getId());

        if (existingAppointmentOptional.isEmpty()) {
            response.put("message", "Appointment not found.");

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        Map<String, String> validationErrors =
                validateAppointment(appointment);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(validationErrors);
        }

        try {
            Appointment existingAppointment =
                    existingAppointmentOptional.get();

            existingAppointment.setDoctor(appointment.getDoctor());
            existingAppointment.setPatient(appointment.getPatient());
            existingAppointment.setAppointmentTime(
                    appointment.getAppointmentTime()
            );
            existingAppointment.setStatus(appointment.getStatus());

            appointmentRepository.save(existingAppointment);

            response.put(
                    "message",
                    "Appointment updated successfully."
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            response.put(
                    "message",
                    "Unable to update appointment."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Cancels an appointment after confirming that the appointment
     * belongs to the patient represented by the supplied token.
     */
    public ResponseEntity<Map<String, String>> cancelAppointment(
            long id,
            String token
    ) {
        Map<String, String> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put("message", "Authorization token is required.");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        Optional<Appointment> appointmentOptional =
                appointmentRepository.findById(id);

        if (appointmentOptional.isEmpty()) {
            response.put("message", "Appointment not found.");

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        try {
            /*
             * Rename getUserIdFromToken() if your TokenService uses a
             * different method name.
             */
            Long patientId = tokenService.getUserIdFromToken(token);

            Appointment appointment = appointmentOptional.get();

            if (
                patientId == null ||
                appointment.getPatient() == null ||
                !patientId.equals(appointment.getPatient().getId())
            ) {
                response.put(
                        "message",
                        "You are not authorized to cancel this appointment."
                );

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(response);
            }

            appointmentRepository.delete(appointment);

            response.put(
                    "message",
                    "Appointment cancelled successfully."
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            response.put(
                    "message",
                    "Unable to cancel appointment."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Retrieves appointments belonging to the logged-in doctor for
     * the selected date. Patient name filtering is optional.
     */
    public Map<String, Object> getAppointment(
            String pname,
            LocalDate date,
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put("appointments", List.of());
            response.put("message", "Authorization token is required.");
            return response;
        }

        if (date == null) {
            date = LocalDate.now();
        }

        try {
            /*
             * Rename getUserIdFromToken() if your TokenService uses a
             * different method name.
             */
            Long doctorId = tokenService.getUserIdFromToken(token);

            if (doctorId == null) {
                response.put("appointments", List.of());
                response.put("message", "Invalid token.");
                return response;
            }

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

            List<Appointment> appointments;

            if (
                pname == null ||
                pname.isBlank() ||
                "null".equalsIgnoreCase(pname)
            ) {
                appointments =
                        appointmentRepository
                                .findByDoctorIdAndAppointmentTimeBetween(
                                        doctorId,
                                        start,
                                        end
                                );
            } else {
                appointments =
                        appointmentRepository
                                .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                        doctorId,
                                        pname.trim(),
                                        start,
                                        end
                                );
            }

            response.put("appointments", appointments);
            response.put("message", "Appointments retrieved successfully.");

        } catch (Exception exception) {
            response.put("appointments", List.of());
            response.put(
                    "message",
                    "Unable to retrieve appointments."
            );
        }

        return response;
    }

    /**
     * Validates doctor, patient, appointment time and schedule
     * conflicts.
     */
    private Map<String, String> validateAppointment(
            Appointment appointment
    ) {
        Map<String, String> errors = new HashMap<>();

        if (appointment == null) {
            errors.put("message", "Appointment data is required.");
            return errors;
        }

        if (
            appointment.getDoctor() == null ||
            appointment.getDoctor().getId() == null
        ) {
            errors.put("doctor", "A valid doctor ID is required.");
        }

        if (
            appointment.getPatient() == null ||
            appointment.getPatient().getId() == null
        ) {
            errors.put("patient", "A valid patient ID is required.");
        }

        if (appointment.getAppointmentTime() == null) {
            errors.put(
                    "appointmentTime",
                    "Appointment time is required."
            );
        } else if (
            !appointment.getAppointmentTime().isAfter(
                    LocalDateTime.now()
            )
        ) {
            errors.put(
                    "appointmentTime",
                    "Appointment time must be in the future."
            );
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        Optional<Doctor> doctorOptional =
                doctorRepository.findById(
                        appointment.getDoctor().getId()
                );

        if (doctorOptional.isEmpty()) {
            errors.put("doctor", "Doctor not found.");
        }

        Optional<Patient> patientOptional =
                patientRepository.findById(
                        appointment.getPatient().getId()
                );

        if (patientOptional.isEmpty()) {
            errors.put("patient", "Patient not found.");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        LocalDateTime requestedStart =
                appointment.getAppointmentTime();

        LocalDateTime requestedEnd =
                requestedStart.plusHours(1);

        List<Appointment> possibleConflicts =
                appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(
                                appointment.getDoctor().getId(),
                                requestedStart.minusMinutes(59),
                                requestedEnd.minusMinutes(1)
                        );

        boolean alreadyBooked = possibleConflicts.stream()
                .anyMatch(existing -> {
                    if (
                        appointment.getId() != null &&
                        appointment.getId().equals(existing.getId())
                    ) {
                        return false;
                    }

                    LocalDateTime existingStart =
                            existing.getAppointmentTime();

                    LocalDateTime existingEnd =
                            existingStart.plusHours(1);

                    return requestedStart.isBefore(existingEnd) &&
                            requestedEnd.isAfter(existingStart);
                });

        if (alreadyBooked) {
            errors.put(
                    "appointmentTime",
                    "The doctor already has an appointment during this time."
            );
        }

        if (errors.isEmpty()) {
            appointment.setDoctor(doctorOptional.get());
            appointment.setPatient(patientOptional.get());
        }

        return errors;
    }
}