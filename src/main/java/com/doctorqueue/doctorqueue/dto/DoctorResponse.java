package com.doctorqueue.doctorqueue.dto;

public class DoctorResponse {

    private Long id;
    private String name;
    private String specialization;
    private String phone;

    private Long clinicId;
    private String clinicName;

    public DoctorResponse() {
    }

    public DoctorResponse(
            Long id,
            String name,
            String specialization,
            String phone,
            Long clinicId,
            String clinicName) {

        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.clinicId = clinicId;
        this.clinicName = clinicName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhone() {
        return phone;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public String getClinicName() {
        return clinicName;
    }
}
