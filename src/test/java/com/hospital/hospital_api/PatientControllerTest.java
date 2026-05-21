package com.hospital.hospital_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // fake HTTP requests for testing
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetPatients() throws Exception {

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk());
    }
}