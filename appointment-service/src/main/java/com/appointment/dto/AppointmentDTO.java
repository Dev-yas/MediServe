package com.appointment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AppointmentDTO {
    private UUID patientId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private String timeSlot;
}
