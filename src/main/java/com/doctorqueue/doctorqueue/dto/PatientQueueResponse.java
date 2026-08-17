package com.doctorqueue.doctorqueue.dto;

public class PatientQueueResponse {
    private Long queueId;
    private Long doctorId;
    private String doctorName;
    private String clinicName;

    private String patientName;
    private Integer myTokenNumber;
    private Integer currentTokenNumber;

    private String status;
    private Integer patientsAhead;
    private Integer estimatedWaitMinutes;

    public PatientQueueResponse(
            Long queueId,
            Long doctorId,
            String doctorName,
            String clinicName,
            String patientName,
            Integer myTokenNumber,
            Integer currentTokenNumber,
            String status,
            Integer patientsAhead,
            Integer estimatedWaitMinutes) {

        this.queueId = queueId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.patientName = patientName;
        this.myTokenNumber = myTokenNumber;
        this.currentTokenNumber = currentTokenNumber;
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

    public Integer getMyTokenNumber() {
        return myTokenNumber;
    }

    public Integer getCurrentTokenNumber() {
        return currentTokenNumber;
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
