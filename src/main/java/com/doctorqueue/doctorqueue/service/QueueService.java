package com.doctorqueue.doctorqueue.service;

import com.doctorqueue.doctorqueue.dto.CompletedPatientResponse;
import com.doctorqueue.doctorqueue.dto.DoctorDashboardResponse;
import com.doctorqueue.doctorqueue.dto.JoinQueueRequest;
import com.doctorqueue.doctorqueue.dto.PatientDashboardResponse;
import com.doctorqueue.doctorqueue.dto.DoctorStatisticsResponse;
import com.doctorqueue.doctorqueue.dto.QueueResponse;
import com.doctorqueue.doctorqueue.dto.QueueStatusResponse;
import com.doctorqueue.doctorqueue.entity.Doctor;
import com.doctorqueue.doctorqueue.entity.QueueEntry;
import com.doctorqueue.doctorqueue.entity.QueueStatus;
import com.doctorqueue.doctorqueue.entity.User;
import com.doctorqueue.doctorqueue.repository.DoctorRepository;
import com.doctorqueue.doctorqueue.repository.QueueEntryRepository;
import com.doctorqueue.doctorqueue.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class QueueService {

    private static final int AVERAGE_CONSULTATION_MINUTES = 10;

    private final QueueEntryRepository queueEntryRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    public QueueService(
            QueueEntryRepository queueEntryRepository,
            DoctorRepository doctorRepository,
            UserRepository userRepository) {

        this.queueEntryRepository = queueEntryRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // PATIENT JOINS QUEUE
    // =========================================================


    @Transactional
    public QueueResponse joinQueue(JoinQueueRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "Patient is not authenticated"
            );
        }

        User patient =
                userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Patient account not found"
                                ));

        if (patient.getRole() == null ||
                !"PATIENT".equalsIgnoreCase(
                        patient.getRole().name())) {

            throw new RuntimeException(
                    "Only patients can join a queue"
            );
        }

        // =========================================================
