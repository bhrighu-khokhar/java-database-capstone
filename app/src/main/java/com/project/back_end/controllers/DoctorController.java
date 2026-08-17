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

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(
            DoctorService doctorService,
            Service service) {

        this.doctorService = doctorService;
        this.service = service;
    }

    // Get doctor availability
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable LocalDate date,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, user);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        return ResponseEntity.ok(
                doctorService.getDoctorAvailability(
                        doctorId,
                        date
                )
        );
    }

    // Get all doctors
    @GetMapping
    public ResponseEntity<?> getDoctor() {

        return ResponseEntity.ok(
                doctorService.getDoctors()
        );
    }

    // Save doctor
    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "ADMIN");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.saveDoctor(doctor);

        if (result == 1) {
            return ResponseEntity.ok(
                    "Doctor added successfully"
            );
        }

        if (result == -1) {
            return ResponseEntity.status(409)
                    .body("Doctor with this email already exists");
        }

        return ResponseEntity.internalServerError()
                .body("Unable to add doctor");
    }

    // Doctor login
    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(
            @Valid @RequestBody Login login) {

        String token = doctorService.validateDoctor(
                login.getEmail(),
                login.getPassword()
        );

        if ("Invalid email or password".equals(token)) {
            return ResponseEntity.status(401)
                    .body(token);
        }

        return ResponseEntity.ok(token);
    }

    // Update doctor
    @PutMapping("/{id}/{token}")
    public ResponseEntity<?> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "ADMIN");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.updateDoctor(id, doctor);

        if (result == 1) {
            return ResponseEntity.ok(
                    "Doctor updated successfully"
            );
        }

        if (result == -1) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.internalServerError()
                .body("Unable to update doctor");
    }

    // Delete doctor
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {

        ResponseEntity<?> tokenResponse =
                service.validateToken(token, "ADMIN");

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return tokenResponse;
        }

        int result = doctorService.deleteDoctor(id);

        if (result == 1) {
            return ResponseEntity.ok(
                    "Doctor deleted successfully"
            );
        }

        if (result == -1) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.internalServerError()
                .body("Unable to delete doctor");
    }

    // Filter doctors
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<?> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {

        return service.filterDoctor(
                name,
                speciality,
                time
        );
    }
}