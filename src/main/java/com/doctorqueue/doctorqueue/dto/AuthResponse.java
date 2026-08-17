package com.doctorqueue.doctorqueue.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private Long doctorId;

    public AuthResponse(
            String token,
            Long userId,
            String name,
            String email,
            String role,
            Long doctorId) {

        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.doctorId = doctorId;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getDoctorId() {
        return doctorId;
    }
}
