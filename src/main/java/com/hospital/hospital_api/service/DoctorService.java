package com.hospital.hospital_api.service;

import com.hospital.hospital_api.exception.InvalidDoctorException;
import com.hospital.hospital_api.model.Doctor;
import com.hospital.hospital_api.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Doctor addDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().trim().isEmpty() || doctor.getName().matches(".*\\d.*")) {
            throw new InvalidDoctorException("Invalid Name. Name must not be blank and cannot contain numbers.");
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new InvalidDoctorException("Specialization must not be blank.");
        }
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new InvalidDoctorException("Doctor not found with ID: " + id);
        }
        doctorRepository.deleteById(id);
    }
}
