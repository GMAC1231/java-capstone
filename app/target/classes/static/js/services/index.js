import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

const ADMIN_API = `${API_BASE_URL}/admin`;
const DOCTOR_API = `${API_BASE_URL}/doctor/login`;

window.addEventListener("load", () => {
    const adminBtn = document.getElementById("adminLogin");
    const doctorBtn = document.getElementById("doctorLogin");

    if (adminBtn) {
        adminBtn.addEventListener("click", () => {
            localStorage.setItem("userRole", "admin");
            openModal("adminLogin");
        });
    }

    if (doctorBtn) {
        doctorBtn.addEventListener("click", () => {
            localStorage.setItem("userRole", "doctor");
            openModal("doctorLogin");
        });
    }
});

async function adminLoginHandler(event) {
    if (event) {
        event.preventDefault();
    }

    const usernameInput = document.getElementById("adminUsername");
    const passwordInput = document.getElementById("adminPassword");

    const username = usernameInput?.value.trim();
    const password = passwordInput?.value;

    if (!username || !password) {
        alert("Please enter username and password.");
        return;
    }

    const admin = {
        username,
        password
    };

    try {
        const response = await fetch(ADMIN_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(admin)
        });

        if (!response.ok) {
            alert("Invalid credentials!");
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
        selectRole("admin");
    } catch (error) {
        console.error("Admin login error:", error);
        alert(error.message || "Unable to complete admin login.");
    }
}

async function doctorLoginHandler(event) {
    if (event) {
        event.preventDefault();
    }

    const emailInput = document.getElementById("doctorEmail");
    const passwordInput = document.getElementById("doctorPassword");

    const email = emailInput?.value.trim();
    const password = passwordInput?.value;

    if (!email || !password) {
        alert("Please enter email and password.");
        return;
    }

    const doctor = {
        email,
        password
    };

    try {
        const response = await fetch(DOCTOR_API, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(doctor)
        });

        if (!response.ok) {
            alert("Invalid credentials!");
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
        selectRole("doctor");
    } catch (error) {
        console.error("Doctor login error:", error);
        alert(error.message || "Unable to complete doctor login.");
    }
}

function selectRole(role) {
    localStorage.setItem("userRole", role);

    if (role === "admin") {
        window.location.href = "/admin/dashboard";
    } else if (role === "doctor") {
        window.location.href = "/doctor/dashboard";
    }
}

window.adminLoginHandler = adminLoginHandler;
window.doctorLoginHandler = doctorLoginHandler;
window.selectRole = selectRole;