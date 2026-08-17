package com.doctorqueue.doctorqueue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JoinQueueRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 50, message = "Patient name must be between 2 and 50 characters")
    private String patientName;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
