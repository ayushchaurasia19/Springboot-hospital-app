package com.hospital.hospital_api.Controller;

import com.hospital.hospital_api.dto.AppointmentRequest;
import com.hospital.hospital_api.model.Appointment;
import com.hospital.hospital_api.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @PostMapping
    public ResponseEntity<String> bookAppointment(@RequestBody AppointmentRequest request) {
        appointmentService.bookAppointment(request.getPatientId(), request.getDoctorId(), request.getTimeSlot());
        return ResponseEntity.ok("Appointment booked successfully");
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok("Appointment cancelled successfully");
    }
}
