package com.doctorqueue.doctorqueue.controller;

import com.doctorqueue.doctorqueue.entity.Clinic;
import com.doctorqueue.doctorqueue.repository.ClinicRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@CrossOrigin(origins = "http://localhost:5173")
public class ClinicController {

    private final ClinicRepository clinicRepository;

    public ClinicController(
            ClinicRepository clinicRepository) {

        this.clinicRepository = clinicRepository;
    }

    @GetMapping
    public List<Clinic> getAllClinics() {

        return clinicRepository.findAll();
    }
}