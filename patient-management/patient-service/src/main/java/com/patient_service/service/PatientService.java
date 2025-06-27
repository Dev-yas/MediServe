package com.patient_service.service;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.exception.EmailAlreadyExistsException;
import com.patient_service.exception.PatientNotFoundException;
import com.patient_service.grpc.BillingServiceGrpcClient;
import com.patient_service.mapper.PatientMapper;
import com.patient_service.model.Patient;
import com.patient_service.repository.PatientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO getPatientById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));
        return PatientMapper.toDTO(patient);
    }

    public PatientResponseDTO getPatientByName(String name) {
        Optional<Patient> patientOpt = patientRepository.findByName(name);
        Patient patient = patientOpt.orElseThrow(() ->
                new PatientNotFoundException("Patient not found with name: " + name));
        return PatientMapper.toDTO(patient);
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists: " + patientRequestDTO.getEmail()
            );
        }

        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO)
        );

        billingServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail()
        );

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));

        if (patientRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Email already in use: " + dto.getEmail());
        }

        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());
        patient.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));

        return PatientMapper.toDTO(patientRepository.save(patient));
    }

    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient not found with ID: " + id);
        }
        patientRepository.deleteById(id);
    }
}
