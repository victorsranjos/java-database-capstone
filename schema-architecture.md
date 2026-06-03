## Architecture Summary

This Spring Boot application follows a three-tier architecture that combines both MVC and RESTful design patterns. MVC controllers are used to render the Admin and Doctor dashboards through Thymeleaf templates, providing a dynamic server-side user interface. For all other system functionalities, REST controllers expose APIs that handle client requests and return data in a structured format.

The application uses two databases to manage different types of data. MySQL stores core relational data such as patients, doctors, appointments, and administrators using JPA entities and repositories, while MongoDB manages prescription records through document-based models. All requests pass through a centralized service layer that contains the business logic and coordinates communication between controllers and the corresponding repositories. This architecture promotes separation of concerns, making the application easier to maintain, extend, and scale.

## Numbered flow of data and control

**1. User Modules**
- Users interact with different application modules.
- **AdminDashboard** and **DoctorDashboard** are web-based dashboards rendered using Thymeleaf templates.
- **Appointments**, **PatientDashboard**, and **PatientRecord** modules communicate through REST APIs and exchange data in JSON format.

**2. Controllers Layer**
- Requests from AdminDashboard and DoctorDashboard are handled by **Thymeleaf Controllers**.
- Requests from the REST modules are handled by **REST Controllers**.
- Controllers act as the entry point into the Spring Boot application.

**3. Service Layer**
- Both Thymeleaf Controllers and REST Controllers delegate processing to a common **Service Layer**.
- The Service Layer contains the application's business logic and coordinates data access operations.
- This layer separates presentation logic from data access logic.

**4. Repository Layer**
- The Service Layer interacts with the appropriate repositories:
  - **MySQL Repositories** for relational data.
  - **MongoDB Repository** for prescription-related data.
- Repositories abstract database operations from the service layer.

**5. MySQL Database**
- MySQL Repositories access the **MySQL Database**.
- This database stores structured application data such as patients, doctors, appointments, and administrators.

**6. MySQL Models**
- Data stored in MySQL is represented by JPA entity classes.
- These entities are mapped to database tables and include:
  - Patient
  - Doctor
  - Appointment
  - Admin
- The entities collectively form the application's MySQL domain model.

**7. Domain Models**
- The application maintains two model groups:
  - **MySQL Models** (JPA Entities) for relational data.
  - **MongoDB Models** (Documents) for prescription data.
- The MongoDB Repository accesses the MongoDB Database using the **Prescription** document model.
- These models provide the object representation used throughout the service and repository layers.

<img width="1405" height="813" alt="image" src="https://github.com/user-attachments/assets/8f45c662-0d80-4779-b4f5-045ed1c02060" />
