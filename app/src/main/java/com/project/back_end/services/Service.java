package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.model.Admin;
import com.project.back_end.model.Appointment;
import com.project.back_end.model.Doctor;
import com.project.back_end.model.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            DoctorService doctorService,
            PatientService patientService) {

        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // Validate JWT token
    public ResponseEntity<?> validateToken(String token, String role) {

        try {

            if (tokenService.validateToken(token, role)) {
                return ResponseEntity.ok("Valid token");
            }

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        }
    }

    // Validate Admin Login
    public ResponseEntity<?> validateAdmin(String username, String password) {

        try {

            Admin admin = adminRepository.findByUsername(username);

            if (admin == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password");
            }

            if (!admin.getPassword().equals(password)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password");
            }

            String token = tokenService.generateToken(username);

            return ResponseEntity.ok(token);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    // Filter Doctors
    public ResponseEntity<?> filterDoctor(
            String name,
            String specialty,
            String time) {

        try {

            if ((name == null || name.isBlank())
                    && (specialty == null || specialty.isBlank())
                    && (time == null || time.isBlank())) {

                return ResponseEntity.ok(
                        doctorService.getDoctors()
                );
            }

            if (name != null && !name.isBlank()
                    && specialty != null && !specialty.isBlank()
                    && time != null && !time.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorsByNameSpecilityandTime(
                                name, specialty, time)
                );
            }

            if (name != null && !name.isBlank()
                    && specialty != null && !specialty.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorByNameAndSpecility(
                                name, specialty)
                );
            }

            if (name != null && !name.isBlank()
                    && time != null && !time.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorByNameAndTime(
                                name, time)
                );
            }

            if (specialty != null && !specialty.isBlank()
                    && time != null && !time.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorByTimeAndSpecility(
                                specialty, time)
                );
            }

            if (name != null && !name.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.findDoctorByName(name)
                );
            }

            if (specialty != null && !specialty.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorBySpecility(specialty)
                );
            }

            if (time != null && !time.isBlank()) {

                return ResponseEntity.ok(
                        doctorService.filterDoctorsByTime(time)
                );
            }

            return ResponseEntity.ok(
                    doctorService.getDoctors()
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to filter doctors");
        }
    }

    // Validate Appointment
    public int validateAppointment(
            Long doctorId,
            LocalDate date,
            LocalDateTime appointmentTime) {

        try {

            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElse(null);

            if (doctor == null) {
                return -1;
            }

            return doctorService.getDoctorAvailability(
                    doctorId,
                    date
            ).contains(appointmentTime.toLocalTime()) ? 1 : 0;

        } catch (Exception e) {

            return 0;
        }
    }

    // Validate Patient Registration
    public boolean validatePatient(
            String email,
            String phone) {

        try {

            Patient patient =
                    patientRepository.findByEmailOrPhone(email, phone);

            return patient == null;

        } catch (Exception e) {

            return false;
        }
    }

    // Validate Patient Login
    public ResponseEntity<?> validatePatientLogin(
            String email,
            String password) {

        try {

            Patient patient =
                    patientRepository.findByEmail(email);

            if (patient == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid email or password");
            }

            if (!patient.getPassword().equals(password)) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid email or password");
            }

            String token =
                    tokenService.generateToken(email);

            return ResponseEntity.ok(token);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error");
        }
    }

    // Filter Patient Appointments
    public ResponseEntity<?> filterPatient(
            String token,
            String condition,
            String doctorName) {

        try {

            String email =
                    tokenService.extractEmail(token);

            Patient patient =
                    patientRepository.findByEmail(email);

            if (patient == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Patient not found");
            }

            Long patientId = patient.getId();

            if (condition != null && !condition.isBlank()
                    && doctorName != null && !doctorName.isBlank()) {

                return ResponseEntity.ok(
                        patientService.filterByDoctorAndCondition(
                                patientId,
                                doctorName,
                                condition)
                );
            }

            if (condition != null && !condition.isBlank()) {

                return ResponseEntity.ok(
                        patientService.filterByCondition(
                                patientId,
                                condition)
                );
            }

            if (doctorName != null && !doctorName.isBlank()) {

                return ResponseEntity.ok(
                        patientService.filterByDoctor(
                                patientId,
                                doctorName)
                );
            }

            return ResponseEntity.ok(
                    patientService.getPatientAppointment(patientId)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to retrieve patient appointments");
        }
    }
}
