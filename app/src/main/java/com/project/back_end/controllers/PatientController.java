package com.project.back_end.controllers;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints used by patients for registration, login,
 * appointment retrieval, and appointment filtering.
 *
 * Both /patient and /api/patient are supported so the endpoints
 * work with the assignment curl commands and the existing frontend.
 */
@RestController
@RequestMapping({"/patient", "${api.path}patient"})
public class PatientController {

    private final Service service;
    private final PatientService patientService;

    public PatientController(
            Service service,
            PatientService patientService
    ) {
        this.service = service;
        this.patientService = patientService;
    }

    /**
     * Registers a new patient after checking duplicate email/phone.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> registerPatient(
            @Valid @RequestBody Patient patient
    ) {
        Map<String, String> response = new HashMap<>();

        if (!service.validatePatient(patient)) {
            response.put(
                    "message",
                    "A patient with the same email or phone already exists."
            );
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        int result = patientService.createPatient(patient);

        if (result == 1) {
            response.put("message", "Patient registered successfully.");
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        response.put("message", "Unable to register patient.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Logs in a patient.
     *
     * Accepts either:
     * {"email":"jane.doe@example.com","password":"passJane1"}
     * or:
     * {"identifier":"jane.doe@example.com","password":"passJane1"}
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> patientLogin(
            @RequestBody Map<String, String> request
    ) {
        String identifier = request.get("identifier");

        if (identifier == null || identifier.isBlank()) {
            identifier = request.get("email");
        }

        Login login = new Login(
                identifier,
                request.get("password")
        );

        return service.validatePatientLogin(login);
    }

    /**
     * Returns every appointment belonging to the authenticated patient.
     * The patient ID in the URL must match the patient represented by
     * the supplied token.
     */
    @GetMapping("/appointments/{patientId}/patient/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long patientId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> error = new HashMap<>();

            if (tokenResponse.getBody() != null) {
                error.putAll(tokenResponse.getBody());
            } else {
                error.put("message", "Invalid or expired token.");
            }

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(error);
        }

        return patientService.getPatientAppointment(patientId, token);
    }

    /**
     * Filters the logged-in patient's appointments.
     * Examples:
     * /patient/appointments/filter?condition=future&name=Ahmed&token=...
     * /patient/appointments/filter?name=Ahmed&token=...
     */
    @GetMapping("/appointments/filter")
    public ResponseEntity<Map<String, Object>> filterAppointments(
            @RequestParam(defaultValue = "") String condition,
            @RequestParam(defaultValue = "") String name,
            @RequestParam String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> error = new HashMap<>();

            if (tokenResponse.getBody() != null) {
                error.putAll(tokenResponse.getBody());
            }

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(error);
        }

        return service.filterPatient(condition, name, token);
    }

    /**
     * Returns the logged-in patient's profile details.
     */
    @GetMapping("/details/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> error = new HashMap<>();

            if (tokenResponse.getBody() != null) {
                error.putAll(tokenResponse.getBody());
            }

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(error);
        }

        return patientService.getPatientDetails(token);
    }
}
