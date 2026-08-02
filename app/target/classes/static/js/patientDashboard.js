import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import {
    getDoctors,
    filterDoctors
} from "./services/doctorServices.js";
import {
    patientLogin,
    patientSignup
} from "./services/patientServices.js";

function getElementValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : "";
}

export function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");

    if (!contentDiv) {
        return;
    }

    contentDiv.innerHTML = "";

    if (!Array.isArray(doctors) || doctors.length === 0) {
        contentDiv.innerHTML =
            "<p class='empty-message'>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach((doctor) => {
        contentDiv.appendChild(createDoctorCard(doctor));
    });
}

export async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");

    if (!contentDiv) {
        return;
    }

    contentDiv.innerHTML = "<p class='loading-message'>Loading doctors...</p>";

    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Load patient doctor cards error:", error);
        contentDiv.innerHTML =
            "<p class='error-message'>Unable to load doctors.</p>";
    }
}

export async function filterDoctorsOnChange() {
    const name = getElementValue("searchBar");
    const time = getElementValue("filterTime");
    const specialty = getElementValue("filterSpecialty");

    const contentDiv = document.getElementById("content");

    if (!contentDiv) {
        return;
    }

    contentDiv.innerHTML = "<p class='loading-message'>Filtering doctors...</p>";

    try {
        const doctors = await filterDoctors(name, time, specialty);
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Patient doctor filter error:", error);
        contentDiv.innerHTML =
            "<p class='error-message'>Unable to filter doctors.</p>";
    }
}

export async function signupPatient(event) {
    if (event) {
        event.preventDefault();
    }

    const patient = {
        name: getElementValue("patientName"),
        email: getElementValue("patientEmail"),
        password:
            document.getElementById("patientPassword")?.value || "",
        phone: getElementValue("patientPhone"),
        address: getElementValue("patientAddress")
    };

    if (
        !patient.name ||
        !patient.email ||
        !patient.password ||
        !patient.phone ||
        !patient.address
    ) {
        alert("Please complete all patient registration fields.");
        return;
    }

    try {
        const result = await patientSignup(patient);

        if (!result.success) {
            alert(result.message || "Patient signup failed.");
            return;
        }

        alert(result.message || "Patient registered successfully.");

        const modal = document.getElementById("modal");
        if (modal) {
            modal.style.display = "none";
        }

        const signupForm =
            document.getElementById("patientSignupForm");

        if (signupForm) {
            signupForm.reset();
        }

        openModal("patientLogin");
    } catch (error) {
        console.error("Patient signup error:", error);
        alert("Unable to register patient.");
    }
}

export async function loginPatient(event) {
    if (event) {
        event.preventDefault();
    }

    const credentials = {
        email: getElementValue("patientLoginEmail") ||
            getElementValue("patientEmail"),
        password:
            document.getElementById("patientLoginPassword")?.value ||
            document.getElementById("patientPassword")?.value ||
            ""
    };

    if (!credentials.email || !credentials.password) {
        alert("Please enter email and password.");
        return;
    }

    try {
        const response = await patientLogin(credentials);

        if (!response) {
            alert("Unable to connect to the server.");
            return;
        }

        if (!response.ok) {
            let errorMessage = "Invalid credentials!";

            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch {
                // Keep the default message.
            }

            alert(errorMessage);
            return;
        }

        const data = await response.json();

        const token =
            data.token ||
            data.accessToken ||
            data.jwt;

        if (!token) {
            throw new Error("Authentication token was not returned.");
        }

        localStorage.setItem("token", token);
        localStorage.setItem("userRole", "loggedPatient");

        window.location.href =
            "/pages/loggedPatientDashboard.html";
    } catch (error) {
        console.error("Patient login error:", error);
        alert(error.message || "Unable to complete patient login.");
    }
}

function bindPatientDashboardEvents() {
    const patientSignupBtn =
        document.getElementById("patientSignup") ||
        document.getElementById("patientSignupBtn");

    const patientLoginBtn =
        document.getElementById("patientLogin") ||
        document.getElementById("patientLoginBtn");

    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty =
        document.getElementById("filterSpecialty");

    const signupForm =
        document.getElementById("patientSignupForm");

    const loginForm =
        document.getElementById("patientLoginForm");

    if (patientSignupBtn) {
        patientSignupBtn.addEventListener("click", () => {
            openModal("patientSignup");
        });
    }

    if (patientLoginBtn) {
        patientLoginBtn.addEventListener("click", () => {
            openModal("patientLogin");
        });
    }

    if (searchBar) {
        searchBar.addEventListener("input", filterDoctorsOnChange);
    }

    if (filterTime) {
        filterTime.addEventListener("change", filterDoctorsOnChange);
    }

    if (filterSpecialty) {
        filterSpecialty.addEventListener(
            "change",
            filterDoctorsOnChange
        );
    }

    if (signupForm) {
        signupForm.addEventListener("submit", signupPatient);
    }

    if (loginForm) {
        loginForm.addEventListener("submit", loginPatient);
    }
}

window.signupPatient = signupPatient;
window.loginPatient = loginPatient;
window.loadDoctorCards = loadDoctorCards;

document.addEventListener("DOMContentLoaded", async () => {
    bindPatientDashboardEvents();
    await loadDoctorCards();
});
