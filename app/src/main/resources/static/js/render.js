// render.js

// FIX #21: Define setRole and getRole using localStorage for role persistence
function setRole(role) {
  localStorage.setItem("userRole", role);
}

function getRole() {
  return localStorage.getItem("userRole");
}

// FIX #6: Rewrote selectRole — the original had dead code for "loggedPatient"
// nested inside the "doctor" branch, making it unreachable. Also the patient
// redirect used "if" instead of "else if", causing the doctor branch to also
// run when role === "patient".
function selectRole(role) {
  setRole(role);
  const token = localStorage.getItem("token");

  if (role === "admin") {
    if (token) {
      window.location.href = `/adminDashboard/${token}`;
    }
  } else if (role === "doctor") {
    if (token) {
      window.location.href = `/doctorDashboard/${token}`;
    }
  } else if (role === "loggedPatient") {
    window.location.href = "/pages/loggedPatientDashboard.html";
  } else if (role === "patient") {
    window.location.href = "/pages/patientDashboard.html";
  }
}

// Expose globally so non-module scripts (header.js) can call it
window.selectRole = selectRole;

function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/"; // if no role, send to role selection page
    return;
  }
}
