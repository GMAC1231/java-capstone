package com.project.back_end.repositories;

import com.project.back_end.models.Appointment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    @Query("""
        SELECT DISTINCT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        LEFT JOIN FETCH d.availableTimes
        LEFT JOIN FETCH a.patient p
        WHERE d.id = :doctorId
          AND a.appointmentTime BETWEEN :start AND :end
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT DISTINCT a
        FROM Appointment a
        LEFT JOIN FETCH a.patient p
        LEFT JOIN FETCH a.doctor d
        LEFT JOIN FETCH d.availableTimes
        WHERE d.id = :doctorId
          AND LOWER(p.name) LIKE LOWER(
              CONCAT('%', :patientName, '%')
          )
          AND a.appointmentTime BETWEEN :start AND :end
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment>
    findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Appointment a
        WHERE a.doctor.id = :doctorId
        """)
    void deleteAllByDoctorId(
            @Param("doctorId") Long doctorId
    );

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment>
    findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
            Long patientId,
            int status
    );

    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.patient p
        WHERE p.id = :patientId
          AND LOWER(d.name) LIKE LOWER(
              CONCAT('%', :doctorName, '%')
          )
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId
    );

    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.patient p
        WHERE p.id = :patientId
          AND a.status = :status
          AND LOWER(d.name) LIKE LOWER(
              CONCAT('%', :doctorName, '%')
          )
        ORDER BY a.appointmentTime ASC
        """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status
    );
}