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

    @GetMapping("/{doctorId}/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String patientName,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "DOCTOR");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return ResponseEntity.ok(
                appointmentService.getAppointments(
                        doctorId,
                        date,
                        patientName));
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(
            @Valid @RequestBody Appointment appointment,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result =
                appointmentService.bookAppointment(appointment);

        if (result == 1) {
            return ResponseEntity.ok(
                    "Appointment booked successfully");
        }

        return ResponseEntity.badRequest()
                .body("Appointment could not be booked");
    }

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

        if (appointment.getPatient() == null ||
            appointment.getPatient().getId() == null) {

            return ResponseEntity.badRequest()
                    .body("Patient is required");
        }

        return ResponseEntity.ok(
                appointmentService.updateAppointment(
                        id,
                        appointment,
                        appointment.getPatient().getId()));
    }

    @DeleteMapping("/{id}/{patientId}/{token}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @PathVariable Long patientId,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return ResponseEntity.ok(
                appointmentService.cancelAppointment(
                        id,
                        patientId));
    }
}