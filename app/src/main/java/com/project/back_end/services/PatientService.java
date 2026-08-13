package com.project.back_end.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    // Constructor injection
    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {

        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Create Patient
    @Transactional
    public int createPatient(Patient patient) {

        try {

            patientRepository.save(patient);

            return 1;

        } catch (Exception e) {

            e.printStackTrace();

            return 0;
        }
    }

    // Get all appointments of a patient
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPatientAppointment(Long patientId) {

        try {

            List<Appointment> appointments =
                    appointmentRepository.findByPatientId(patientId);

            List<AppointmentDTO> dtoList =
                    appointments.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving appointments");
        }
    }

    // Filter appointments by condition
    // future = status 0
    // past = status 1
    @Transactional(readOnly = true)
    public ResponseEntity<?> filterByCondition(
            Long patientId,
            String condition) {

        try {

            List<Appointment> appointments;

            if ("future".equalsIgnoreCase(condition)) {

                appointments =
                        appointmentRepository
                                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                                        patientId, 0);

            } else if ("past".equalsIgnoreCase(condition)) {

                appointments =
                        appointmentRepository
                                .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                                        patientId, 1);

            } else {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid condition. Use 'past' or 'future'.");
            }

            List<AppointmentDTO> dtoList =
                    appointments.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering appointments");
        }
    }

    // Filter appointments by doctor
    @Transactional(readOnly = true)
    public ResponseEntity<?> filterByDoctor(
            Long patientId,
            String doctorName) {

        try {

            List<Appointment> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientId(
                                    doctorName,
                                    patientId);

            List<AppointmentDTO> dtoList =
                    appointments.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering appointments");
        }
    }

    // Filter appointments by doctor and condition
    @Transactional(readOnly = true)
    public ResponseEntity<?> filterByDoctorAndCondition(
            Long patientId,
            String doctorName,
            String condition) {

        try {

            List<Appointment> appointments;

            if ("future".equalsIgnoreCase(condition)) {

                appointments =
                        appointmentRepository
                                .filterByDoctorNameAndPatientIdAndStatus(
                                        doctorName,
                                        patientId,
                                        0);

            } else if ("past".equalsIgnoreCase(condition)) {

                appointments =
                        appointmentRepository
                                .filterByDoctorNameAndPatientIdAndStatus(
                                        doctorName,
                                        patientId,
                                        1);

            } else {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid condition. Use 'past' or 'future'.");
            }

            List<AppointmentDTO> dtoList =
                    appointments.stream()
                            .map(this::convertToDTO)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering appointments");
        }
    }

    // Get patient details using JWT token
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPatientDetails(String token) {

        try {

            String email = tokenService.extractEmail(token);

            Patient patient =
                    patientRepository.findByEmail(email);

            if (patient == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Patient not found");
            }

            return ResponseEntity.ok(patient);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient details");
        }
    }

    // Convert Appointment entity into AppointmentDTO
    private AppointmentDTO convertToDTO(Appointment appointment) {

        return new AppointmentDTO(

                appointment.getId(),

                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),

                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),

                appointment.getAppointmentTime(),

                appointment.getStatus()
        );
    }
}
