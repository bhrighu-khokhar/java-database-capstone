package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // Book a new appointment
    @Transactional
    public int bookAppointment(Appointment appointment) {

        try {
            if (appointment == null ||
                appointment.getDoctor() == null ||
                appointment.getPatient() == null ||
                appointment.getAppointmentTime() == null) {
                return 0;
            }

            Long doctorId = appointment.getDoctor().getId();
            Long patientId = appointment.getPatient().getId();

            Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
            Patient patient = patientRepository.findById(patientId).orElse(null);

            if (doctor == null || patient == null) {
                return 0;
            }

            appointment.setDoctor(doctor);
            appointment.setPatient(patient);

            appointmentRepository.save(appointment);

            return 1;

        } catch (Exception e) {
            return 0;
        }
    }

    // Update an existing appointment
    @Transactional
    public String updateAppointment(
            Long appointmentId,
            Appointment updatedAppointment,
            Long patientId) {

        try {
            Appointment existingAppointment =
                    appointmentRepository.findById(appointmentId).orElse(null);

            if (existingAppointment == null) {
                return "Appointment not found";
            }

            if (existingAppointment.getPatient() == null ||
                !existingAppointment.getPatient().getId().equals(patientId)) {
                return "Patient is not authorized to update this appointment";
            }

            if (updatedAppointment.getAppointmentTime() == null) {
                return "Appointment time cannot be null";
            }

            if (updatedAppointment.getDoctor() == null ||
                updatedAppointment.getDoctor().getId() == null) {
                return "Doctor is required";
            }

            Doctor doctor = doctorRepository
                    .findById(updatedAppointment.getDoctor().getId())
                    .orElse(null);

            if (doctor == null) {
                return "Doctor not found";
            }

            existingAppointment.setDoctor(doctor);
            existingAppointment.setAppointmentTime(
                    updatedAppointment.getAppointmentTime());

            appointmentRepository.save(existingAppointment);

            return "Appointment updated successfully";

        } catch (Exception e) {
            return "Unable to update appointment";
        }
    }

    // Cancel an appointment
    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {

        try {
            Appointment appointment =
                    appointmentRepository.findById(appointmentId).orElse(null);

            if (appointment == null) {
                return "Appointment not found";
            }

            if (appointment.getPatient() == null ||
                !appointment.getPatient().getId().equals(patientId)) {
                return "Patient is not authorized to cancel this appointment";
            }

            appointmentRepository.deleteById(appointmentId);

            return "Appointment cancelled successfully";

        } catch (Exception e) {
            return "Unable to cancel appointment";
        }
    }

    // Get appointments for a doctor on a particular day
    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(
            Long doctorId,
            LocalDate date,
            String patientName) {

        LocalDateTime start =
                date.atStartOfDay();

        LocalDateTime end =
                date.atTime(LocalTime.MAX);

        if (patientName != null &&
            !patientName.trim().isEmpty()) {

            return appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId,
                            patientName,
                            start,
                            end);
        }

        return appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(
                        doctorId,
                        start,
                        end);
    }

    // Change appointment status
    @Transactional
    public boolean changeStatus(Long appointmentId, int status) {

        try {
            Appointment appointment =
                    appointmentRepository.findById(appointmentId).orElse(null);

            if (appointment == null) {
                return false;
            }

            appointment.setStatus(status);
            appointmentRepository.save(appointment);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