// PREVENT DUPLICATE ACTIVE QUEUE
// =========================================================

        List<QueueStatus> activeStatuses =
                List.of(
                        QueueStatus.WAITING,
                        QueueStatus.SERVING
                );

        Optional<QueueEntry> existingQueue =
                queueEntryRepository
                        .findFirstByPatientIdAndDoctorIdAndStatusInOrderByJoinedAtDesc(
                                patient.getId(),
                                request.getDoctorId(),
                                activeStatuses
                        );

        if (existingQueue.isPresent()) {

            QueueEntry existing =
                    existingQueue.get();

            int patientsAhead = 0;

            if (existing.getStatus() ==
                    QueueStatus.WAITING) {

                patientsAhead =
                        (int) queueEntryRepository
                                .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                        request.getDoctorId(),
                                        QueueStatus.WAITING
                                )
                                .stream()
                                .filter(queueEntry ->
                                        queueEntry.getTokenNumber()
                                                < existing.getTokenNumber()
                                )
                                .count();
            }

            boolean doctorServing =
                    !queueEntryRepository
                            .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                    request.getDoctorId(),
                                    QueueStatus.SERVING
                            )
                            .isEmpty();

            int estimatedWaitMinutes =
                    patientsAhead
                            * AVERAGE_CONSULTATION_MINUTES;

            if (doctorServing &&
                    existing.getStatus() ==
                            QueueStatus.WAITING) {

                estimatedWaitMinutes =
                        (patientsAhead + 1)
                                * AVERAGE_CONSULTATION_MINUTES;
            }

            return toResponse(
                    existing,
                    patientsAhead,
                    estimatedWaitMinutes
            );
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found"));

        int nextToken =
                getNextTokenNumber(doctor.getId());

        QueueEntry entry = new QueueEntry();

        entry.setDoctor(doctor);
        entry.setPatient(patient);
        entry.setPatientName(request.getPatientName());
        entry.setTokenNumber(nextToken);
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinedAt(LocalDateTime.now());

        int patientsAhead =
                (int) queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.WAITING
                        )
                        .size();

        boolean doctorServing =
                !queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.SERVING
                        )
                        .isEmpty();

        int estimatedWaitMinutes;

        if (doctorServing) {
            estimatedWaitMinutes =
                    (patientsAhead + 1)
                            * AVERAGE_CONSULTATION_MINUTES;
        } else {
            estimatedWaitMinutes =
                    patientsAhead
                            * AVERAGE_CONSULTATION_MINUTES;
        }

        entry.setEstimatedWaitMinutes(
                estimatedWaitMinutes
        );

        QueueEntry savedEntry =
                queueEntryRepository.save(entry);

        return toResponse(
                savedEntry,
                patientsAhead,
                estimatedWaitMinutes
        );
    }

    // =========================================================
    // GET DOCTOR QUEUE
    // =========================================================

    public List<QueueResponse> getDoctorQueue(
            Long doctorId) {

        return queueEntryRepository
                .findByDoctorIdOrderByTokenNumberAsc(doctorId)
                .stream()
                .map(entry -> {

                    int patientsAhead = 0;

                    if (entry.getStatus() ==
                            QueueStatus.WAITING) {

                        patientsAhead =
                                (int) queueEntryRepository
                                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                                doctorId,
                                                QueueStatus.WAITING
                                        )
                                        .stream()
                                        .filter(e ->
                                                e.getTokenNumber()
                                                        < entry.getTokenNumber()
                                        )
                                        .count();
                    }

                    int waitMinutes =
                            patientsAhead
                                    * AVERAGE_CONSULTATION_MINUTES;

                    return toResponse(
                            entry,
                            patientsAhead,
                            waitMinutes
                    );
                })
                .toList();
    }

    // =========================================================
    // START NEXT PATIENT
    // =========================================================

    @Transactional
    public QueueResponse startNextPatient(
            Long doctorId) {

        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found"
                        ));

        boolean alreadyServing =
                !queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.SERVING
                        )
                        .isEmpty();

        if (alreadyServing) {

            throw new RuntimeException(
                    "Doctor is already serving a patient"
            );
        }

        QueueEntry nextPatient =
                queueEntryRepository
                        .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.WAITING
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No waiting patients"
                                ));

        nextPatient.setStatus(
                QueueStatus.SERVING
        );

        QueueEntry savedEntry =
                queueEntryRepository.save(nextPatient);

        return toResponse(
                savedEntry,
                0,
                0
        );
    }

    // =========================================================
    // COMPLETE CURRENT PATIENT
    // =========================================================

    @Transactional
    public QueueResponse completeCurrentPatient(
            Long doctorId) {

        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found"
                        ));

        QueueEntry currentPatient =
                queueEntryRepository
                        .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.SERVING
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No patient is currently being served"
                                ));

        currentPatient.setStatus(
                QueueStatus.COMPLETED
        );

        currentPatient.setEstimatedWaitMinutes(0);

        QueueEntry savedEntry =
                queueEntryRepository.save(currentPatient);

        return toResponse(
                savedEntry,
                0,
                0
        );
    }

    // =========================================================
    // CANCEL QUEUE
    // =========================================================

    @Transactional
    public QueueResponse cancelQueue(
            Long queueId) {

        QueueEntry entry =
                queueEntryRepository.findById(queueId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue entry not found"
                                ));

        if (entry.getStatus() ==
                QueueStatus.COMPLETED) {

            throw new RuntimeException(
                    "Completed queue cannot be cancelled"
            );
        }

        if (entry.getStatus() ==
                QueueStatus.CANCELLED) {

            throw new RuntimeException(
                    "Queue is already cancelled"
            );
        }

        entry.setStatus(
                QueueStatus.CANCELLED
        );

        entry.setEstimatedWaitMinutes(0);

        QueueEntry savedEntry =
                queueEntryRepository.save(entry);

        return toResponse(
                savedEntry,
                0,
                0
        );
    }

    // =========================================================
    // SKIP CURRENT PATIENT
    // =========================================================

    @Transactional
    public QueueResponse skipCurrentPatient(
            Long doctorId) {

        doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found"
                        ));

        QueueEntry currentPatient =
                queueEntryRepository
                        .findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.SERVING
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No patient is currently being served"
                                ));

        currentPatient.setStatus(
                QueueStatus.SKIPPED
        );

        currentPatient.setEstimatedWaitMinutes(0);

        QueueEntry savedEntry =
                queueEntryRepository.save(
                        currentPatient
                );

        return toResponse(
                savedEntry,
                0,
                0
        );
    }

    // =========================================================
    // COMPLETE SKIPPED PATIENT
    // =========================================================

    @Transactional
    public QueueResponse completeSkippedPatient(
            Long doctorId,
            Long queueId) {

        QueueEntry patient =
                queueEntryRepository.findById(queueId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue entry not found"
                                ));

        if (patient.getDoctor() == null ||
                !patient.getDoctor()
                        .getId()
                        .equals(doctorId)) {

            throw new RuntimeException(
                    "Patient does not belong to this doctor"
            );
        }

        if (patient.getStatus() !=
                QueueStatus.SKIPPED) {

            throw new RuntimeException(
                    "Only skipped patients can be marked as complete"
            );
        }

        patient.setStatus(
                QueueStatus.COMPLETED
        );

        patient.setEstimatedWaitMinutes(0);

        QueueEntry saved =
                queueEntryRepository.save(patient);

        return toResponse(
                saved,
                0,
                0
        );
    }

    // =========================================================
    // COMPLETED PATIENTS TODAY
    // =========================================================

    @Transactional(readOnly = true)
    public List<CompletedPatientResponse>
    getCompletedToday(Long doctorId) {

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);

        return queueEntryRepository
                .findCompletedToday(
                        doctorId,
                        startOfDay,
                        endOfDay
                )
                .stream()
                .map(entry ->
                        new CompletedPatientResponse(
                                entry.getId(),
                                entry.getPatientName(),
                                entry.getTokenNumber(),
                                entry.getJoinedAt(),
                                entry.getStatus().name()
                        )
                )
                .toList();
    }

    // =========================================================
    // PATIENT QUEUE STATUS
    // =========================================================

    public QueueStatusResponse getMyQueueStatus(
            Long queueId) {

        QueueEntry myEntry =
                queueEntryRepository.findById(queueId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue entry not found"
                                ));

        Doctor doctor = myEntry.getDoctor();

        int currentToken = 0;

        List<QueueEntry> servingPatients =
                queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.SERVING
                        );

        if (!servingPatients.isEmpty()) {

            currentToken =
                    servingPatients
                            .get(0)
                            .getTokenNumber();
        }

        if (myEntry.getStatus() ==
                QueueStatus.COMPLETED ||
                myEntry.getStatus() ==
                        QueueStatus.CANCELLED ||
                myEntry.getStatus() ==
                        QueueStatus.SKIPPED) {

            return new QueueStatusResponse(
                    myEntry.getId(),
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getClinic().getName(),
                    myEntry.getTokenNumber(),
                    currentToken,
                    0,
                    0,
                    myEntry.getStatus().name()
            );
        }

        if (myEntry.getStatus() ==
                QueueStatus.SERVING) {

            return new QueueStatusResponse(
                    myEntry.getId(),
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getClinic().getName(),
                    myEntry.getTokenNumber(),
                    currentToken,
                    0,
                    0,
                    myEntry.getStatus().name()
            );
        }

        int patientsAhead =
                (int) queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.WAITING
                        )
                        .stream()
                        .filter(entry ->
                                entry.getTokenNumber()
                                        < myEntry.getTokenNumber()
                        )
                        .count();

        int estimatedWaitMinutes =
                patientsAhead
                        * AVERAGE_CONSULTATION_MINUTES;

        return new QueueStatusResponse(
                myEntry.getId(),
                doctor.getId(),
                doctor.getName(),
                doctor.getClinic().getName(),
                myEntry.getTokenNumber(),
                currentToken,
                patientsAhead,
                estimatedWaitMinutes,
                myEntry.getStatus().name()
        );
    }

    // =========================================================
    // GET QUEUE STATUS
    // =========================================================

    public QueueResponse getQueueStatus(
            Long queueId) {

        QueueEntry entry =
                queueEntryRepository.findById(queueId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue entry not found"
                                ));

        Doctor doctor = entry.getDoctor();

        if (entry.getStatus() ==
                QueueStatus.COMPLETED ||
                entry.getStatus() ==
                        QueueStatus.CANCELLED ||
                entry.getStatus() ==
                        QueueStatus.SKIPPED) {

            return toResponse(
                    entry,
                    0,
                    0
            );
        }

        if (entry.getStatus() ==
                QueueStatus.SERVING) {

            return toResponse(
                    entry,
                    0,
                    0
            );
        }

        boolean doctorServing =
                !queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.SERVING
                        )
                        .isEmpty();

        int patientsAhead =
                (int) queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctor.getId(),
                                QueueStatus.WAITING
                        )
                        .stream()
                        .filter(queueEntry ->
                                queueEntry.getTokenNumber()
                                        < entry.getTokenNumber()
                        )
                        .count();

        int estimatedWaitMinutes;

        if (doctorServing) {

            estimatedWaitMinutes =
                    (patientsAhead + 1)
                            * AVERAGE_CONSULTATION_MINUTES;

        } else {

            estimatedWaitMinutes =
                    patientsAhead
                            * AVERAGE_CONSULTATION_MINUTES;
        }

        return toResponse(
                entry,
                patientsAhead,
                estimatedWaitMinutes
        );
    }

    // =========================================================
    // DOCTOR DASHBOARD
    // =========================================================

    public DoctorDashboardResponse
    getDoctorDashboard(Long doctorId) {

        Doctor doctor =
                doctorRepository.findById(doctorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Doctor not found"
                                ));

        List<QueueEntry> servingPatients =
                queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.SERVING
                        );

        Long currentQueueId = null;
        Integer currentTokenNumber = null;
        String currentPatientName = null;

        if (!servingPatients.isEmpty()) {

            QueueEntry currentPatient =
                    servingPatients.get(0);

            currentQueueId =
                    currentPatient.getId();

            currentTokenNumber =
                    currentPatient.getTokenNumber();

            currentPatientName =
                    currentPatient.getPatientName();
        }

        long waitingPatients =
                queueEntryRepository
                        .countByDoctorIdAndStatus(
                                doctorId,
                                QueueStatus.WAITING
                        );

        long skippedPatients =
                queueEntryRepository
                        .countByDoctorIdAndStatus(
                                doctorId,
                                QueueStatus.SKIPPED
                        );

        long completedPatients =
                queueEntryRepository
                        .countByDoctorIdAndStatus(
                                doctorId,
                                QueueStatus.COMPLETED
                        );

        return new DoctorDashboardResponse(
                doctor.getId(),
                doctor.getName(),
                currentQueueId,
                currentTokenNumber,
                currentPatientName,
                waitingPatients,
                skippedPatients,
                completedPatients
        );
    }

    // =========================================================
    // CURRENT LOGGED-IN PATIENT DASHBOARD
    // =========================================================

    @Transactional(readOnly = true)
    public PatientDashboardResponse getCurrentPatientDashboard() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "Patient is not authenticated"
            );
        }

        User patient =
                userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Patient account not found"
                                ));

        List<QueueStatus> activeStatuses =
                List.of(
                        QueueStatus.WAITING,
                        QueueStatus.SERVING,
                        QueueStatus.SKIPPED
                );

        QueueEntry entry =
                queueEntryRepository
                        .findFirstByPatientIdAndStatusInOrderByJoinedAtDesc(
                                patient.getId(),
                                activeStatuses
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active queue found"
                                ));

        return getPatientDashboard(entry.getId());
    }

    // =========================================================
