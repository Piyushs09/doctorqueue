package com.doctorqueue.doctorqueue.controller;

import com.doctorqueue.doctorqueue.dto.DoctorResponse;
import com.doctorqueue.doctorqueue.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(
            DoctorService doctorService) {

        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorResponse> getAllDoctors() {

        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public DoctorResponse getDoctor(
            @PathVariable Long id) {

        return doctorService.getDoctorById(id);
    }

    @GetMapping("/clinic/{clinicId}")
    public List<DoctorResponse> getDoctorsByClinic(
            @PathVariable Long clinicId) {

        return doctorService.getDoctorsByClinic(
                clinicId
        );
    }
}