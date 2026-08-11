package com.project.back_end.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Doctor doctor;

    @ManyToOne
    @NotNull
    private Patient patient;

    @NotNull
    @Future
    private LocalDateTime appointmentTime;

    @NotNull
    private int status;

    // No-argument constructor required by JPA
    public Appointment() {
    }

    // Parameterized constructor
    public Appointment(Doctor doctor, Patient patient,
                       LocalDateTime appointmentTime, int status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    // Getter and Setter for id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Getter and Setter for doctor
    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    // Getter and Setter for patient
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    // Getter and Setter for appointmentTime
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    // Getter and Setter for status
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // Calculates the appointment end time
    @Transient
    public LocalDateTime getEndTime() {
        if (appointmentTime == null) {
            return null;
        }

        return appointmentTime.plusHours(1);
    }

    // Returns only the date of the appointment
    @Transient
    public LocalDate getAppointmentDate() {
        if (appointmentTime == null) {
            return null;
        }

        return appointmentTime.toLocalDate();
    }

    // Returns only the time of the appointment
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        if (appointmentTime == null) {
            return null;
        }

        return appointmentTime.toLocalTime();
    }
}
