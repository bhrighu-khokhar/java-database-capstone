package com.project.back_end.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(
            PatientService patientService,
            Service service) {

        this.patientService = patientService;
        this.service = service;
    }

    // Get patient details
    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return patientService.getPatientDetails(token);
    }

    // Create patient
    @PostMapping
    public ResponseEntity<?> createPatient(
            @Valid @RequestBody Patient patient) {

        boolean valid = service.validatePatient(
                patient.getEmail(),
                patient.getPhone()
        );

        if (!valid) {
            return ResponseEntity
                    .badRequest()
                    .body("Patient with this email or phone already exists");
        }

        int result = patientService.createPatient(patient);

        if (result == 1) {
            return ResponseEntity
                    .ok("Patient registered successfully");
        }

        return ResponseEntity
                .internalServerError()
                .body("Unable to create patient");
    }

    // Patient login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody Login login) {

        return service.validatePatientLogin(
                login.getEmail(),
                login.getPassword()
        );
    }

    // Get patient appointments
    @GetMapping("/appointments/{patientId}/{user}/{token}")
    public ResponseEntity<?> getPatientAppointment(
            @PathVariable Long patientId,
            @PathVariable String user,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, user);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return ResponseEntity.ok(
                patientService.getPatientAppointment(patientId)
        );
    }

    // Filter patient appointments
    @GetMapping("/appointments/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "PATIENT");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return service.filterPatient(
                token,
                condition,
                name
        );
    }
}
