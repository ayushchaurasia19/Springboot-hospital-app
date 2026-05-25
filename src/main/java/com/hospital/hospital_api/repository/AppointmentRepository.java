package com.hospital.hospital_api.repository;

import com.hospital.hospital_api.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find scheduled appointments to prevent double-booking a doctor
    boolean existsByDoctorIdAndTimeSlotAndStatus(Long doctorId, String timeSlot, String status);

    // Find scheduled appointments to prevent a patient from overlapping bookings
    boolean existsByPatientIdAndTimeSlotAndStatus(Long patientId, String timeSlot, String status);

    List<Appointment> findAllByOrderByTimeSlotAsc();
}
