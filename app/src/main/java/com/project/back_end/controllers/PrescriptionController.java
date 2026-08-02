package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final Service service;

    public PrescriptionController(
            PrescriptionService prescriptionService,
            Service service
    ) {
        this.prescriptionService = prescriptionService;
        this.service = service;
    }

    /**
     * Saves a prescription for an authenticated doctor.
     */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(
            @PathVariable String token,
            @Valid @RequestBody Prescription prescription
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "doctor");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return prescriptionService.savePrescription(prescription);
    }

    /**
     * Retrieves prescriptions linked to an appointment.
     */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "doctor");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> errorResponse = new HashMap<>();

            if (tokenResponse.getBody() != null) {
                errorResponse.putAll(tokenResponse.getBody());
            } else {
                errorResponse.put(
                        "message",
                        "Invalid or expired token."
                );
            }

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(errorResponse);
        }

        return prescriptionService.getPrescription(appointmentId);
    }
}