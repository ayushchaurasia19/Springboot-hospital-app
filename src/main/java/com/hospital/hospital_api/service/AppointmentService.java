package com.hospital.hospital_api.service;

import com.hospital.hospital_api.exception.InvalidAppointmentException;
import com.hospital.hospital_api.model.Appointment;
import com.hospital.hospital_api.model.Doctor;
import com.hospital.hospital_api.model.Patient;
import com.hospital.hospital_api.repository.AppointmentRepository;
import com.hospital.hospital_api.repository.DoctorRepository;
import com.hospital.hospital_api.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderByTimeSlotAsc();
    }

    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, String timeSlot) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new InvalidAppointmentException("Patient not found with ID: " + patientId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new InvalidAppointmentException("Doctor not found with ID: " + doctorId));

        // 1. Check if the slot is in the doctor's available slots
        if (doctor.getAvailableSlots() == null) {
            throw new InvalidAppointmentException("Doctor has no available time slots.");
        }
        boolean slotAvailable = Arrays.stream(doctor.getAvailableSlots().split(","))
                .map(String::trim)
                .anyMatch(slot -> slot.equalsIgnoreCase(timeSlot.trim()));

        if (!slotAvailable) {
            throw new InvalidAppointmentException("Time slot is not available for this doctor.");
        }

        // 2. Check if doctor is already booked for this slot
        if (appointmentRepository.existsByDoctorIdAndTimeSlotAndStatus(doctorId, timeSlot, "SCHEDULED")) {
            throw new InvalidAppointmentException("Doctor is already booked for this time slot.");
        }

        // 3. Check if patient already has a booking at this time
        if (appointmentRepository.existsByPatientIdAndTimeSlotAndStatus(patientId, timeSlot, "SCHEDULED")) {
            throw new InvalidAppointmentException("Patient already has an appointment scheduled at this time slot.");
        }

        Appointment appointment = new Appointment(patient, doctor, timeSlot, "SCHEDULED", LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentException("Appointment not found with ID: " + id));

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }
}
