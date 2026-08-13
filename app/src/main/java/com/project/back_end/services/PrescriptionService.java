package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    // Constructor injection
    public PrescriptionService(
            PrescriptionRepository prescriptionRepository) {

        this.prescriptionRepository = prescriptionRepository;
    }

    // Save prescription
    public ResponseEntity<?> savePrescription(Prescription prescription) {

        try {

            Long appointmentId = prescription.getAppointmentId();

            List<Prescription> existingPrescriptions =
                    prescriptionRepository.findByAppointmentId(appointmentId);

            if (existingPrescriptions != null
                    && !existingPrescriptions.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Prescription already exists for this appointment");
            }

            prescriptionRepository.save(prescription);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Prescription saved successfully");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving prescription");
        }
    }

    // Get prescription by appointment ID
    public ResponseEntity<?> getPrescription(Long appointmentId) {

        try {

            List<Prescription> prescriptions =
                    prescriptionRepository
                            .findByAppointmentId(appointmentId);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("prescriptions", prescriptions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving prescription");
        }
    }
}
