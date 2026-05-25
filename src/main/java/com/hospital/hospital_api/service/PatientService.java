package com.hospital.hospital_api.service;

import com.hospital.hospital_api.exception.InvalidPatientException;
import com.hospital.hospital_api.model.Patient;
import com.hospital.hospital_api.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient addPatient(Patient patient) {
        if (patient.getName() == null || patient.getName().trim().isEmpty() || patient.getName().matches(".*\\d.*")) {
            throw new InvalidPatientException("Invalid Name. Name must not be blank and cannot contain numbers.");
        }
        if (patient.getAge() < 1 || patient.getAge() > 150) {
            throw new InvalidPatientException("Invalid Age. Age must be between 1 and 150.");
        }
        return patientRepository.save(patient);
    }

    public Optional<Patient> searchByName(String name) {
        return patientRepository.findByNameIgnoreCase(name);
    }

    @Transactional
    public boolean deleteByName(String name) {
        Optional<Patient> patient = patientRepository.findByNameIgnoreCase(name);
        if (patient.isPresent()) {
            patientRepository.delete(patient.get());
            return true;
        }
        return false;
    }
}
