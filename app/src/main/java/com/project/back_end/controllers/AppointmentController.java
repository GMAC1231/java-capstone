package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(
            AppointmentService appointmentService,
            Service service
    ) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    /**
     * Retrieves appointments for a doctor by date and patient name.
     */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "doctor");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> response = new HashMap<>();

            response.putAll(tokenResponse.getBody() != null
                    ? tokenResponse.getBody()
                    : Map.of("message", "Invalid or expired token."));

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(response);
        }

        Map<String, Object> appointments =
                appointmentService.getAppointment(
                        patientName,
                        date,
                        token
                );

        return ResponseEntity.ok(appointments);
    }

    /**
     * Books a new appointment for an authenticated patient.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int validationResult =
                service.validateAppointment(appointment);

        Map<String, String> response = new HashMap<>();

        if (validationResult == -1) {
            response.put("message", "Doctor not found.");

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        if (validationResult == 0) {
            response.put(
                    "message",
                    "The selected appointment time is unavailable."
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        int bookingResult =
                appointmentService.bookAppointment(appointment);

        if (bookingResult == 1) {
            response.put(
                    "message",
                    "Appointment booked successfully."
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        response.put(
                "message",
                "Unable to book appointment."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Updates an appointment for an authenticated patient.
     */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return appointmentService.updateAppointment(appointment);
    }

    /**
     * Cancels an appointment for an authenticated patient.
     */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "patient");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return appointmentService.cancelAppointment(id, token);
    }
}