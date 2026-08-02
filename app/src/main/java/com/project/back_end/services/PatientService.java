package com.project.back_end.services;

import com.project.back_end.dto.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repositories.AppointmentRepository;
import com.project.back_end.repositories.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Saves a new patient.
     *
     * @return 1 when successful and 0 when an error occurs.
     */
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception exception) {
            System.err.println(
                    "Error creating patient: " + exception.getMessage()
            );
            return 0;
        }
    }

    /**
     * Retrieves all appointments belonging to a patient after
     * validating that the token belongs to the same patient.
     */
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            Long id,
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        if (id == null) {
            response.put("message", "Patient ID is required.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        if (token == null || token.isBlank()) {
            response.put("message", "Authorization token is required.");

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

            if (!patient.getId().equals(id)) {
                response.put(
                        "message",
                        "You are not authorized to access these appointments."
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPatientId(id);

            List<AppointmentDTO> appointmentDTOs =
                    convertToAppointmentDTOList(appointments);

            response.put("appointments", appointmentDTOs);
            response.put(
                    "message",
                    "Appointments retrieved successfully."
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error retrieving patient appointments: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to retrieve appointments."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Filters appointments by condition.
     *
     * past   -> status 1
     * future -> status 0
     */
    public ResponseEntity<Map<String, Object>> filterByCondition(
            String condition,
            Long id
    ) {
        Map<String, Object> response = new HashMap<>();

        if (id == null) {
            response.put("message", "Patient ID is required.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        Integer status = getStatusFromCondition(condition);

        if (status == null) {
            response.put(
                    "message",
                    "Condition must be either past or future."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            List<Appointment> appointments =
                    appointmentRepository
                            .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                                    id,
                                    status
                            );

            response.put(
                    "appointments",
                    convertToAppointmentDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error filtering appointments by condition: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to filter appointments."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Filters patient appointments by doctor name.
     */
    public ResponseEntity<Map<String, Object>> filterByDoctor(
            String name,
            Long patientId
    ) {
        Map<String, Object> response = new HashMap<>();

        if (patientId == null) {
            response.put("message", "Patient ID is required.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            String doctorName = name == null ? "" : name.trim();

            List<Appointment> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientId(
                                    doctorName,
                                    patientId
                            );

            response.put(
                    "appointments",
                    convertToAppointmentDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error filtering appointments by doctor: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to filter appointments."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Filters appointments by doctor name and condition.
     */
    public ResponseEntity<Map<String, Object>>
    filterByDoctorAndCondition(
            String condition,
            String name,
            long patientId
    ) {
        Map<String, Object> response = new HashMap<>();

        Integer status = getStatusFromCondition(condition);

        if (status == null) {
            response.put(
                    "message",
                    "Condition must be either past or future."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            String doctorName = name == null ? "" : name.trim();

            List<Appointment> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientIdAndStatus(
                                    doctorName,
                                    patientId,
                                    status
                            );

            response.put(
                    "appointments",
                    convertToAppointmentDTOList(appointments)
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error filtering appointments: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to filter appointments."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Retrieves the logged-in patient's details using the email
     * stored in the token.
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(
            String token
    ) {
        Map<String, Object> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put("message", "Authorization token is required.");

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

            response.put("patient", patient);
            response.put(
                    "message",
                    "Patient details retrieved successfully."
            );

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error retrieving patient details: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to retrieve patient details."
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Converts an Appointment entity into an AppointmentDTO.
     */
    private AppointmentDTO convertToAppointmentDTO(
            Appointment appointment
    ) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }

    private List<AppointmentDTO> convertToAppointmentDTOList(
            List<Appointment> appointments
    ) {
        List<AppointmentDTO> appointmentDTOs = new ArrayList<>();

        if (appointments == null) {
            return appointmentDTOs;
        }

        for (Appointment appointment : appointments) {
            appointmentDTOs.add(
                    convertToAppointmentDTO(appointment)
            );
        }

        return appointmentDTOs;
    }

    private Integer getStatusFromCondition(String condition) {
        if (condition == null) {
            return null;
        }

        if ("past".equalsIgnoreCase(condition)) {
            return 1;
        }

        if ("future".equalsIgnoreCase(condition)) {
            return 0;
        }

        return null;
    }
}