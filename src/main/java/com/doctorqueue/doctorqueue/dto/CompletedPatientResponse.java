
package com.doctorqueue.doctorqueue.dto;

import java.time.LocalDateTime;

public class CompletedPatientResponse {

    private Long queueId;

    private String patientName;

    private Integer tokenNumber;

    private LocalDateTime joinedAt;

    private String status;

    public CompletedPatientResponse(
            Long queueId,
            String patientName,
            Integer tokenNumber,
            LocalDateTime joinedAt,
            String status) {

        this.queueId = queueId;
        this.patientName = patientName;
        this.tokenNumber = tokenNumber;
        this.joinedAt = joinedAt;
        this.status = status;
    }

    public Long getQueueId() {
        return queueId;
    }

    public String getPatientName() {
        return patientName;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public String getStatus() {
        return status;
    }
}