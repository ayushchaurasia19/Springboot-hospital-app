package com.hospital.hospital_api.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.hospital_api.model.Patient;
import com.hospital.hospital_api.service.PatientService;

@RestController
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients") // handles GET /patients
    public List<Patient> getPatient() {
        return patientService.getAllPatients();
    }

    @PostMapping("/patients") // handles POST /patients
    public ResponseEntity<String> addPatient(@RequestBody Patient p) { // incoming JSON to java obj
        patientService.addPatient(p);
        return ResponseEntity.ok("Added Patient");
    }

    @GetMapping("/patients/search/{name}")
    public Patient searchPatient(@PathVariable String name) { // extract value from URL
        return patientService.searchByName(name).orElse(null);
    }

    @DeleteMapping("/patients/delete/{name}")
    public String deletePatient(@PathVariable String name) {
        boolean deleted = patientService.deleteByName(name);
        return deleted ? "Patient deleted" : "Patient not found";
    }
}
