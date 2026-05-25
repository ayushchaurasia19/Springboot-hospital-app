package com.hospital.hospital_api;

import com.hospital.hospital_api.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        // Clear the database before every test to ensure test isolation
        patientRepository.deleteAll();
    }

    @Test
    void testGetPatientsEmptyInitially() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAddPatientSuccess() throws Exception {
        String patientJson = """
                {
                    "name": "John Doe",
                    "age": 30,
                    "gender": "Male",
                    "timeSlot": "09:00 AM - 10:00 AM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Added Patient"));

        // Verify patient was actually added and can be retrieved
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].age", is(30)))
                .andExpect(jsonPath("$[0].gender", is("Male")))
                .andExpect(jsonPath("$[0].timeSlot", is("09:00 AM - 10:00 AM")));
    }

    @Test
    void testSearchPatientSuccess() throws Exception {
        // Add a patient first
        String patientJson = """
                {
                    "name": "Jane Smith",
                    "age": 28,
                    "gender": "Female",
                    "timeSlot": "10:00 AM - 11:00 AM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isOk());

        // Perform search
        mockMvc.perform(get("/patients/search/Jane Smith"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Jane Smith")))
                .andExpect(jsonPath("$.age", is(28)))
                .andExpect(jsonPath("$.gender", is("Female")))
                .andExpect(jsonPath("$.timeSlot", is("10:00 AM - 11:00 AM")));
    }

    @Test
    void testDeletePatientSuccess() throws Exception {
        // Add a patient first
        String patientJson = """
                {
                    "name": "Bob Builder",
                    "age": 40,
                    "gender": "Male",
                    "timeSlot": "11:00 AM - 12:00 PM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isOk());

        // Delete the patient
        mockMvc.perform(delete("/patients/delete/Bob Builder"))
                .andExpect(status().isOk())
                .andExpect(content().string("Patient deleted"));

        // Verify patient list is now empty
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==========================================
    // NEGATIVE TESTING & CORNER CASES
    // ==========================================

    @Test
    void testSearchPatientNotFound() throws Exception {
        // Search in an empty repository
        mockMvc.perform(get("/patients/search/Unknown Patient"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())));
    }

    @Test
    void testDeletePatientNotFound() throws Exception {
        // Try deleting a patient that does not exist
        mockMvc.perform(delete("/patients/delete/Unknown Patient"))
                .andExpect(status().isOk())
                .andExpect(content().string("Patient not found"));
    }

    // ==========================================
    // EDGE CASES & SPECIFIC LOGIC
    // ==========================================

    @Test
    void testDeletePatientCaseInsensitive() throws Exception {
        // Add patient with mixed casing
        String patientJson = """
                {
                    "name": "Alice Cooper",
                    "age": 50,
                    "gender": "Female",
                    "timeSlot": "02:00 PM - 03:00 PM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isOk());

        // Delete using lowercase name to test case insensitivity in delete endpoint
        mockMvc.perform(delete("/patients/delete/alice cooper"))
                .andExpect(status().isOk())
                .andExpect(content().string("Patient deleted"));
    }

    @Test
    void testSearchPatientCaseInsensitive() throws Exception {
        // Add patient
        String patientJson = """
                {
                    "name": "Alice Cooper",
                    "age": 50,
                    "gender": "Female",
                    "timeSlot": "02:00 PM - 03:00 PM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isOk());

        // Search with exact case (should match)
        mockMvc.perform(get("/patients/search/Alice Cooper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Cooper")));

        // Search with lowercase (should also match due to case insensitivity)
        mockMvc.perform(get("/patients/search/alice cooper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Cooper")));
    }

    @Test
    void testAddPatientWithNegativeAgeRejected() throws Exception {
        String patientJson = """
                {
                    "name": "Baby John",
                    "age": -1,
                    "gender": "Male",
                    "timeSlot": "03:00 PM - 04:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Age. Age must be between 1 and 150."));

        // Verify patient was not added
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAddPatientWithTooOldAgeRejected() throws Exception {
        String patientJson = """
                {
                    "name": "Ancient John",
                    "age": 151,
                    "gender": "Male",
                    "timeSlot": "03:00 PM - 04:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Age. Age must be between 1 and 150."));

        // Verify patient was not added
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAddPatientWithBoundaryAgesSuccess() throws Exception {
        // Test lower bound (age = 1)
        String patient1 = """
                {
                    "name": "One Year Old",
                    "age": 1,
                    "gender": "Male",
                    "timeSlot": "03:00 PM - 04:00 PM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patient1))
                .andExpect(status().isOk())
                .andExpect(content().string("Added Patient"));

        // Test upper bound (age = 150)
        String patient2 = """
                {
                    "name": "Super Centenarian",
                    "age": 150,
                    "gender": "Female",
                    "timeSlot": "04:00 PM - 05:00 PM"
                }
                """;
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patient2))
                .andExpect(status().isOk())
                .andExpect(content().string("Added Patient"));

        // Verify both boundary patients were added
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testAddPatientWithBlankNameRejected() throws Exception {
        String patientJson = """
                {
                    "name": "",
                    "age": 22,
                    "gender": "Male",
                    "timeSlot": "04:00 PM - 05:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Name. Name must not be blank and cannot contain numbers."));

        // Verify patient was not added
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAddPatientWithWhitespaceOnlyNameRejected() throws Exception {
        String patientJson = """
                {
                    "name": "    ",
                    "age": 22,
                    "gender": "Male",
                    "timeSlot": "04:00 PM - 05:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Name. Name must not be blank and cannot contain numbers."));
    }

    @Test
    void testAddPatientWithDigitsInNameRejected() throws Exception {
        String patientJson = """
                {
                    "name": "John123",
                    "age": 25,
                    "gender": "Male",
                    "timeSlot": "04:00 PM - 05:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Name. Name must not be blank and cannot contain numbers."));
    }

    @Test
    void testFailingSpecialCharName() throws Exception {
        // his test asserts that names with special characters are rejected (expects 400 Bad Request).
        String patientJson = """
                {
                    "name": "Jane_Doe!",
                    "age": 28,
                    "gender": "Female",
                    "timeSlot": "04:00 PM - 05:00 PM"
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest()); // Fail: actually returns 200 OK
    }

    @Test
    void testAddPatientWithBlankFieldsRejected() throws Exception {
        // Test when required fields (like name/age) are blank/invalid, expecting Bad Request
        String patientJson = """
                {
                    "name": "abc",
                    "age": 22,
                    "gender": "Male",
                    "timeSlot": ""
                }
                """;

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid Name")));

        // Verify patient was not added
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}