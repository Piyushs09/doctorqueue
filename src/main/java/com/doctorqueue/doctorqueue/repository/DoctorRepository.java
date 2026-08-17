package com.doctorqueue.doctorqueue.repository;

import com.doctorqueue.doctorqueue.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long> {

    List<Doctor> findByClinicId(Long clinicId);
}