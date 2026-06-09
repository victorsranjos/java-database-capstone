// appointmentRecordService.js
import { API_BASE_URL } from "../config/config.js";
const APPOINTMENT_API = `${API_BASE_URL}/appointment`;


import { getDoctors } from './doctorServices.js';

//This is for the doctor to get all the patient Appointments
export async function getAllAppointments(date, patientName, token) {
  // Decode JWT to get doctor's email
  const payload = JSON.parse(atob(token.split('.')[1]));
  const email = payload.sub;

  // Find the doctor ID
  const doctors = await getDoctors();
  const doctor = doctors.find(d => d.email === email);
  
  if (!doctor) {
    throw new Error("Doctor not found");
  }

  // Construct URL matching the backend: /{doctorId}/{date}/{token}?patientName=...
  let url = `${APPOINTMENT_API}/${doctor.id}/${date}/${token}`;
  if (patientName && patientName !== "null") {
    url += `?patientName=${encodeURIComponent(patientName)}`;
  }

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error("Failed to fetch appointments");
  }

  return await response.json();
}

export async function bookAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}

export async function updateAppointment(appointment, token) {
  try {
    const response = await fetch(`${APPOINTMENT_API}/${token}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(appointment)
    });

    const data = await response.json();
    return {
      success: response.ok,
      message: data.message || "Something went wrong"
    };
  } catch (error) {
    console.error("Error while booking appointment:", error);
    return {
      success: false,
      message: "Network error. Please try again later."
    };
  }
}
