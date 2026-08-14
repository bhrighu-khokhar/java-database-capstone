package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {

        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {

        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        if (doctor == null || doctor.getAvailableTimes() == null) {
            return new ArrayList<>();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                        doctorId, start, end);

        List<String> bookedTimes = appointments.stream()
                .filter(a -> a.getAppointmentTime() != null)
                .map(a -> a.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toList());

        return doctor.getAvailableTimes()
                .stream()
                .filter(time -> !bookedTimes.contains(time))
                .collect(Collectors.toList());
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {

        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }

            doctorRepository.save(doctor);
            return 1;

        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Long id, Doctor updatedDoctor) {

        try {
            Doctor existingDoctor =
                    doctorRepository.findById(id).orElse(null);

            if (existingDoctor == null) {
                return -1;
            }

            existingDoctor.setName(updatedDoctor.getName());
            existingDoctor.setSpecialty(updatedDoctor.getSpecialty());
            existingDoctor.setEmail(updatedDoctor.getEmail());
            existingDoctor.setPhone(updatedDoctor.getPhone());
            existingDoctor.setAvailableTimes(
                    updatedDoctor.getAvailableTimes());

            if (updatedDoctor.getPassword() != null
                    && !updatedDoctor.getPassword().isEmpty()) {

                existingDoctor.setPassword(updatedDoctor.getPassword());
            }

            doctorRepository.save(existingDoctor);

            return 1;

        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(Long id) {

        try {
            Doctor doctor =
                    doctorRepository.findById(id).orElse(null);

            if (doctor == null) {
                return -1;
            }

            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);

            return 1;

        } catch (Exception e) {
            return 0;
        }
    }

    public String validateDoctor(String email, String password) {

        try {
            Doctor doctor = doctorRepository.findByEmail(email);

            if (doctor == null) {
                return "Invalid email or password";
            }

            if (!doctor.getPassword().equals(password)) {
                return "Invalid email or password";
            }

            // TokenService currently accepts only one argument
            return tokenService.generateToken(doctor.getEmail());

        } catch (Exception e) {
            return "Invalid email or password";
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> findDoctorByName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return doctorRepository.findAll();
        }

        return doctorRepository.findByNameLike("%" + name + "%");
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByNameSpecilityandTime(
            String name,
            String specialty,
            String time) {

        List<Doctor> doctors;

        if (name != null && !name.trim().isEmpty()
                && specialty != null && !specialty.trim().isEmpty()) {

            doctors = doctorRepository
                    .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                            name, specialty);

        } else if (name != null && !name.trim().isEmpty()) {

            doctors = findDoctorByName(name);

        } else if (specialty != null && !specialty.trim().isEmpty()) {

            doctors = doctorRepository
                    .findBySpecialtyIgnoreCase(specialty);

        } else {

            doctors = doctorRepository.findAll();
        }

        return filterDoctorByTime(doctors, time);
    }

    public List<Doctor> filterDoctorByTime(
            List<Doctor> doctors,
            String time) {

        if (time == null || time.trim().isEmpty()) {
            return doctors;
        }

        String requestedTime = time.toUpperCase();

        return doctors.stream()
                .filter(doctor -> doctor.getAvailableTimes() != null)
                .filter(doctor ->
                        doctor.getAvailableTimes()
                                .stream()
                                .anyMatch(slot ->
                                        matchesTimePeriod(
                                                slot,
                                                requestedTime)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndTime(
            String name,
            String time) {

        List<Doctor> doctors = findDoctorByName(name);

        return filterDoctorByTime(doctors, time);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndSpecility(
            String name,
            String specialty) {

        return doctorRepository
                .findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
                        name, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTimeAndSpecility(
            String time,
            String specialty) {

        List<Doctor> doctors =
                doctorRepository.findBySpecialtyIgnoreCase(specialty);

        return filterDoctorByTime(doctors, time);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorBySpecility(
            String specialty) {

        return doctorRepository
                .findBySpecialtyIgnoreCase(specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByTime(String time) {

        List<Doctor> doctors = doctorRepository.findAll();

        return filterDoctorByTime(doctors, time);
    }

    private boolean matchesTimePeriod(
            String slot,
            String requestedPeriod) {

        if (slot == null) {
            return false;
        }

        try {
            LocalTime localTime = LocalTime.parse(slot);

            if ("AM".equals(requestedPeriod)) {
                return localTime.getHour() < 12;
            }

            if ("PM".equals(requestedPeriod)) {
                return localTime.getHour() >= 12;
            }

        } catch (Exception e) {
            return slot.toUpperCase().contains(requestedPeriod);
        }

        return false;
    }
}
