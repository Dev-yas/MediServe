package com.appointment.repository;

import com.appointment.model.Appointment;
import com.appointment.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    int countByDoctorAndAppointmentDateAndTimeSlot(Doctor doctor, LocalDate date, String timeSlot);
}

