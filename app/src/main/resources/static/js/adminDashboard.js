import { openModal } from "./components/modals.js";
import {
    getDoctors,
    filterDoctors,
    saveDoctor
} from "./services/doctorServices.js";
import { createDoctorCard } from "./components/doctorCard.js";

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
        contentDiv.innerHTML = "<p class='empty-message'>No doctors found.</p>";
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
        console.error("Load doctors error:", error);
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

        if (!doctors || doctors.length === 0) {
            contentDiv.innerHTML =
                "<p class='empty-message'>No doctors found.</p>";
            return;
        }

        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Filter doctors error:", error);
        contentDiv.innerHTML =
            "<p class='error-message'>Unable to filter doctors.</p>";
    }
}

function collectAvailableTimes() {
    const selectedTimes = Array.from(
        document.querySelectorAll(
            'input[name="availableTimes"]:checked'
        )
    ).map((checkbox) => checkbox.value);

    if (selectedTimes.length > 0) {
        return selectedTimes;
    }

    const availableTimesInput =
        document.getElementById("availableTimes");

    if (!availableTimesInput) {
        return [];
    }

    return availableTimesInput.value
        .split(",")
        .map((time) => time.trim())
        .filter(Boolean);
}

export async function adminAddDoctor(event) {
    if (event) {
        event.preventDefault();
    }

    const token = localStorage.getItem("token");

    if (!token) {
        alert("Admin session expired. Please log in again.");
        localStorage.removeItem("userRole");
        window.location.href = "/";
        return;
    }

    const doctor = {
        name: getElementValue("doctorName"),
        specialty: getElementValue("doctorSpecialty"),
        email: getElementValue("doctorEmail"),
        password: document.getElementById("doctorPassword")?.value || "",
        phone: getElementValue("doctorPhone"),
        availableTimes: collectAvailableTimes()
    };

    if (
        !doctor.name ||
        !doctor.specialty ||
        !doctor.email ||
        !doctor.password ||
        !doctor.phone
    ) {
        alert("Please complete all required doctor fields.");
        return;
    }

    try {
        const result = await saveDoctor(doctor, token);

        if (!result.success) {
            alert(result.message || "Unable to add doctor.");
            return;
        }

        alert(result.message || "Doctor added successfully.");

        const modal = document.getElementById("modal");
        if (modal) {
            modal.style.display = "none";
        }

        const form = document.getElementById("addDoctorForm");
        if (form) {
            form.reset();
        }

        await loadDoctorCards();
    } catch (error) {
        console.error("Add doctor error:", error);
        alert("An unexpected error occurred while adding the doctor.");
    }
}

function bindAdminDashboardEvents() {
    const addDocBtn = document.getElementById("addDocBtn");
    const fallbackAddDoctorBtn =
        document.getElementById("addDoctorBtn");
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty =
        document.getElementById("filterSpecialty");
    const addDoctorForm =
        document.getElementById("addDoctorForm");

    const openAddDoctorModal = () => {
        openModal("addDoctor");
    };

    if (addDocBtn) {
        addDocBtn.addEventListener("click", openAddDoctorModal);
    }

    if (fallbackAddDoctorBtn) {
        fallbackAddDoctorBtn.addEventListener(
            "click",
            openAddDoctorModal
        );
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

    if (addDoctorForm) {
        addDoctorForm.addEventListener("submit", adminAddDoctor);
    }
}

window.adminAddDoctor = adminAddDoctor;
window.loadDoctorCards = loadDoctorCards;

document.addEventListener("DOMContentLoaded", async () => {
    bindAdminDashboardEvents();
    await loadDoctorCards();
});
