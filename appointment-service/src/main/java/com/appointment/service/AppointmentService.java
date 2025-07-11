package com.appointment.service;

import com.appointment.feign.PatientClient;
import com.appointment.model.Appointment;
import com.appointment.model.Doctor;
import com.appointment.repository.AppointmentRepository;
import com.appointment.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientClient patientClient;

    private static final List<String> TIME_SLOTS = List.of(
            "09:00-09:30", "09:30-10:00", "10:00-10:30", "10:30-11:00",
            "11:00-11:30", "11:30-12:00", "12:00-12:30"
    );

    public Appointment bookAppointment(UUID patientId, Long doctorId, LocalDate date, String requestedSlot) {
        // Validate patient exists via Feign client
        try {
            patientClient.getPatientById(patientId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found or Patient Service unreachable");
        }

        // Fetch doctor
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

        // Find available slot from requested onward
        for (String slot : TIME_SLOTS) {
            if (slot.compareTo(requestedSlot) < 0) continue;

            int count = appointmentRepo.countByDoctorAndAppointmentDateAndTimeSlot(doctor, date, slot);
            if (count < 2) {
                Appointment appt = new Appointment();
                appt.setDoctor(doctor);
                appt.setPatientId(patientId);
                appt.setAppointmentDate(date);
                appt.setTimeSlot(slot);
                return appointmentRepo.save(appt);
            }
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "No available time slot");
    }
}
