package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

// 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to define it as a REST controller that serves JSON responses.
//    - Use `@RequestMapping("${api.path}doctor")` to prefix all endpoints with a configurable API path followed by "doctor".
//    - This class manages doctor-related functionalities such as registration, login, updates, and availability.


// 2. Autowire Dependencies:
//    - Inject `DoctorService` for handling the core logic related to doctors (e.g., CRUD operations, authentication).
//    - Inject the shared `Service` class for general-purpose features like token validation and filtering.
    private DoctorService doctorService;
    private Service service;

    public DoctorController (DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

// 3. Define the `getDoctorAvailability` Method:
//    - Handles HTTP GET requests to check a specific doctor’s availability on a given date.
//    - Requires `user` type, `doctorId`, `date`, and `token` as path variables.
//    - First validates the token against the user type.
//    - If the token is invalid, returns an error response; otherwise, returns the availability status for the doctor.
    @GetMapping("/availability/{role}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String role,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token
    ) {

        Map<String, Object> validation = service.validateToken(token, role);

        if (!validation.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(validation);
        }

        Map<String, Object> response =
                Map.of("availability", doctorService.getDoctorAvailability(doctorId, LocalDate.parse(date)));

        return ResponseEntity.ok(response);
    }

// 4. Define the `getDoctor` Method:
//    - Handles HTTP GET requests to retrieve a list of all doctors.
//    - Returns the list within a response map under the key `"doctors"` with HTTP 200 OK status.
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {

        Map<String, Object> response = new HashMap<>();

        response.put("doctors", doctorService.getDoctors());

        return ResponseEntity.ok(response);
    }

// 5. Define the `saveDoctor` Method:
//    - Handles HTTP POST requests to register a new doctor.
//    - Accepts a validated `Doctor` object in the request body and a token for authorization.
//    - Validates the token for the `"admin"` role before proceeding.
//    - If the doctor already exists, returns a conflict response; otherwise, adds the doctor and returns a success message.
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, Object> validation = service.validateToken(token, "admin");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.saveDoctor(doctor);

        if (result == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Doctor already exists"));
        }

        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Doctor added to db"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Some internal error occurred"));
    }

// 6. Define the `doctorLogin` Method:
//    - Handles HTTP POST requests for doctor login.
//    - Accepts a validated `Login` DTO containing credentials.
//    - Delegates authentication to the `DoctorService` and returns login status and token information.
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(
            @RequestBody Login login) {

        return ResponseEntity.ok(
                doctorService.validateDoctor(login)
        );
    }

// 7. Define the `updateDoctor` Method:
//    - Handles HTTP PUT requests to update an existing doctor's information.
//    - Accepts a validated `Doctor` object and a token for authorization.
//    - Token must belong to an `"admin"`.
//    - If the doctor exists, updates the record and returns success; otherwise, returns not found or error messages.
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, Object> validation = service.validateToken(token, "admin");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.updateDoctor(doctor);

        if (result == 1) {
            return ResponseEntity.ok(
                    Map.of("message", "Doctor updated")
            );
        }

        if (result == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Some internal error occurred"));
    }

// 8. Define the `deleteDoctor` Method:
//    - Handles HTTP DELETE requests to remove a doctor by ID.
//    - Requires both doctor ID and an admin token as path variables.
//    - If the doctor exists, deletes the record and returns a success message; otherwise, responds with a not found or error message.
    @DeleteMapping("/{doctorId}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(
            @PathVariable Long doctorId,
            @PathVariable String token) {

        Map<String, Object> validation = service.validateToken(token, "admin");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        int result = doctorService.deleteDoctor(doctorId);

        if (result == 1) {
            return ResponseEntity.ok(
                    Map.of("message", "Doctor deleted successfully")
            );
        }

        if (result == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not found with id"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Some internal error occurred"));
    }

// 9. Define the `filter` Method:
//    - Handles HTTP GET requests to filter doctors based on name, time, and specialty.
//    - Accepts `name`, `time`, and `speciality` as path variables.
//    - Calls the shared `Service` to perform filtering logic and returns matching doctors in the response.
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String specialty) {

        return ResponseEntity.ok(
                service.filterDoctor(name, specialty, time)
        );
    }

}
