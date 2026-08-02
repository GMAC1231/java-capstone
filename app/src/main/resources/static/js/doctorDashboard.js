import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

let appointmentTableBody = null;
let selectedDate = new Date().toISOString().split("T")[0];
let token = localStorage.getItem("token");
let patientName = null;

function showTableMessage(message, className = "noPatientRecord") {
    if (!appointmentTableBody) {
        return;
    }

    appointmentTableBody.innerHTML = `
        <tr>
            <td colspan="5" class="${className}">
                ${message}
            </td>
        </tr>
    `;
}

export async function loadAppointments() {
    if (!appointmentTableBody) {
        appointmentTableBody =
            document.getElementById("patientTableBody");
    }

    if (!appointmentTableBody) {
        return;
    }

    if (!token) {
        showTableMessage(
            "Your session has expired. Please log in again.",
            "error-message"
        );
        return;
    }

    showTableMessage("Loading appointments...", "loading-message");

    try {
        const appointments = await getAllAppointments(
            selectedDate,
            patientName,
            token
        );

        appointmentTableBody.innerHTML = "";

        if (!Array.isArray(appointments) || appointments.length === 0) {
            showTableMessage("No Appointments found for today");
            return;
        }

        appointments.forEach((appointment) => {
            const patient =
                appointment.patient ||
                appointment.patientDetails ||
                {};

            const row = createPatientRow(patient, appointment);

            if (row) {
                appointmentTableBody.appendChild(row);
            }
        });
    } catch (error) {
        console.error("Load appointments error:", error);
        showTableMessage(
            "Unable to load appointments. Please try again.",
            "error-message"
        );
    }
}

function bindDoctorDashboardEvents() {
    appointmentTableBody =
        document.getElementById("patientTableBody");

    const searchBar = document.getElementById("searchBar");
    const todayButton =
        document.getElementById("todayButton") ||
        document.getElementById("todayAppointmentsBtn");
    const datePicker =
        document.getElementById("datePicker") ||
        document.getElementById("appointmentDate");

    if (datePicker) {
        datePicker.value = selectedDate;
    }

    if (searchBar) {
        searchBar.addEventListener("input", async (event) => {
            const value = event.target.value.trim();
            patientName = value || null;
            await loadAppointments();
        });
    }

    if (todayButton) {
        todayButton.addEventListener("click", async () => {
            selectedDate =
                new Date().toISOString().split("T")[0];

            if (datePicker) {
                datePicker.value = selectedDate;
            }

            await loadAppointments();
        });
    }

    if (datePicker) {
        datePicker.addEventListener("change", async (event) => {
            selectedDate =
                event.target.value ||
                new Date().toISOString().split("T")[0];

            await loadAppointments();
        });
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    if (typeof window.renderContent === "function") {
        window.renderContent();
    }

    token = localStorage.getItem("token");
    bindDoctorDashboardEvents();
    await loadAppointments();
});

window.loadAppointments = loadAppointments;
