# Schema Design

## MySQL Database Design

### Table: patients

- id: INT, Primary Key, AUTO_INCREMENT
- first_name: VARCHAR(100), NOT NULL
- last_name: VARCHAR(100), NOT NULL
- email: VARCHAR(255), UNIQUE, NOT NULL
- phone: VARCHAR(20), UNIQUE, NOT NULL
- date_of_birth: DATE, NOT NULL
- address: TEXT
- emergency_contact: VARCHAR(255)
- created_at: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP

**Notes:**
- Email and phone format validation should be handled in application code.
- Patient records should not be physically deleted in most cases; use soft deletion if required to preserve medical history.

### Table: doctors

- id: INT, Primary Key, AUTO_INCREMENT
- first_name: VARCHAR(100), NOT NULL
- last_name: VARCHAR(100), NOT NULL
- email: VARCHAR(255), UNIQUE, NOT NULL
- phone: VARCHAR(20), UNIQUE, NOT NULL
- specialization: VARCHAR(150), NOT NULL
- license_number: VARCHAR(100), UNIQUE, NOT NULL
- status: ENUM('active', 'inactive'), DEFAULT 'active'
- created_at: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP

**Notes:**
- License numbers must be unique.
- Doctors may have associated availability schedules.

### Table: appointments

- id: INT, Primary Key, AUTO_INCREMENT
- doctor_id: INT, Foreign Key → doctors(id), NOT NULL
- patient_id: INT, Foreign Key → patients(id), NOT NULL
- appointment_time: DATETIME, NOT NULL
- duration_minutes: INT, NOT NULL
- status: INT, NOT NULL (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- reason_for_visit: VARCHAR(500)
- created_at: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP

**Constraints & Notes:**
- Prevent overlapping appointments for the same doctor using application logic and scheduling checks.
- Patient appointment history should be retained permanently for auditing and medical records.
- If a patient is deleted, appointments should remain archived or patient deletion should be restricted.
- ON DELETE RESTRICT is recommended for doctor_id and patient_id.

### Table: admin

- id: INT, Primary Key, AUTO_INCREMENT
- username: VARCHAR(100), UNIQUE, NOT NULL
- email: VARCHAR(255), UNIQUE, NOT NULL
- password_hash: VARCHAR(255), NOT NULL
- role: VARCHAR(50), NOT NULL
- last_login: DATETIME
- created_at: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP

### Table: clinic_locations

- id: INT, Primary Key, AUTO_INCREMENT
- location_name: VARCHAR(150), NOT NULL
- address: TEXT, NOT NULL
- phone: VARCHAR(20)
- operating_hours: VARCHAR(255)
- created_at: TIMESTAMP, DEFAULT CURRENT_TIMESTAMP

### Table: payments

- id: INT, Primary Key, AUTO_INCREMENT
- appointment_id: INT, Foreign Key → appointments(id), NOT NULL
- amount: DECIMAL(10,2), NOT NULL
- payment_method: ENUM('cash', 'card', 'insurance', 'online'), NOT NULL
- payment_status: ENUM('pending', 'paid', 'refunded'), NOT NULL
- transaction_reference: VARCHAR(255), UNIQUE
- paid_at: DATETIME

### Table: doctor_availability

- id: INT, Primary Key, AUTO_INCREMENT
- doctor_id: INT, Foreign Key → doctors(id), NOT NULL
- day_of_week: TINYINT, NOT NULL
- start_time: TIME, NOT NULL
- end_time: TIME, NOT NULL

**Notes:**
- Defines doctor availability separately from appointments.
- Helps enforce scheduling rules and prevent invalid bookings.

### Design Decisions

- Appointment records are retained indefinitely for legal, auditing, and medical-history purposes.
- Prescriptions should be tied to a specific appointment to maintain treatment context.
- Doctors cannot have overlapping appointments.
- Sensitive authentication data is stored only as password hashes.
- Email and phone format validation should be implemented in application code and API validation layers.

## MongoDB Collection Design

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "appointmentId": 51,
  "patientId": 12,
  "doctorId": 7,
  "createdAt": "2026-06-03T10:30:00Z",
  "medications": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Every 6 hours",
      "durationDays": 5
    }
  ],
  "doctorNotes": "Patient should rest and stay hydrated.",
  "tags": ["fever", "follow-up"],
  "attachments": [
    {
      "fileName": "blood-test.pdf",
      "fileType": "application/pdf",
      "uploadedAt": "2026-06-03T10:35:00Z"
    }
  ],
  "metadata": {
    "refillCount": 2,
    "priority": "normal",
    "requiresFollowUp": true
  },
  "pharmacy": {
    "name": "City Pharmacy",
    "location": "Downtown Branch"
  }
}
```

### MongoDB Design Notes

- Store references to patientId, doctorId, and appointmentId rather than embedding full MySQL records to avoid duplication.
- MongoDB's flexible schema allows new prescription fields to be added without altering database tables.
- Arrays are useful for multiple medications and attachments.
- Nested metadata supports evolving requirements.
- Additional collections such as feedback, messages, and activity_logs can follow a similar document-oriented structure.
