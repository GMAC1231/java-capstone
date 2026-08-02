import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";
import { showBookingOverlay } from "./modals.js";

function createTextElement(tagName, className, text) {
    const element = document.createElement(tagName);
    element.classList.add(className);
    element.textContent = text;
    return element;
}

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.classList.add("doctor-card");
    card.dataset.doctorId = doctor.id;

    const role = localStorage.getItem("userRole");
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = createTextElement("h3", "doctor-name", doctor.name || "Doctor name unavailable");
    const specialization = createTextElement("p", "doctor-specialty", `Specialty: ${doctor.specialty || "Not specified"}`);
    const email = createTextElement("p", "doctor-email", `Email: ${doctor.email || "Not available"}`);
    const phone = createTextElement("p", "doctor-phone", `Phone: ${doctor.phone || "Not available"}`);

    const times = Array.isArray(doctor.availableTimes)
        ? doctor.availableTimes.join(", ")
        : doctor.availableTimes || "No availability listed";

    const availability = createTextElement("p", "doctor-availability", `Available times: ${times}`);

    infoDiv.append(name, specialization, email, phone, availability);

    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.classList.add("delete-doctor-btn");
        removeBtn.textContent = "Delete";

        removeBtn.addEventListener("click", async () => {
            if (!window.confirm(`Are you sure you want to delete ${doctor.name}?`)) return;

            const token = localStorage.getItem("token");
            if (!token) {
                alert("Your session has expired. Please log in again.");
                localStorage.removeItem("userRole");
                window.location.href = "/";
                return;
            }

            removeBtn.disabled = true;
            removeBtn.textContent = "Deleting...";

            try {
                const deleted = await deleteDoctor(doctor.id, token);
                if (deleted === false) throw new Error("The doctor could not be deleted.");
                card.remove();
            } catch (error) {
                console.error("Delete doctor error:", error);
                alert(error.message || "Unable to delete doctor.");
                removeBtn.disabled = false;
                removeBtn.textContent = "Delete";
            }
        });

        actionsDiv.appendChild(removeBtn);
    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.type = "button";
        bookNow.classList.add("book-now-btn");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", () => alert("Patient needs to login first."));
        actionsDiv.appendChild(bookNow);
    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.type = "button";
        bookNow.classList.add("book-now-btn");
        bookNow.textContent = "Book Now";

        bookNow.addEventListener("click", async (event) => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Your session has expired. Please log in again.");
                localStorage.setItem("userRole", "patient");
                window.location.href = "/pages/patientDashboard.html";
                return;
            }

            bookNow.disabled = true;
            bookNow.textContent = "Loading...";

            try {
                const patientData = await getPatientData(token);
                showBookingOverlay(event, doctor, patientData);
            } catch (error) {
                console.error("Booking preparation error:", error);
                alert(error.message || "Unable to start appointment booking.");
            } finally {
                bookNow.disabled = false;
                bookNow.textContent = "Book Now";
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    card.appendChild(infoDiv);
    if (actionsDiv.childElementCount > 0) card.appendChild(actionsDiv);
    return card;
}
