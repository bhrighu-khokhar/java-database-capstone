package com.project.back_end.controllers;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(
            AppointmentService appointmentService,
            Service service) {

        this.appointmentService = appointmentService;
        this.service = service;
    }

    // Get appointments for doctor
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "DOCTOR");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return appointmentService.getAppointments(
                date,
                patientName,
                token
        );
    }

    // Book appointment
    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = appointmentService.bookAppointment(appointment);

        if (result == 1) {
            return ResponseEntity.ok(
                    "Appointment booked successfully");
        }

        if (result == -1) {
            return ResponseEntity.badRequest()
                    .body("Invalid doctor or appointment time");
        }

        return ResponseEntity.badRequest()
                .body("Appointment could not be booked");
    }

    // Update appointment
    @PutMapping("/{id}/{token}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        Long appointmentId = appointment.getId();

        if (appointmentId == null) {
            appointmentId = id;
        }

        return appointmentService.updateAppointment(
                appointmentId,
                appointment,
                appointment.getPatient().getId()
        );
    }

    // Cancel appointment
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return appointmentService.cancelAppointment(id);
    }
}
