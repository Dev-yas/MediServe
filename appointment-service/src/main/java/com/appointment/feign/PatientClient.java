package com.appointment.feign;

import com.appointment.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "patient-service")
public interface PatientClient {
    @GetMapping("/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") UUID id);
}
