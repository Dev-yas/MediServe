package com.appointment.controller;

import com.appointment.dto.AppointmentDTO;
import com.appointment.model.Appointment;
import com.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService service;

    @PostMapping
    public ResponseEntity<Appointment> book(@RequestBody AppointmentDTO dto) {
        Appointment appt = service.bookAppointment(dto.getPatientId(), dto.getDoctorId(), dto.getAppointmentDate(), dto.getTimeSlot());
        return ResponseEntity.ok(appt);
    }
}
