/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

/**
 * Global variables
 */
const patientTableBody = document.getElementById("patientTableBody");

let selectedDate = new Date().toISOString().split("T")[0];
const token = localStorage.getItem("token");
let patientName = null;

/**
 * Search functionality
 */
const searchBar = document.getElementById("searchBar");

if (searchBar) {
  searchBar.addEventListener("input", () => {
    patientName = searchBar.value.trim();

    if (!patientName) {
      patientName = "null";
    }

    loadAppointments();
  });
}

/**
 * Today's appointments button
 */
const todayButton = document.getElementById("todayButton");

if (todayButton) {
  todayButton.addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];

    const datePicker = document.getElementById("datePicker");
    if (datePicker) {
      datePicker.value = selectedDate;
    }

    loadAppointments();
  });
}

/**
 * All appointments button
 */
const allButton = document.getElementById("allButton");

if (allButton) {
  allButton.addEventListener("click", () => {
    selectedDate = "all";

    const datePicker = document.getElementById("datePicker");
    if (datePicker) {
      datePicker.value = "";
    }

    loadAppointments();
  });
}

/**
 * Date picker filter
 */
const datePicker = document.getElementById("datePicker");

if (datePicker) {
  datePicker.value = selectedDate;

  datePicker.addEventListener("change", (event) => {
    selectedDate = event.target.value;
    loadAppointments();
  });
}

/**
 * Load appointments
 */
async function loadAppointments() {
  try {
    const data = await getAllAppointments(
      selectedDate,
      patientName,
      token
    );
    const appointments = data.appointments || [];

    if (!patientTableBody) return;

    patientTableBody.innerHTML = "";

    if (!appointments || appointments.length === 0) {
      const message = selectedDate === "all" ? "No Appointments found" : "No Appointments found for today";
      patientTableBody.innerHTML = `
        <tr>
          <td colspan="100%" class="text-center">
            ${message}
          </td>
        </tr>
      `;
      return;
    }

    appointments.forEach((appointment) => {
      const patient =
        appointment.patient ||
        appointment.patientData ||
        appointment;

      const row = createPatientRow(patient, appointment.id);

      patientTableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading appointments:", error);

    if (patientTableBody) {
      patientTableBody.innerHTML = `
        <tr>
          <td colspan="100%" class="text-center">
            Unable to load appointments
          </td>
        </tr>
      `;
    }
  }
}

/**
 * Initial page load
 */
document.addEventListener("DOMContentLoaded", () => {
  if (typeof renderContent === "function") {
    renderContent();
  }

  loadAppointments();
});