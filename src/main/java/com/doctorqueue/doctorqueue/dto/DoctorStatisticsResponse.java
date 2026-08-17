package com.doctorqueue.doctorqueue.dto;

public class DoctorStatisticsResponse {

    private Long doctorId;

    private String doctorName;

    private long totalPatientsToday;

    private long waitingPatients;

    private long servingPatients;

    private long completedPatients;

    private long skippedPatients;

    private long cancelledPatients;

    private double averageEstimatedWaitMinutes;

    public DoctorStatisticsResponse(
            Long doctorId,
            String doctorName,
            long totalPatientsToday,
            long waitingPatients,
            long servingPatients,
            long completedPatients,
            long skippedPatients,
            long cancelledPatients,
            double averageEstimatedWaitMinutes) {

        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.totalPatientsToday = totalPatientsToday;
        this.waitingPatients = waitingPatients;
        this.servingPatients = servingPatients;
        this.completedPatients = completedPatients;
        this.skippedPatients = skippedPatients;
        this.cancelledPatients = cancelledPatients;
        this.averageEstimatedWaitMinutes =
                averageEstimatedWaitMinutes;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public long getTotalPatientsToday() {
        return totalPatientsToday;
    }

    public long getWaitingPatients() {
        return waitingPatients;
    }

    public long getServingPatients() {
        return servingPatients;
    }

    public long getCompletedPatients() {
        return completedPatients;
    }

    public long getSkippedPatients() {
        return skippedPatients;
    }

    public long getCancelledPatients() {
        return cancelledPatients;
    }

    public double getAverageEstimatedWaitMinutes() {
        return averageEstimatedWaitMinutes;
    }
}
