package com.doctorqueue.doctorqueue.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private Integer tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    private Integer estimatedWaitMinutes;

    public QueueEntry() {
    }

    public QueueEntry(
            Doctor doctor,
            String patientName,
            Integer tokenNumber,
            QueueStatus status,
            LocalDateTime joinedAt,
            Integer estimatedWaitMinutes) {

        this.doctor = doctor;
        this.patientName = patientName;
        this.tokenNumber = tokenNumber;
        this.status = status;
        this.joinedAt = joinedAt;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(Integer tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

}
