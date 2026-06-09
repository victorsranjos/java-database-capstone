package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {
// 1. **Add @Service Annotation**:
//    - The `@Service` annotation is used to mark this class as a Spring service component. 
//    - It will be managed by Spring's container and used for business logic related to patients and appointments.
//    - Instruction: Ensure that the `@Service` annotation is applied above the class declaration.

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
// 2. **Constructor Injection for Dependencies**:
//    - The `PatientService` class has dependencies on `PatientRepository`, `AppointmentRepository`, and `TokenService`.
//    - These dependencies are injected via the constructor to maintain good practices of dependency injection and testing.
//    - Instruction: Ensure constructor injection is used for all the required dependencies.

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository, TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

// 3. **createPatient Method**:
//    - Creates a new patient in the database. It saves the patient object using the `PatientRepository`.
//    - If the patient is successfully saved, the method returns `1`; otherwise, it logs the error and returns `0`.
//    - Instruction: Ensure that error handling is done properly and exceptions are caught and logged appropriately.
    public int createPatient(Patient patient) {

        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

// 4. **getPatientAppointment Method**:
//    - Retrieves a list of appointments for a specific patient, based on their ID.
//    - The appointments are then converted into `AppointmentDTO` objects for easier consumption by the API client.
//    - This method is marked as `@Transactional` to ensure database consistency during the transaction.
//    - Instruction: Ensure that appointment data is properly converted into DTOs and the method handles errors gracefully.
    @Transactional
    public Map<String, Object> getPatientAppointment(
            Long patientId,
            String token) {

        Map<String, Object> response = new HashMap<>();

        try {

            String email = tokenService.extractIdentifier(token);

            Patient patient =
                    patientRepository.findByEmail(email);

            if (patient == null ||
                    !patient.getId().equals(patientId)) {

                response.put(
                        "message",
                        "Unauthorized"
                );

                return response;
            }

            List<AppointmentDTO> appointments =
                    appointmentRepository
                            .findByPatientId(patientId)
                            .stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());

            response.put("appointments", appointments);

            return response;

        } catch (Exception e) {

            response.put(
                    "message",
                    "Internal server error"
            );

            return response;
        }
    }

// 5. **filterByCondition Method**:
//    - Filters appointments for a patient based on the condition (e.g., "past" or "future").
//    - Retrieves appointments with a specific status (0 for future, 1 for past) for the patient.
//    - Converts the appointments into `AppointmentDTO` and returns them in the response.
//    - Instruction: Ensure the method correctly handles "past" and "future" conditions, and that invalid conditions are caught and returned as errors.
    @Transactional
    public Map<String, Object> filterByCondition(
            String condition,
            Long patientId) {

        Map<String, Object> response =
                new HashMap<>();

        try {

            int status;

            if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else {

                response.put(
                        "message",
                        "Invalid condition"
                );

                return response;
            }

            List<AppointmentDTO> appointments =
                    appointmentRepository
                            .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(
                                    patientId,
                                    status
                            )
                            .stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());

            response.put(
                    "appointments",
                    appointments
            );

            return response;

        } catch (Exception e) {

            response.put(
                    "message",
                    "Internal server error"
            );

            return response;
        }
    }

// 6. **filterByDoctor Method**:
//    - Filters appointments for a patient based on the doctor's name.
//    - It retrieves appointments where the doctor’s name matches the given value, and the patient ID matches the provided ID.
//    - Instruction: Ensure that the method correctly filters by doctor's name and patient ID and handles any errors or invalid cases.
    @Transactional
    public Map<String, Object> filterByDoctor(
            String doctorName,
            Long patientId) {

        Map<String, Object> response =
                new HashMap<>();

        try {

            List<AppointmentDTO> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientId(
                                    doctorName,
                                    patientId
                            )
                            .stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());

            response.put("appointments", appointments);
            return response;

        } catch (Exception e) {
            response.put("message", "Internal server error");
            return response;
        }
    }
// 7. **filterByDoctorAndCondition Method**:
//    - Filters appointments based on both the doctor's name and the condition (past or future) for a specific patient.
//    - This method combines filtering by doctor name and appointment status (past or future).
//    - Converts the appointments into `AppointmentDTO` objects and returns them in the response.
//    - Instruction: Ensure that the filter handles both doctor name and condition properly, and catches errors for invalid input.
    @Transactional
    public Map<String, Object> filterByDoctorAndCondition(
            String condition,
            String doctorName,
            Long patientId) {

        Map<String, Object> response =
                new HashMap<>();

        try {

            int status;

            if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else {

                response.put(
                        "message",
                        "Invalid condition"
                );

                return response;
            }

            List<AppointmentDTO> appointments =
                    appointmentRepository
                            .filterByDoctorNameAndPatientIdAndStatus(
                                    doctorName,
                                    patientId,
                                    status
                            )
                            .stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());

            response.put(
                    "appointments",
                    appointments
            );

            return response;

        } catch (Exception e) {

            response.put(
                    "message",
                    "Internal server error"
            );

            return response;
        }
    }

// 8. **getPatientDetails Method**:
//    - Retrieves patient details using the `tokenService` to extract the patient's email from the provided token.
//    - Once the email is extracted, it fetches the corresponding patient from the `patientRepository`.
//    - It returns the patient's information in the response body.
    //    - Instruction: Make sure that the token extraction process works correctly and patient details are fetched properly based on the extracted email.
public Map<String, Object> getPatientDetails(
        String token) {

    Map<String, Object> response =
            new HashMap<>();

    try {

        String email =
                tokenService.extractIdentifier(token);

        Patient patient =
                patientRepository.findByEmail(email);

        if (patient == null) {

            response.put("message", "Patient not found");

            return response;
        }

        response.put("patient", patient);

        return response;

    } catch (Exception e) {

        response.put("message", "Internal server error");

        return response;
    }
}

// 9. **Handling Exceptions and Errors**:
//    - The service methods handle exceptions using try-catch blocks and log any issues that occur. If an error occurs during database operations, the service responds with appropriate HTTP status codes (e.g., `500 Internal Server Error`).
//    - Instruction: Ensure that error handling is consistent across the service, with proper logging and meaningful error messages returned to the client.

// 10. **Use of DTOs (Data Transfer Objects)**:
//    - The service uses `AppointmentDTO` to transfer appointment-related data between layers. This ensures that sensitive or unnecessary data (e.g., password or private patient information) is not exposed in the response.
//    - Instruction: Ensure that DTOs are used appropriately to limit the exposure of internal data and only send the relevant fields to the client.
    private AppointmentDTO toDTO(
            Appointment appointment) {

        return new AppointmentDTO(
                appointment.getId(),

                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),

                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),

                appointment.getAppointmentTime(),

                appointment.getStatus()
        );
    }


}
