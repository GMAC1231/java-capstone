package com.project.back_end.repositories;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database operations for Patient entities.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Retrieves one patient by their unique email address.
     *
     * @param email patient email address
     * @return matching patient, or null when no match exists
     */
    Patient findByEmail(String email);

    /**
     * Retrieves a patient when either the email or phone matches.
     * This method is used to prevent duplicate registrations.
     *
     * @param email patient email address
     * @param phone patient phone number
     * @return matching patient, or null when no match exists
     */
    Patient findByEmailOrPhone(String email, String phone);
}
