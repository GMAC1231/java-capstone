package com.project.back_end.repositories;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository
        extends MongoRepository<Prescription, String> {

    List<Prescription> findByAppointmentId(Long appointmentId);
}