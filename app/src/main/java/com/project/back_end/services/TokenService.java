package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repositories.AdminRepository;
import com.project.back_end.repositories.DoctorRepository;
import com.project.back_end.repositories.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {

    private static final long TOKEN_VALIDITY =
            7L * 24 * 60 * 60 * 1000;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository
    ) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public String generateToken(String identifier) {
        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + TOKEN_VALIDITY
        );

        return Jwts.builder()
                .subject(identifier)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(
            String identifier,
            String role
    ) {
        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + TOKEN_VALIDITY
        );

        return Jwts.builder()
                .subject(identifier)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractIdentifier(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractEmailFromToken(String token) {
        return extractIdentifier(token);
    }

    public Long getUserIdFromToken(String token) {
        String identifier = extractIdentifier(token);

        Doctor doctor =
                doctorRepository.findByEmail(identifier);

        if (doctor != null) {
            return doctor.getId();
        }

        Patient patient =
                patientRepository.findByEmail(identifier);

        if (patient != null) {
            return patient.getId();
        }

        return null;
    }

    public boolean validateToken(
            String token,
            String user
    ) {
        try {
            Claims claims = extractClaims(token);

            String identifier = claims.getSubject();

            String tokenRole = claims.get(
                    "role",
                    String.class
            );

            if (
                identifier == null ||
                identifier.isBlank() ||
                claims.getExpiration() == null ||
                claims.getExpiration().before(new Date())
            ) {
                return false;
            }

            if (
                tokenRole != null &&
                !tokenRole.equalsIgnoreCase(user)
            ) {
                return false;
            }

            return switch (user.toLowerCase()) {
                case "admin" ->
                        adminRepository.findByUsername(identifier)
                                != null;

                case "doctor" ->
                        doctorRepository.findByEmail(identifier)
                                != null;

                case "patient", "loggedpatient" ->
                        patientRepository.findByEmail(identifier)
                                != null;

                default -> false;
            };

        } catch (
                JwtException |
                IllegalArgumentException exception
        ) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        String cleanToken = removeBearerPrefix(token);

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(cleanToken)
                .getPayload();
    }

    private String removeBearerPrefix(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token cannot be null or empty"
            );
        }

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }
}