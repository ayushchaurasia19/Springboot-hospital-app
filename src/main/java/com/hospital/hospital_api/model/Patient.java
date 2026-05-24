package com.hospital.hospital_api.model;

public class Patient {

    private String name;
    private int age;
    private String gender;
    private String timeSlot;

    public Patient() {
    }

    public Patient(String name, int age, String gender, String timeSlot) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.timeSlot = timeSlot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
}