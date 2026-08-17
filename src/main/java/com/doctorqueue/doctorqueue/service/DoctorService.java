package com.doctorqueue.doctorqueue.service;

import com.doctorqueue.doctorqueue.dto.CreateDoctorRequest;
import com.doctorqueue.doctorqueue.dto.DoctorResponse;
import com.doctorqueue.doctorqueue.entity.Clinic;
import com.doctorqueue.doctorqueue.entity.Doctor;
import com.doctorqueue.doctorqueue.repository.ClinicRepository;
import com.doctorqueue.doctorqueue.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;

    public DoctorService(
            DoctorRepository doctorRepository,
            ClinicRepository clinicRepository) {

        this.doctorRepository = doctorRepository;
        this.clinicRepository = clinicRepository;
    }

    public Doctor createDoctor(
            CreateDoctorRequest request) {

        Clinic clinic =
                clinicRepository.findById(
                        request.getClinicId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Clinic not found"
                        )
                );

        Doctor doctor = new Doctor(
                request.getName(),
                request.getSpecialization(),
                request.getPhone(),
                clinic
        );

        return doctorRepository.save(doctor);
    }

    public List<DoctorResponse> getAllDoctors() {

        return doctorRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DoctorResponse getDoctorById(
            Long id) {

        Doctor doctor =
                doctorRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Doctor not found"
                                )
                        );

        return toResponse(doctor);
    }

    public List<DoctorResponse> getDoctorsByClinic(
            Long clinicId) {

        return doctorRepository
                .findByClinicId(clinicId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DoctorResponse toResponse(
            Doctor doctor) {

        Clinic clinic = doctor.getClinic();

        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getPhone(),
                clinic.getId(),
                clinic.getName()
        );
    }
}