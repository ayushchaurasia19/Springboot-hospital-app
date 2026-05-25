package com.hospital.hospital_api.repository;

import com.hospital.hospital_api.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
