package com.doctorqueue.doctorqueue.repository;

import com.doctorqueue.doctorqueue.entity.QueueEntry;
import com.doctorqueue.doctorqueue.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository
        extends JpaRepository<QueueEntry, Long> {

    Optional<QueueEntry> findByIdAndPatientId(
            Long id,
            Long patientId
    );

    Optional<QueueEntry>
    findFirstByPatientIdAndStatusInOrderByJoinedAtDesc(
            Long patientId,
            List<QueueStatus> statuses
    );

    Optional<QueueEntry>
    findFirstByPatientIdAndDoctorIdAndStatusInOrderByJoinedAtDesc(
            Long patientId,
            Long doctorId,
            List<QueueStatus> statuses
    );

    // =========================================================
    // DOCTOR QUEUE
    // =========================================================

    List<QueueEntry> findByDoctorIdAndStatus(
            Long doctorId,
            QueueStatus status
    );

    long countByDoctorIdAndStatus(
            Long doctorId,
            QueueStatus status
    );

    List<QueueEntry> findByDoctorIdOrderByTokenNumberAsc(
            Long doctorId
    );

    List<QueueEntry>
    findByDoctorIdAndStatusOrderByTokenNumberAsc(
            Long doctorId,
            QueueStatus status
    );

    Optional<QueueEntry>
    findFirstByDoctorIdAndStatusOrderByTokenNumberAsc(
            Long doctorId,
            QueueStatus status
    );

    // =========================================================
    // TODAY
    // =========================================================

    @Query("""
            SELECT q
            FROM QueueEntry q
            WHERE q.doctor.id = :doctorId
              AND q.joinedAt >= :startOfDay
              AND q.joinedAt < :endOfDay
            ORDER BY q.tokenNumber ASC
            """)
    List<QueueEntry> findTodayByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
            SELECT q
            FROM QueueEntry q
            WHERE q.doctor.id = :doctorId
              AND q.status = :status
              AND q.joinedAt >= :startOfDay
              AND q.joinedAt < :endOfDay
            ORDER BY q.tokenNumber ASC
            """)
    List<QueueEntry> findByDoctorIdAndStatusAndJoinedAtBetween(
            @Param("doctorId") Long doctorId,
            @Param("status") QueueStatus status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
            SELECT q
            FROM QueueEntry q
            WHERE q.doctor.id = :doctorId
              AND q.status =
                  com.doctorqueue.doctorqueue.entity.QueueStatus.COMPLETED
              AND q.joinedAt >= :startOfDay
              AND q.joinedAt < :endOfDay
            ORDER BY q.tokenNumber ASC
            """)
    List<QueueEntry> findCompletedToday(
            @Param("doctorId") Long doctorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}