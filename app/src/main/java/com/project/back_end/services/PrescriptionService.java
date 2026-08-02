package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repositories.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository
    ) {
        this.prescriptionRepository = prescriptionRepository;
    }

    /**
     * Saves a new prescription in MongoDB.
     */
    public ResponseEntity<Map<String, String>> savePrescription(
            Prescription prescription
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            prescriptionRepository.save(prescription);

            response.put("message", "Prescription saved");

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error saving prescription: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to save prescription"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    /**
     * Retrieves prescriptions linked to an appointment.
     */
    public ResponseEntity<Map<String, Object>> getPrescription(
            Long appointmentId
    ) {
        Map<String, Object> response = new HashMap<>();

        if (appointmentId == null) {
            response.put(
                    "message",
                    "Appointment ID is required"
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            List<Prescription> prescriptions =
                    prescriptionRepository.findByAppointmentId(
                            appointmentId
                    );

            response.put("prescription", prescriptions);

            return ResponseEntity.ok(response);

        } catch (Exception exception) {
            System.err.println(
                    "Error retrieving prescription: " +
                    exception.getMessage()
            );

            response.put(
                    "message",
                    "Unable to retrieve prescription"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}