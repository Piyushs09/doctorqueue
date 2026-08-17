package com.doctorqueue.doctorqueue.dto;

import com.doctorqueue.doctorqueue.entity.QueueStatus;

import java.time.LocalDateTime;

public class QueueResponse {

    private Long queueId;
    private Long doctorId;
    private String doctorName;
    private String patientName;
    private Integer tokenNumber;
    private QueueStatus status;
    private LocalDateTime joinedAt;
    private Integer patientsAhead;
    private Integer estimatedWaitMinutes;

    public QueueResponse() {
    }

    public QueueResponse(
            Long queueId,
            Long doctorId,
            String doctorName,
            String patientName,
            Integer tokenNumber,
            QueueStatus status,
            LocalDateTime joinedAt,
            Integer patientsAhead,
            Integer estimatedWaitMinutes) {

        this.queueId = queueId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.tokenNumber = tokenNumber;
        this.status = status;
        this.joinedAt = joinedAt;
        this.patientsAhead = patientsAhead;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public Long getQueueId() {
        return queueId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public Integer getPatientsAhead() {
        return patientsAhead;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }
}
