package com.doctorqueue.doctorqueue.service;

import com.doctorqueue.doctorqueue.dto.AuthResponse;
import com.doctorqueue.doctorqueue.dto.LoginRequest;
import com.doctorqueue.doctorqueue.dto.RegisterRequest;
import com.doctorqueue.doctorqueue.entity.Clinic;
import com.doctorqueue.doctorqueue.entity.Doctor;
import com.doctorqueue.doctorqueue.entity.User;
import com.doctorqueue.doctorqueue.entity.UserRole;
import com.doctorqueue.doctorqueue.repository.ClinicRepository;
import com.doctorqueue.doctorqueue.repository.DoctorRepository;
import com.doctorqueue.doctorqueue.repository.UserRepository;
import com.doctorqueue.doctorqueue.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            DoctorRepository doctorRepository,
            ClinicRepository clinicRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.doctorRepository = doctorRepository;
        this.clinicRepository = clinicRepository;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "Email is already registered"
            );
        }

        UserRole role;

        try {
            role = UserRole.valueOf(
                    request.getRole()
                            .trim()
                            .toUpperCase()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Invalid account type"
            );
        }

        Doctor doctor = null;

        // =====================================================
        // DOCTOR REGISTRATION
        // =====================================================

        if (role == UserRole.DOCTOR) {

            if (request.getSpecialization() == null ||
                    request.getSpecialization().isBlank()) {

                throw new RuntimeException(
                        "Specialization is required for doctor registration"
                );
            }

            if (request.getClinicId() == null) {

                throw new RuntimeException(
                        "Clinic is required for doctor registration"
                );
            }

            Clinic clinic = clinicRepository
                    .findById(request.getClinicId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Clinic not found"
                            )
                    );

            doctor = new Doctor(
                    request.getName().trim(),
                    request.getSpecialization().trim(),
                    request.getPhone(),
                    clinic
            );

            doctor = doctorRepository.save(doctor);
        }

        // =====================================================
        // CREATE USER
        // =====================================================

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setPhone(request.getPhone());
        user.setRole(role);

        // IMPORTANT:
        // Patient -> null
        // Doctor  -> newly-created Doctor
        user.setDoctor(doctor);

        user.setCreatedAt(LocalDateTime.now());

        User savedUser =
                userRepository.save(user);

        // =====================================================
        // DOCTOR ID
        // =====================================================

        Long doctorId = null;

        if (savedUser.getDoctor() != null) {
            doctorId =
                    savedUser.getDoctor().getId();
        }

        // =====================================================
        // JWT
        // =====================================================

        String token =
                jwtService.generateToken(
                        savedUser.getEmail(),
                        savedUser.getId(),
                        savedUser.getRole().name()
                );

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                doctorId
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @Transactional(readOnly = true)
    public AuthResponse login(
            LoginRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid email or password"
                                )
                        );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        Long doctorId = null;

        if (user.getDoctor() != null) {
            doctorId =
                    user.getDoctor().getId();
        }

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getId(),
                        user.getRole().name()
                );

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                doctorId
        );
    }
}