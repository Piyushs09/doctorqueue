package com.doctorqueue.doctorqueue.controller;

import com.doctorqueue.doctorqueue.dto.CompletedPatientResponse;
import com.doctorqueue.doctorqueue.dto.DoctorDashboardResponse;
import com.doctorqueue.doctorqueue.dto.DoctorStatisticsResponse;
import com.doctorqueue.doctorqueue.dto.JoinQueueRequest;
import com.doctorqueue.doctorqueue.dto.PatientDashboardResponse;
import com.doctorqueue.doctorqueue.dto.QueueResponse;
import com.doctorqueue.doctorqueue.service.QueueEventService;
import com.doctorqueue.doctorqueue.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "http://localhost:5173")
public class QueueController {

    private final QueueService queueService;

    private final QueueEventService queueEventService;

    public QueueController(
            QueueService queueService,
            QueueEventService queueEventService) {

        this.queueService =
                queueService;

        this.queueEventService =
                queueEventService;
    }

    // =========================================================
    // SSE
    // =========================================================

    @GetMapping("/events")
    public SseEmitter events() {

        return queueEventService.subscribe();
    }

    // =========================================================
    // PATIENT
    // =========================================================

    @PostMapping("/join")
    public QueueResponse joinQueue(
            @Valid @RequestBody JoinQueueRequest request) {

        QueueResponse response =
                queueService.joinQueue(request);

        queueEventService
                .publishQueueUpdate();

        return response;
    }

    @GetMapping("/{queueId}")
    public QueueResponse getQueueStatus(
            @PathVariable Long queueId) {

        return queueService
                .getQueueStatus(queueId);
    }

    @GetMapping("/patient/{queueId}/dashboard")
    public PatientDashboardResponse
    getPatientDashboard(
            @PathVariable Long queueId) {

        return queueService
                .getPatientDashboard(queueId);
    }

    @GetMapping("/patient/current")
    public PatientDashboardResponse
    getCurrentPatientDashboard() {

        return queueService
                .getCurrentPatientDashboard();
    }

    @DeleteMapping("/{queueId}")
    public QueueResponse cancelQueue(
            @PathVariable Long queueId) {

        QueueResponse response =
                queueService.cancelQueue(queueId);

        queueEventService
                .publishQueueUpdate();

        return response;
    }

    // =========================================================
    // DOCTOR QUEUE
    // =========================================================

    @GetMapping("/doctor/{doctorId}")
    public List<QueueResponse> getDoctorQueue(
            @PathVariable Long doctorId) {

        return queueService
                .getDoctorQueue(doctorId);
    }

    // =========================================================
    // DOCTOR DASHBOARD
    // =========================================================

    @GetMapping("/doctor/{doctorId}/dashboard")
    public DoctorDashboardResponse
    getDoctorDashboard(
            @PathVariable Long doctorId) {

        return queueService
                .getDoctorDashboard(doctorId);
    }

    // =========================================================
    // DOCTOR STATISTICS
    // =========================================================

    @GetMapping("/doctor/{doctorId}/statistics")
    public DoctorStatisticsResponse
    getDoctorStatistics(
            @PathVariable Long doctorId) {

        return queueService
                .getDoctorStatistics(doctorId);
    }

    // =========================================================
    // COMPLETED TODAY
    // =========================================================

    @GetMapping("/doctor/{doctorId}/completed-today")
    public List<CompletedPatientResponse>
    getCompletedToday(
            @PathVariable Long doctorId) {

        return queueService
                .getCompletedToday(doctorId);
    }

    // =========================================================
    // NEXT PATIENT
    // =========================================================

    @PostMapping("/doctor/{doctorId}/next")
    public QueueResponse startNextPatient(
            @PathVariable Long doctorId) {

        QueueResponse response =
                queueService
                        .startNextPatient(doctorId);

        queueEventService
                .publishQueueUpdate();

        return response;
    }

    // =========================================================
    // COMPLETE CURRENT
    // =========================================================

    @PostMapping("/doctor/{doctorId}/complete")
    public QueueResponse
    completeCurrentPatient(
            @PathVariable Long doctorId) {

        QueueResponse response =
                queueService
                        .completeCurrentPatient(
                                doctorId
                        );

        queueEventService
                .publishQueueUpdate();

        return response;
    }

    // =========================================================
    // SKIP CURRENT
    // =========================================================

    @PostMapping("/doctor/{doctorId}/skip")
    public QueueResponse skipCurrentPatient(
            @PathVariable Long doctorId) {

        QueueResponse response =
                queueService
                        .skipCurrentPatient(
                                doctorId
                        );

        queueEventService
                .publishQueueUpdate();

        return response;
    }

    // =========================================================
    // COMPLETE SKIPPED
    // =========================================================

    @PostMapping(
            "/doctor/{doctorId}/skipped/{queueId}/complete"
    )
    public QueueResponse
    completeSkippedPatient(
            @PathVariable Long doctorId,
            @PathVariable Long queueId) {

        QueueResponse response =
                queueService
                        .completeSkippedPatient(
                                doctorId,
                                queueId
                        );

        queueEventService
                .publishQueueUpdate();

        return response;
    }
}