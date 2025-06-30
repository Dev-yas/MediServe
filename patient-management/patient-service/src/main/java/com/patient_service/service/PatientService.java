package com.patient_service.service;

import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientResponseDTO;
import com.patient_service.exception.EmailAlreadyExistsException;
import com.patient_service.grpc.BillingServiceGrpcClient;
import com.patient_service.grpc.NotificationGrpcClient;
import com.patient_service.kafka.KafkaProducer;
import com.patient_service.mapper.PatientMapper;
import com.patient_service.model.Patient;
import com.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final NotificationGrpcClient notificationGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(
            PatientRepository patientRepository,
            BillingServiceGrpcClient billingServiceGrpcClient,
            NotificationGrpcClient notificationGrpcClient,
            KafkaProducer kafkaProducer
    ) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.notificationGrpcClient = notificationGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    // ✅ Create Patient
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists: " + patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        // gRPC call to billing
        billingServiceGrpcClient.createBillingAccount(
                newPatient.getId().toString(),
                newPatient.getName(),
                newPatient.getEmail()
        );

        // gRPC call to notification
        String subject = "Welcome, " + newPatient.getName() + "!";
        String body = String.format("Hi %s,\n\nYour account has been created with Patient ID: %s.",
                newPatient.getName(), newPatient.getId());

        notificationGrpcClient.sendNotification(newPatient.getEmail(), subject, body);

        // ✅ Kafka event
        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    // ✅ Get all patients
    public List<PatientResponseDTO> getPatients() {
        return patientRepository.findAll()
                .stream()
                .map(PatientMapper::toDTO)
                .toList();
    }

    // ✅ Get patient by ID
    public PatientResponseDTO getPatientById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        return PatientMapper.toDTO(patient);
    }

    // ✅ Get patient by name
    public PatientResponseDTO getPatientByName(String name) {
        Patient patient = patientRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Patient not found with name: " + name));
        return PatientMapper.toDTO(patient);
    }

    // ✅ Update patient
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO dto) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        // Update other fields as needed

        Patient updated = patientRepository.save(existing);
        return PatientMapper.toDTO(updated);
    }

    // ✅ Delete patient
    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
    }
}
