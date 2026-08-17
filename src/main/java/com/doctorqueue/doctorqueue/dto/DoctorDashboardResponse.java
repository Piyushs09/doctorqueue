package com.doctorqueue.doctorqueue.dto;

public class DoctorDashboardResponse {

    private Long doctorId;

    private String doctorName;

    private Long currentQueueId;

    private Integer currentTokenNumber;

    private String currentPatientName;

    private long waitingPatients;

    private long skippedPatients;

    private long completedPatients;

    public DoctorDashboardResponse(
            Long doctorId,
            String doctorName,
            Long currentQueueId,
            Integer currentTokenNumber,
            String currentPatientName,
            long waitingPatients,
            long skippedPatients,
            long completedPatients) {

        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.currentQueueId = currentQueueId;
        this.currentTokenNumber = currentTokenNumber;
        this.currentPatientName = currentPatientName;
        this.waitingPatients = waitingPatients;
        this.skippedPatients = skippedPatients;
        this.completedPatients = completedPatients;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getCurrentQueueId() {
        return currentQueueId;
    }

    public Integer getCurrentTokenNumber() {
        return currentTokenNumber;
    }

    public String getCurrentPatientName() {
        return currentPatientName;
    }

    public long getWaitingPatients() {
        return waitingPatients;
    }

    public long getSkippedPatients() {
        return skippedPatients;
    }

    public long getCompletedPatients() {
        return completedPatients;
    }
}