package com.doctorqueue.doctorqueue.repository;

import com.doctorqueue.doctorqueue.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
}
