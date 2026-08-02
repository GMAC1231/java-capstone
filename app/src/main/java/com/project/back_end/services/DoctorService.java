package com.project.back_end.services;

import com.project.back_end.dto.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repositories.AppointmentRepository;
import com.project.back_end.repositories.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService
    ) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /**
     * Returns the doctor's available time slots after removing
     * slots that are already booked on the selected date.
     */
    public List<String> getDoctorAvailability(
            Long doctorId,
            LocalDate date
    ) {
        Optional<Doctor> doctorOptional =
                doctorRepository.findById(doctorId);

        if (doctorOptional.isEmpty()) {
            return new ArrayList<>();
        }

        if (date == null) {
            date = LocalDate.now();
        }

        Doctor doctor = doctorOptional.get();

        List<String> availableTimes =
                doctor.getAvailableTimes() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(doctor.getAvailableTimes());

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end =
                date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(
                                doctorId,
                                start,
                                end
                        );

        return availableTimes.stream()
                .filter(slot -> !isSlotBooked(slot, appointments))
                .collect(Collectors.toList());
    }

    /**
     * Saves a new doctor.
     *
     * @return 1 for success, -1 if email already exists,
     *         0 for internal error.
     */
    public int saveDoctor(Doctor doctor) {
        try {
            if (
                doctor == null ||
                doctor.getId() == null ||
                doctor.getEmail().isBlank()
            ) {
                return 0;
            }

            Doctor existingDoctor =
                    doctorRepository.findByEmail(
                            doctor.getEmail().trim()
                    );

            if (existingDoctor != null) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;

        } catch (Exception exception) {
            System.err.println(
                    "Error saving doctor: " +
                    exception.getMessage()
            );
            return 0;
        }
    }

    /**
     * Updates an existing doctor.
     *
     * @return 1 for success, -1 if doctor is not found,
     *         0 for internal error.
     */
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getId() == null) {
                return -1;
            }

            Optional<Doctor> existingOptional =
                    doctorRepository.findById(doctor.getId());

            if (existingOptional.isEmpty()) {
                return -1;
            }

            Doctor existingDoctor = existingOptional.get();

            existingDoctor.setName(doctor.getName());
            existingDoctor.setSpecialty(doctor.getSpecialty());
            existingDoctor.setEmail(doctor.getEmail());
            existingDoctor.setPhone(doctor.getPhone());
            existingDoctor.setAvailableTimes(
                    doctor.getAvailableTimes()
            );

            if (
                doctor.getPassword() != null &&
                !doctor.getPassword().isBlank()
            ) {
                existingDoctor.setPassword(
                        doctor.getPassword()
                );
            }

            doctorRepository.save(existingDoctor);
            return 1;

        } catch (Exception exception) {
            System.err.println(
                    "Error updating doctor: " +
                    exception.getMessage()
            );
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        try {
            return doctorRepository.findAll();
        } catch (Exception exception) {
            System.err.println(
                    "Error retrieving doctors: " +
                    exception.getMessage()
            );
            return new ArrayList<>();
        }
    }

    /**
     * Deletes appointments linked to the doctor before deleting
     * the doctor record.
     *
     * @return 1 for success, -1 if doctor is not found,
     *         0 for internal error.
     */
    public int deleteDoctor(long id) {
        try {
            Optional<Doctor> doctorOptional =
                    doctorRepository.findById(id);

            if (doctorOptional.isEmpty()) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.delete(doctorOptional.get());

            return 1;

        } catch (Exception exception) {
            System.err.println(
                    "Error deleting doctor: " +
                    exception.getMessage()
            );
            return 0;
        }
    }

    /**
     * Validates doctor login credentials and returns a token.
     */
    public ResponseEntity<Map<String, String>> validateDoctor(
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

        Doctor doctor = doctorRepository.findByEmail(
                login.getIdentifier().trim()
        );

        if (
            doctor == null ||
            !doctor.getPassword().equals(login.getPassword())
        ) {
            response.put(
                    "message",
                    "Invalid credentials."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        /*
         * Change generateToken(...) if your TokenService uses
         * a different method name or parameter order.
         */
        String token = tokenService.generateToken(
                doctor.getEmail(),
                "doctor"
        );

        response.put("token", token);
        response.put(
                "message",
                "Doctor login successful."
        );

        return ResponseEntity.ok(response);
    }

    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors;

        if (name == null || name.isBlank()) {
            doctors = doctorRepository.findAll();
        } else {
            doctors = doctorRepository.findByNameLike(
                    name.trim()
            );
        }

        response.put("doctors", doctors);
        return response;
    }

    public Map<String, Object>
    filterDoctorsByNameSpecilityandTime(
            String name,
            String specialty,
            String amOrPm
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors =
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                safeString(name),
                                safeString(specialty)
                        );

        response.put(
                "doctors",
                filterDoctorByTime(doctors, amOrPm)
        );

        return response;
    }

    public Map<String, Object> filterDoctorByNameAndTime(
            String name,
            String amOrPm
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors;

        if (name == null || name.isBlank()) {
            doctors = doctorRepository.findAll();
        } else {
            doctors = doctorRepository.findByNameLike(
                    name.trim()
            );
        }

        response.put(
                "doctors",
                filterDoctorByTime(doctors, amOrPm)
        );

        return response;
    }

    public Map<String, Object> filterDoctorByNameAndSpecility(
            String name,
            String specilty
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors =
                doctorRepository
                        .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                                safeString(name),
                                safeString(specilty)
                        );

        response.put("doctors", doctors);
        return response;
    }

    public Map<String, Object> filterDoctorByTimeAndSpecility(
            String specilty,
            String amOrPm
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(
                        safeString(specilty)
                );

        response.put(
                "doctors",
                filterDoctorByTime(doctors, amOrPm)
        );

        return response;
    }

    public Map<String, Object> filterDoctorBySpecility(
            String specilty
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(
                        safeString(specilty)
                );

        response.put("doctors", doctors);
        return response;
    }

    public Map<String, Object> filterDoctorsByTime(
            String amOrPm
    ) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> doctors = doctorRepository.findAll();

        response.put(
                "doctors",
                filterDoctorByTime(doctors, amOrPm)
        );

        return response;
    }

    /**
     * Filters doctors based on whether at least one starting time
     * falls in the requested AM or PM period.
     */
    private List<Doctor> filterDoctorByTime(
            List<Doctor> doctors,
            String amOrPm
    ) {
        if (doctors == null) {
            return new ArrayList<>();
        }

        if (amOrPm == null || amOrPm.isBlank()) {
            return doctors;
        }

        String period = amOrPm.trim().toUpperCase();

        return doctors.stream()
                .filter(doctor -> {
                    List<String> times =
                            doctor.getAvailableTimes();

                    if (times == null || times.isEmpty()) {
                        return false;
                    }

                    return times.stream()
                            .anyMatch(slot ->
                                    matchesTimePeriod(
                                            slot,
                                            period
                                    )
                            );
                })
                .collect(Collectors.toList());
    }

    private boolean matchesTimePeriod(
            String slot,
            String period
    ) {
        LocalTime startTime = extractStartTime(slot);

        if (startTime == null) {
            return false;
        }

        if ("AM".equals(period)) {
            return startTime.isBefore(LocalTime.NOON);
        }

        if ("PM".equals(period)) {
            return !startTime.isBefore(LocalTime.NOON);
        }

        return true;
    }

    private boolean isSlotBooked(
            String slot,
            List<Appointment> appointments
    ) {
        LocalTime slotStart = extractStartTime(slot);

        if (slotStart == null) {
            return false;
        }

        return appointments.stream()
                .anyMatch(appointment ->
                        appointment.getAppointmentTime() != null &&
                        appointment
                                .getAppointmentTime()
                                .toLocalTime()
                                .equals(slotStart)
                );
    }

    /**
     * Supports slots such as:
     * 09:00 - 10:00
     * 09:00-10:00
     * 09:00 AM - 10:00 AM
     */
    private LocalTime extractStartTime(String slot) {
        if (slot == null || slot.isBlank()) {
            return null;
        }

        String startPart = slot.split("-")[0].trim();

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(startPart, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported time format.
            }
        }

        return null;
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }
}