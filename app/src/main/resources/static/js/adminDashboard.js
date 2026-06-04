/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
import { openModal } from "./components/modals.js";
import {
  getDoctors,
  filterDoctors,
  saveDoctor,
} from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

/**
 * Open Add Doctor Modal
 */
const addDoctorBtn = document.getElementById("addDocBtn");

if (addDoctorBtn) {
  addDoctorBtn.addEventListener("click", () => {
    openModal("addDoctor");
  });
}

/**
 * Load doctors when page loads
 */
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  document
    .getElementById("searchBar")
    ?.addEventListener("input", filterDoctorsOnChange);

  document
    .getElementById("filterTime")
    ?.addEventListener("change", filterDoctorsOnChange);

  document
    .getElementById("filterSpecialty")
    ?.addEventListener("change", filterDoctorsOnChange);
});

/**
 * Fetch and display all doctors
 */
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Error loading doctors:", error);
  }
}

/**
 * Render doctor cards
 */
function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");

  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML = "<p>No doctors found</p>";
    return;
  }

  doctors.forEach((doctor) => {
    const doctorCard = createDoctorCard(doctor);
    contentDiv.appendChild(doctorCard);
  });
}

/**
 * Search and filter doctors
 */
async function filterDoctorsOnChange() {
  try {
    const name =
      document.getElementById("searchBar")?.value?.trim() || "null";

    const time =
      document.getElementById("filterTime")?.value || "null";

    const specialty =
      document.getElementById("filterSpecialty")?.value || "null";

    const response = await filterDoctors(name, time, specialty);

    const doctors = response.doctors || [];

    renderDoctorCards(doctors);
  } catch (error) {
    console.error("Filter error:", error);

    document.getElementById("content").innerHTML =
      "<p>No doctors found</p>";
  }
}

/**
 * Add doctor
 */
window.adminAddDoctor = async function () {
  try {
    const token = localStorage.getItem("token");

    if (!token) {
      alert("Admin authentication required.");
      return;
    }

    const availabilityCheckboxes = document.querySelectorAll(
      'input[name="availability"]:checked'
    );

    const availability = Array.from(availabilityCheckboxes).map(
      (checkbox) => checkbox.value
    );

    const doctor = {
      name: document.getElementById("doctorName").value,
      specialty: document.getElementById("doctorSpecialty").value,
      email: document.getElementById("doctorEmail").value,
      password: document.getElementById("doctorPassword").value,
      mobileNo: document.getElementById("doctorMobile").value,
      availabilityTime:
        document.getElementById("doctorAvailabilityTime")?.value,
      availability,
    };

    const result = await saveDoctor(doctor, token);

    if (result.success) {
      alert(result.message || "Doctor added successfully.");

      const modal = document.getElementById("addDoctorModal");
      if (modal) {
        modal.style.display = "none";
      }

      await loadDoctorCards();
    } else {
      alert(result.message || "Failed to add doctor.");
    }
  } catch (error) {
    console.error("Error adding doctor:", error);
    alert("Something went wrong while adding the doctor.");
  }
};