// DOCTOR STATISTICS
// =========================================================

    @Transactional(readOnly = true)
    public DoctorStatisticsResponse
    getDoctorStatistics(Long doctorId) {

        Doctor doctor =
                doctorRepository.findById(doctorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Doctor not found"
                                ));

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime endOfDay =
                startOfDay.plusDays(1);

        List<QueueEntry> today =
                queueEntryRepository.findTodayByDoctorId(
                        doctorId,
                        startOfDay,
                        endOfDay
                );

        long totalPatientsToday =
                today.size();

        long waitingPatients =
                today.stream()
                        .filter(entry ->
                                entry.getStatus() ==
                                        QueueStatus.WAITING)
                        .count();

        long servingPatients =
                today.stream()
                        .filter(entry ->
                                entry.getStatus() ==
                                        QueueStatus.SERVING)
                        .count();

        long completedPatients =
                today.stream()
                        .filter(entry ->
                                entry.getStatus() ==
                                        QueueStatus.COMPLETED)
                        .count();

        long skippedPatients =
                today.stream()
                        .filter(entry ->
                                entry.getStatus() ==
                                        QueueStatus.SKIPPED)
                        .count();

        long cancelledPatients =
                today.stream()
                        .filter(entry ->
                                entry.getStatus() ==
                                        QueueStatus.CANCELLED)
                        .count();

        double averageEstimatedWaitMinutes =
                today.stream()
                        .map(QueueEntry::getEstimatedWaitMinutes)
                        .filter(wait -> wait != null)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

        return new DoctorStatisticsResponse(
                doctor.getId(),
                doctor.getName(),
                totalPatientsToday,
                waitingPatients,
                servingPatients,
                completedPatients,
                skippedPatients,
                cancelledPatients,
                Math.round(
                        averageEstimatedWaitMinutes * 10.0
                ) / 10.0
        );
    }

    // =========================================================
    // PATIENT DASHBOARD
    // =========================================================

    public PatientDashboardResponse
    getPatientDashboard(Long queueId) {

        QueueEntry entry =
                queueEntryRepository.findById(queueId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Queue entry not found"
                                ));

        Doctor doctor = entry.getDoctor();

        Long doctorId =
                doctor.getId();

        Integer currentToken = null;

        List<QueueEntry> servingPatients =
                queueEntryRepository
                        .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                doctorId,
                                QueueStatus.SERVING
                        );

        if (!servingPatients.isEmpty()) {

            currentToken =
                    servingPatients
                            .get(0)
                            .getTokenNumber();
        }

        int patientsAhead = 0;

        if (entry.getStatus() ==
                QueueStatus.WAITING) {

            patientsAhead =
                    (int) queueEntryRepository
                            .findByDoctorIdAndStatusOrderByTokenNumberAsc(
                                    doctorId,
                                    QueueStatus.WAITING
                            )
                            .stream()
                            .filter(queueEntry ->
                                    queueEntry
                                            .getTokenNumber()
                                            < entry.getTokenNumber()
                            )
                            .count();

            if (!servingPatients.isEmpty()) {
                patientsAhead++;
            }
        }

        int estimatedWaitMinutes =
                patientsAhead
                        * AVERAGE_CONSULTATION_MINUTES;

        if (entry.getStatus() ==
                QueueStatus.SERVING ||
                entry.getStatus() ==
                        QueueStatus.COMPLETED ||
                entry.getStatus() ==
                        QueueStatus.CANCELLED ||
                entry.getStatus() ==
                        QueueStatus.SKIPPED) {

            patientsAhead = 0;
            estimatedWaitMinutes = 0;
        }

        return new PatientDashboardResponse(
                entry.getId(),
                doctor.getId(),
                doctor.getName(),
                doctor.getClinic().getName(),
                entry.getPatientName(),
                entry.getTokenNumber(),
                currentToken,
                entry.getStatus().name(),
                patientsAhead,
                estimatedWaitMinutes
        );
    }

    // =========================================================
    // NEXT TOKEN
    // =========================================================

    private int getNextTokenNumber(
            Long doctorId) {

        List<QueueEntry> entries =
                queueEntryRepository
                        .findByDoctorIdOrderByTokenNumberAsc(
                                doctorId
                        );

        if (entries.isEmpty()) {
            return 1;
        }

        return entries
                .get(entries.size() - 1)
                .getTokenNumber() + 1;
    }

    // =========================================================
    // RESPONSE HELPERS
    // =========================================================

    private QueueResponse toResponse(
            QueueEntry entry,
            int patientsAhead) {

        return toResponse(
                entry,
                patientsAhead,
                entry.getEstimatedWaitMinutes()
        );
    }

    private QueueResponse toResponse(
            QueueEntry entry,
            int patientsAhead,
            int estimatedWaitMinutes) {

        Doctor doctor =
                entry.getDoctor();

        return new QueueResponse(
                entry.getId(),
                doctor.getId(),
                doctor.getName(),
                entry.getPatientName(),
                entry.getTokenNumber(),
                entry.getStatus(),
                entry.getJoinedAt(),
                patientsAhead,
                estimatedWaitMinutes
        );
    }
}