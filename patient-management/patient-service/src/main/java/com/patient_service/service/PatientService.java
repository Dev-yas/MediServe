package com.patient_service.service;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.exception.EmailAlreadyExistsException;
import com.patient_service.exception.PatientNotFoundException;
import com.patient_service.grpc.BillingServiceGrpcClient;
import com.patient_service.grpc.NotificationGrpcClient;
import com.patient_service.mapper.PatientMapper;
import com.patient_service.model.Patient;
import com.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final NotificationGrpcClient notificationGrpcClient;

    public PatientService(
            PatientRepository patientRepository,
            BillingServiceGrpcClient billingServiceGrpcClient,
            NotificationGrpcClient notificationGrpcClient) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.notificationGrpcClient = notificationGrpcClient;
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

        // Call billing service
        billingServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail()
        );

        // Send email notification
        String subject = "Welcome, " + newPatient.getName() + "!";
        String body = String.format(
                "Hi %s,\n\nYour account has been successfully created with Patient ID: %s.\n\nThank you for registering with us!",
                newPatient.getName(), newPatient.getId()
        );

        notificationGrpcClient.sendNotification(newPatient.getEmail(), subject, body);

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
