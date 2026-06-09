package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {
// 1. **@Service Annotation**
// The @Service annotation marks this class as a service component in Spring. This allows Spring to automatically detect it through component scanning
// and manage its lifecycle, enabling it to be injected into controllers or other services using @Autowired or constructor injection.
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

// 2. **Constructor Injection for Dependencies**
// The constructor injects all required dependencies (TokenService, Repositories, and other Services). This approach promotes loose coupling, improves testability,
// and ensures that all required dependencies are provided at object creation time.

    public Service(TokenService tokenService, AdminRepository adminRepository, DoctorRepository doctorRepository, PatientRepository patientRepository, DoctorService doctorService, PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

// 3. **validateToken Method**
// This method checks if the provided JWT token is valid for a specific user. It uses the TokenService to perform the validation.
// If the token is invalid or expired, it returns a 401 Unauthorized response with an appropriate error message. This ensures security by preventing
// unauthorized access to protected resources.
    public Map<String, Object> validateToken(
            String token,
            String role
    ) {

        Map<String, Object> response = new HashMap<>();

        if (!tokenService.validateToken(token, role)) {

            response.put(
                    "message",
                    "Invalid or expired token"
            );
        }

        return response;
    }

// 4. **validateAdmin Method**
// This method validates the login credentials for an admin user.
// - It first searches the admin repository using the provided username.
// - If an admin is found, it checks if the password matches.
// - If the password is correct, it generates and returns a JWT token (using the admin’s username) with a 200 OK status.
// - If the password is incorrect, it returns a 401 Unauthorized status with an error message.
// - If no admin is found, it also returns a 401 Unauthorized.
// - If any unexpected error occurs during the process, a 500 Internal Server Error response is returned.
// This method ensures that only valid admin users can access secured parts of the system.
    public Map<String, Object> validateAdmin(
            Login login
    ) {

        Map<String, Object> response = new HashMap<>();

        try {

            Admin admin =
                    adminRepository.findByUsername(
                            login.getIdentifier()
                    );

            if (admin == null) {

                response.put(
                        "message",
                        "Admin not found"
                );

                return response;
            }

            if (!admin.getPassword()
                    .equals(login.getPassword())) {

                response.put(
                        "message",
                        "Invalid password"
                );

                return response;
            }

            response.put(
                    "token",
                    tokenService.generateToken(
                            admin.getUsername()
                    )
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

// 5. **filterDoctor Method**
// This method provides filtering functionality for doctors based on name, specialty, and available time slots.
// - It supports various combinations of the three filters.
// - If none of the filters are provided, it returns all available doctors.
// This flexible filtering mechanism allows the frontend or consumers of the API to search and narrow down doctors based on user criteria.
    public Map<String, Object> filterDoctor(
            String name,
            String specialty,
            String time
    ) {

        if (name != null &&
                !name.isBlank() &&
                specialty != null &&
                !specialty.isBlank() &&
                time != null &&
                !time.isBlank()) {

            return doctorService
                    .filterDoctorsByNameSpecilityAndTime(
                            name,
                            specialty,
                            time
                    );
        }

        if (name != null &&
                !name.isBlank() &&
                specialty != null &&
                !specialty.isBlank()) {

            return doctorService
                    .filterDoctorByNameAndSpecility(
                            name,
                            specialty
                    );
        }

        if (name != null &&
                !name.isBlank() &&
                time != null &&
                !time.isBlank()) {

            return doctorService
                    .filterDoctorByNameAndTime(
                            name,
                            time
                    );
        }

        if (specialty != null &&
                !specialty.isBlank() &&
                time != null &&
                !time.isBlank()) {

            return doctorService
                    .filterDoctorByTimeAndSpecility(
                            specialty,
                            time
                    );
        }

        if (name != null &&
                !name.isBlank()) {

            return doctorService
                    .findDoctorByName(name);
        }

        if (specialty != null &&
                !specialty.isBlank()) {

            return doctorService
                    .filterDoctorBySpecility(
                            specialty
                    );
        }

        if (time != null &&
                !time.isBlank()) {

            return doctorService
                    .filterDoctorsByTime(
                            time
                    );
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "doctors",
                doctorService.getDoctors()
        );

        return response;
    }

// 6. **validateAppointment Method**
// This method validates if the requested appointment time for a doctor is available.
// - It first checks if the doctor exists in the repository.
// - Then, it retrieves the list of available time slots for the doctor on the specified date.
// - It compares the requested appointment time with the start times of these slots.
// - If a match is found, it returns 1 (valid appointment time).
// - If no matching time slot is found, it returns 0 (invalid).
// - If the doctor doesn’t exist, it returns -1.
// This logic prevents overlapping or invalid appointment bookings.
    public int validateAppointment(
            Appointment appointment
    ) {

        Doctor doctor =
                doctorRepository.findById(
                        appointment.getDoctor().getId()
                ).orElse(null);

        if (doctor == null) {
            return -1;
        }

        LocalDate date =
                appointment
                        .getAppointmentTime()
                        .toLocalDate();

        List<String> availableSlots =
                doctorService
                        .getDoctorAvailability(
                                doctor.getId(),
                                date
                        );

        String requestedTime =
                appointment
                        .getAppointmentTime()
                        .toLocalTime()
                        .toString();

        return availableSlots.contains(requestedTime)
                ? 1
                : 0;
    }

// 7. **validatePatient Method**
// This method checks whether a patient with the same email or phone number already exists in the system.
// - If a match is found, it returns false (indicating the patient is not valid for new registration).
// - If no match is found, it returns true.
// This helps enforce uniqueness constraints on patient records and prevent duplicate entries.
    public int validatePatient(
            Patient patient
    ) {

        Patient existingPatient =
                patientRepository.findByEmailOrPhone(
                        patient.getEmail(),
                        patient.getPhone()
                );

        if (existingPatient != null) {
            return 0;
        }

        return 1;
    }

        public Map<String, Object> validatePatientLogin(
                Login login
        ) {

            Map<String, Object> response =
                    new HashMap<>();

            try {

                Patient patient =
                        patientRepository.findByEmail(
                                login.getIdentifier()
                        );

                if (patient == null) {

                    response.put(
                            "message",
                            "Patient not found"
                    );

                    return response;
                }

                if (!patient.getPassword()
                        .equals(login.getPassword())) {

                    response.put(
                            "message",
                            "Invalid password"
                    );

                    return response;
                }

                response.put(
                        "token",
                        tokenService.generateToken(
                                patient.getEmail()
                        )
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

// 8. **validatePatientLogin Method**
// This method handles login validation for patient users.
// - It looks up the patient by email.
// - If found, it checks whether the provided password matches the stored one.
// - On successful validation, it generates a JWT token and returns it with a 200 OK status.
// - If the password is incorrect or the patient doesn't exist, it returns a 401 Unauthorized with a relevant error.
// - If an exception occurs, it returns a 500 Internal Server Error.
// This method ensures only legitimate patients can log in and access their data securely.

// 9. **filterPatient Method**
// This method filters a patient's appointment history based on condition and doctor name.
// - It extracts the email from the JWT token to identify the patient.
// - Depending on which filters (condition, doctor name) are provided, it delegates the filtering logic to PatientService.
// - If no filters are provided, it retrieves all appointments for the patient.
// This flexible method supports patient-specific querying and enhances user experience on the client side.
    public Map<String, Object> filterPatient(
            String condition,
            String doctorName,
            String token
    ) {

        String email =
                tokenService.extractIdentifier(token);

        Patient patient =
                patientRepository.findByEmail(
                        email
                );

        if (patient == null) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Patient not found"
            );

            return response;
        }

        if (condition != null &&
                !condition.isBlank() &&
                doctorName != null &&
                !doctorName.isBlank()) {

            return patientService
                    .filterByDoctorAndCondition(
                            condition,
                            doctorName,
                            patient.getId()
                    );
        }

        if (condition != null &&
                !condition.isBlank()) {

            return patientService
                    .filterByCondition(
                            condition,
                            patient.getId()
                    );
        }

        if (doctorName != null &&
                !doctorName.isBlank()) {

            return patientService
                    .filterByDoctor(
                            doctorName,
                            patient.getId()
                    );
        }

        return patientService
                .getPatientAppointment(
                        patient.getId(),
                        token
                );
        }
}
