package com.hospital.hospital_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.hospital_api.dto.AppointmentRequest;
import com.hospital.hospital_api.dto.LoginRequest;
import com.hospital.hospital_api.dto.RegisterRequest;
import com.hospital.hospital_api.model.Doctor;
import com.hospital.hospital_api.model.Patient;
import com.hospital.hospital_api.repository.AppointmentRepository;
import com.hospital.hospital_api.repository.DoctorRepository;
import com.hospital.hospital_api.repository.PatientRepository;
import com.hospital.hospital_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorAppointmentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        doctorRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUserRegistrationAndLoginSuccess() throws Exception {
        RegisterRequest register = new RegisterRequest("john_receptionist", "secure123", "RECEPTIONIST");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username", is("john_receptionist")))
                .andExpect(jsonPath("$.role", is("RECEPTIONIST")));

        LoginRequest login = new LoginRequest("john_receptionist", "secure123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username", is("john_receptionist")))
                .andExpect(jsonPath("$.role", is("RECEPTIONIST")));
    }

    @Test
    void testLoginWithInvalidCredentialsRejected() throws Exception {
        LoginRequest login = new LoginRequest("non_existent", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAnonymousAccessToProtectedEndpointsBlocked() throws Exception {
        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddAndGetDoctorSuccess() throws Exception {
        Doctor doctor = new Doctor("Dr. Stephen Strange", "Neurosurgeon", "09:00 AM - 10:00 AM, 02:00 PM - 03:00 PM");

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Added Doctor successfully")));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Dr. Stephen Strange")))
                .andExpect(jsonPath("$[0].specialization", is("Neurosurgeon")));
    }

    @Test
    @WithMockUser(username = "receptionist", roles = {"RECEPTIONIST"})
    void testBookAppointmentSuccessAndDoubleBookingPrevention() throws Exception {
        // Save doctor
        Doctor doctor = doctorRepository.save(new Doctor("Dr. Gregory House", "Diagnostics", "09:00 AM - 10:00 AM, 10:00 AM - 11:00 AM"));

        // Save two patients
        Patient patient1 = patientRepository.save(new Patient("Tony Stark", 45, "Male", "09:00 AM - 10:00 AM"));
        Patient patient2 = patientRepository.save(new Patient("Steve Rogers", 100, "Male", "09:00 AM - 10:00 AM"));

        // Book patient 1 successfully
        AppointmentRequest req1 = new AppointmentRequest(patient1.getId(), doctor.getId(), "09:00 AM - 10:00 AM");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Appointment booked successfully")));

        // Attempting to double-book doctor for same slot with patient 2 should fail
        AppointmentRequest req2 = new AppointmentRequest(patient2.getId(), doctor.getId(), "09:00 AM - 10:00 AM");

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Doctor is already booked for this time slot.")));
    }
}
