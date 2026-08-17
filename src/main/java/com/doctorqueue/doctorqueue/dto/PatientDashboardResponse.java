package com.doctorqueue.doctorqueue.dto;

public class PatientDashboardResponse {

    private Long queueId;
    private Long doctorId;
    private String doctorName;
    private String clinicName;
    private String patientName;

    private Integer tokenNumber;
    private Integer currentToken;

    private String status;

    private Integer patientsAhead;
    private Integer estimatedWaitMinutes;

    public PatientDashboardResponse(
            Long queueId,
            Long doctorId,
            String doctorName,
            String clinicName,
            String patientName,
            Integer tokenNumber,
            Integer currentToken,
            String status,
            Integer patientsAhead,
            Integer estimatedWaitMinutes) {

        this.queueId = queueId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.patientName = patientName;
        this.tokenNumber = tokenNumber;
        this.currentToken = currentToken;
        this.status = status;
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

    public String getClinicName() {
        return clinicName;
    }

    public String getPatientName() {
        return patientName;
    }

    public Integer getTokenNumber() {
        return tokenNumber;
    }

    public Integer getCurrentToken() {
        return currentToken;
    }

    public String getStatus() {
        return status;
    }

    public Integer getPatientsAhead() {
        return patientsAhead;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }
}