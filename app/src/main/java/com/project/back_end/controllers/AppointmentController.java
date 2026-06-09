package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    // GET /{doctorId}/{date}/{token}?patientName=...
    // FIX #3: patientName moved from @PathVariable (missing in URL) to @RequestParam
    @GetMapping("/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token,
            @RequestParam(required = false) String patientName
    ) {
        Map<String, Object> validation = service.validateToken(token, "doctor");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        if ("all".equalsIgnoreCase(date)) {
            return ResponseEntity.ok(
                    appointmentService.getAllAppointments(doctorId, patientName)
            );
        }

        return ResponseEntity.ok(
                appointmentService.getAppointment(doctorId, LocalDate.parse(date), patientName)
        );
    }

    // POST /{token}
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> createAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token
    ) {
        Map<String, Object> tokenValidation = service.validateToken(token, "patient");

        if (!tokenValidation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenValidation);
        }

        int patientValidation = service.validatePatient(appointment.getPatient());

        if (patientValidation == 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Patient already exists"));
        }

        int appointmentValidation = service.validateAppointment(appointment);

        if (appointmentValidation == -1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Doctor not found"));
        }

        if (appointmentValidation == 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Appointment unavailable"));
        }

        int result = appointmentService.bookAppointment(appointment);

        if (result == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create appointment"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Appointment created successfully"));
    }

    // PUT /{token} — FIX #11: wires up AppointmentService.updateAppointment (was missing)
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @RequestBody Appointment appointment,
            @PathVariable String token
    ) {
        Map<String, Object> validation = service.validateToken(token, "patient");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        String result = appointmentService.updateAppointment(appointment);

        return switch (result) {
            case "Appointment updated successfully" ->
                    ResponseEntity.ok(Map.of("message", result));
            case "Appointment not found" ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", result));
            default ->
                    ResponseEntity.badRequest().body(Map.of("message", result));
        };
    }

    // DELETE /{appointmentId}/{token} — FIX #11: wires up AppointmentService.cancelAppointment (was missing)
    @DeleteMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long appointmentId,
            @PathVariable String token
    ) {
        Map<String, Object> validation = service.validateToken(token, "patient");

        if (!validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validation);
        }

        String result = appointmentService.cancelAppointment(appointmentId, token);

        return switch (result) {
            case "Appointment cancelled successfully" ->
                    ResponseEntity.ok(Map.of("message", result));
            case "Appointment not found" ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", result));
            case "Unauthorized" ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", result));
            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", result));
        };
    }
}
