package com.project.back_end.services;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {

        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // Get signing key
    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Generate JWT token
    public String generateToken(String email) {

        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + 7L * 24 * 60 * 60 * 1000
        );

        return Jwts.builder()
                .subject(email)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract email from JWT
    public String extractEmail(String token) {

        Claims claims = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // Validate token according to role
    public boolean validateToken(String token, String role) {

        try {

            String email = extractEmail(token);

            if ("ADMIN".equalsIgnoreCase(role)) {

                Admin admin =
                        adminRepository.findByUsername(email);

                return admin != null;

            } else if ("DOCTOR".equalsIgnoreCase(role)) {

                Doctor doctor =
                        doctorRepository.findByEmail(email);

                return doctor != null;

            } else if ("PATIENT".equalsIgnoreCase(role)) {

                Patient patient =
                        patientRepository.findByEmail(email);

                return patient != null;
            }

            return false;

        } catch (Exception e) {

            return false;
        }
    }
}
