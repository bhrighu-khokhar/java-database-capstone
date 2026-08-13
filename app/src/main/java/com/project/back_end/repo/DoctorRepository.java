package com.project.back_end.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Find doctor by email
    Doctor findByEmail(String email);

    // Find doctors whose name contains the search text
    List<Doctor> findByNameLike(String name);

    // Find doctors by name and specialty
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(
            String name,
            String specialty
    );

    // Find doctors by specialty
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
