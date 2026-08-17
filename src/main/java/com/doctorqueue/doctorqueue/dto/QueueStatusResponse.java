package com.doctorqueue.doctorqueue.dto;

public class QueueStatusResponse {
    private Long queueId;
    private Long doctorId;
    private String doctorName;
    private String clinicName;

    private int myToken;
    private int currentToken;
    private int patientsAhead;
    private int estimatedWaitMinutes;

    private String status;

    public QueueStatusResponse(
            Long queueId,
            Long doctorId,
            String doctorName,
            String clinicName,
            int myToken,
            int currentToken,
            int patientsAhead,
            int estimatedWaitMinutes,
            String status) {

        this.queueId = queueId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.myToken = myToken;
        this.currentToken = currentToken;
        this.patientsAhead = patientsAhead;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.status = status;
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

    public int getMyToken() {
        return myToken;
    }

    public int getCurrentToken() {
        return currentToken;
    }

    public int getPatientsAhead() {
        return patientsAhead;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public String getStatus() {
        return status;
    }
}
