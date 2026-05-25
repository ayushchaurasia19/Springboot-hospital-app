package com.hospital.hospital_api.repository;

import com.hospital.hospital_api.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByNameIgnoreCase(String name);
    long deleteByNameIgnoreCase(String name);
}
