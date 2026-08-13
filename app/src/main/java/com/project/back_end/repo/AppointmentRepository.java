package com.project.back_end.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Get appointments for a doctor within a time range
    @Query("""
        SELECT DISTINCT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        LEFT JOIN FETCH d.availableTimes
        WHERE d.id = :doctorId
        AND a.appointmentTime BETWEEN :start AND :end
        ORDER BY a.appointmentTime ASC
    """)
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // Get appointments for a doctor and patient name within a time range
    @Query("""
        SELECT DISTINCT a
        FROM Appointment a
        LEFT JOIN FETCH a.doctor d
        LEFT JOIN FETCH a.patient p
        WHERE d.id = :doctorId
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :patientName, '%'))
        AND a.appointmentTime BETWEEN :start AND :end
        ORDER BY a.appointmentTime ASC
    """)
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId,
            @Param("patientName") String patientName,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    // Delete all appointments belonging to a doctor
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);


    // Find all appointments for a patient
    List<Appointment> findByPatientId(Long patientId);


    // Find patient's appointments by status
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
            Long patientId,
            int status
    );


    // Filter appointments by doctor name and patient ID
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN a.doctor d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
        AND a.patient.id = :patientId
        ORDER BY a.appointmentTime ASC
    """)
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId
    );


    // Filter by doctor name, patient ID and status
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN a.doctor d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :doctorName, '%'))
        AND a.patient.id = :patientId
        AND a.status = :status
        ORDER BY a.appointmentTime ASC
    """)
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName,
            @Param("patientId") Long patientId,
            @Param("status") int status
    );


    // Update appointment status
    @Modifying
    @Transactional
    @Query("""
        UPDATE Appointment a
        SET a.status = :status
        WHERE a.id = :id
    """)
    void updateStatus(
            @Param("status") int status,
            @Param("id") long id
    );
}
