package com.hospital.hospital_api.exception;

public class InvalidDoctorException extends RuntimeException {
    public InvalidDoctorException(String message) {
        super(message);
    }
}
