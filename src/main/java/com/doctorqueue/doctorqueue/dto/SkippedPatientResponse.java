package com.doctorqueue.doctorqueue.dto;

public class SkippedPatientResponse {
    private Long queueId;
    private Integer tokenNumber;
    private String patientName;

    public SkippedPatientResponse(
            Long queueId,
            Integer tokenNumber,
            String patientName) {

        this.queueId = queueId;
        this.tokenNumber = tokenNumber;
        this.patientName = patientName;
    }

    public Long getQueueId() {
        return queueId;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public String getPatientName() {
        return patientName;
    }
}
