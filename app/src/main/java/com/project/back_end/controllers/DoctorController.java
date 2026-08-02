package com.project.back_end.controllers;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(
            DoctorService doctorService,
            Service service
    ) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, user);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> response = new HashMap<>();

            if (tokenResponse.getBody() != null) {
                response.putAll(tokenResponse.getBody());
            }

            return ResponseEntity
                    .status(tokenResponse.getStatusCode())
                    .body(response);
        }

        List<String> availability =
                doctorService.getDoctorAvailability(doctorId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("availability", availability);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> addDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "admin");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.saveDoctor(doctor);
        Map<String, String> response = new HashMap<>();

        if (result == 1) {
            response.put("message", "Doctor added to db");
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        if (result == -1) {
            response.put("message", "Doctor already exists");
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        response.put("message", "Some internal error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(
            @RequestBody Login login
    ) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @PathVariable String token,
            @RequestBody Doctor doctor
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "admin");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.updateDoctor(doctor);
        Map<String, String> response = new HashMap<>();

        if (result == 1) {
            response.put("message", "Doctor updated");
            return ResponseEntity.ok(response);
        }

        if (result == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        response.put("message", "Some internal error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token
    ) {
        ResponseEntity<Map<String, String>> tokenResponse =
                service.validateToken(token, "admin");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.deleteDoctor(id);
        Map<String, String> response = new HashMap<>();

        if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            return ResponseEntity.ok(response);
        }

        if (result == -1) {
            response.put("message", "Doctor not found with id");
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        response.put("message", "Some internal error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality
    ) {
        Map<String, Object> response = service.filterDoctor(
                normalizeFilter(name),
                normalizeFilter(speciality),
                normalizeFilter(time)
        );

        return ResponseEntity.ok(response);
    }

    private String normalizeFilter(String value) {
        if (
            value == null ||
            value.isBlank() ||
            "null".equalsIgnoreCase(value)
        ) {
            return "";
        }

        return value.trim();
    }
}