package com.hospital.hospital_api.Controller;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.DeleteExchange;

import com.hospital.hospital_api.model.Patient;

@RestController
public class PatientController {
    private ArrayList<Patient> patientList = new ArrayList<>();
    
    @GetMapping("/patients") // handles GET /patients
    public ArrayList<Patient> getPatient(){
        return patientList;
    }

    @PostMapping("/patients") // handles POST /pateints
    public String addPateint(@RequestBody Patient p){ // incoming JSON to java obj
        patientList.add(p);
        return "Added Patient";
    }

    @GetMapping("/patients/search/{name}")
    public Patient searchPatient(@PathVariable String name){ // extract value from URL
        for(Patient p : patientList){
            if(p.getName().equals(name)){
                return p;
            }
        }
        return null;
    }

    @DeleteMapping("/patients/delete/{name}")
    public String deletePatient(@PathVariable String name){
        
        for(int i = 0; i < patientList.size(); i++){
            if(patientList.get(i).getName().equalsIgnoreCase(name)){
                patientList.remove(i);
                return "Patient deleted";
            }
        }

        return "Patient not found";
    }
}
