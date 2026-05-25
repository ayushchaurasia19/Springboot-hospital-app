package com.hospital.hospital_api.dto;

public class AppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private String timeSlot;

    public AppointmentRequest() {
    }

    public AppointmentRequest(Long patientId, Long doctorId, String timeSlot) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.timeSlot = timeSlot;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
}
