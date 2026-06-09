package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {
// 1. **Add @Service Annotation**:
//    - To indicate that this class is a service layer class for handling business logic.
//    - The `@Service` annotation should be added before the class declaration to mark it as a Spring service component.
//    - Instruction: Add `@Service` above the class definition.
    private AppointmentRepository appointmentRepository;
    private PatientRepository patientRepository;
    private final TokenService tokenService;

// 2. **Constructor Injection for Dependencies**:
//    - The `AppointmentService` class requires several dependencies like `AppointmentRepository`, `Service`, `TokenService`, `PatientRepository`, and `DoctorRepository`.
//    - These dependencies should be injected through the constructor.
//    - Instruction: Ensure constructor injection is used for proper dependency management in Spring.

    public AppointmentService(TokenService tokenService, PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

// 3. **Add @Transactional Annotation for Methods that Modify Database**:
//    - The methods that modify or update the database should be annotated with `@Transactional` to ensure atomicity and consistency of the operations.
//    - Instruction: Add the `@Transactional` annotation above methods that interact with the database, especially those modifying data.

// 4. **Book Appointment Method**:
//    - Responsible for saving the new appointment to the database.
//    - If the save operation fails, it returns `0`; otherwise, it returns `1`.
//    - Instruction: Ensure that the method handles any exceptions and returns an appropriate result code.
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
// 5. **Update Appointment Method**:
//    - This method is used to update an existing appointment based on its ID.
//    - It validates whether the patient ID matches, checks if the appointment is available for updating, and ensures that the doctor is available at the specified time.
//    - If the update is successful, it saves the appointment; otherwise, it returns an appropriate error message.
//    - Instruction: Ensure proper validation and error handling is included for appointment updates.
    @Transactional
    public String updateAppointment(Appointment appointment) {

        if (appointment.getId() == null) {
            return "Appointment ID is required";
        }

        Optional<Appointment> existing =
                appointmentRepository.findById(appointment.getId());

        if (existing.isEmpty()) {
            return "Appointment not found";
        }

        appointmentRepository.save(appointment);

        return "Appointment updated successfully";
    }

// 6. **Cancel Appointment Method**:
//    - This method cancels an appointment by deleting it from the database.
//    - It ensures the patient who owns the appointment is trying to cancel it and handles possible errors.
//    - Instruction: Make sure that the method checks for the patient ID match before deleting the appointment.
    @Transactional
    public String cancelAppointment(Long appointmentId, String token) {

        Optional<Appointment> optional =
                appointmentRepository.findById(appointmentId);

        if (optional.isEmpty()) {
            return "Appointment not found";
        }

        Appointment appointment = optional.get();

        String identifier = tokenService.extractIdentifier(token);

        Patient patient =
                patientRepository.findByEmail(identifier);

        if (patient == null) {
            return "Patient not found";
        }

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            return "Unauthorized";
        }

        appointmentRepository.delete(appointment);

        return "Appointment cancelled successfully";
    }

// 7. **Get Appointments Method**:
//    - This method retrieves a list of appointments for a specific doctor on a particular day, optionally filtered by the patient's name.
//    - It uses `@Transactional` to ensure that database operations are consistent and handled in a single transaction.
//    - Instruction: Ensure the correct use of transaction boundaries, especially when querying the database for appointments.
    @Transactional
    public Map<String, Object> getAppointment(
            Long doctorId,
            LocalDate date,
            String patientName) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> appointments;

        if (patientName == null || patientName.isBlank()) {

            appointments =
                    appointmentRepository
                            .findByDoctorIdAndAppointmentTimeBetween(
                                    doctorId,
                                    start,
                                    end
                            );

        } else {

            appointments =
                    appointmentRepository
                            .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                    doctorId,
                                    patientName,
                                    start,
                                    end
                            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);

        return response;
    }

    @Transactional
    public Map<String, Object> getAllAppointments(
            Long doctorId,
            String patientName) {

        List<Appointment> appointments;

        if (patientName == null || patientName.isBlank()) {
            appointments = appointmentRepository.findByDoctorId(doctorId);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCase(doctorId, patientName);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);

        return response;
    }
// 8. **Change Status Method**:
//    - This method updates the status of an appointment by changing its value in the database.
//    - It should be annotated with `@Transactional` to ensure the operation is executed in a single transaction.
//    - Instruction: Add `@Transactional` before this method to ensure atomicity when updating appointment status.
    @Transactional
    public String changeStatus(Long appointmentId, int status) {

        try {

            if (!appointmentRepository.existsById(appointmentId)) {
                return "Appointment not found";
            }

            appointmentRepository.updateStatus(status, appointmentId);

            return "Appointment status updated successfully";

        } catch (Exception e) {
            return "Failed to update appointment status";
        }
    }

}
