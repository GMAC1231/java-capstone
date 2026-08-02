function isHomePage() {
    const path = window.location.pathname;
    return path === "/" || path.endsWith("/index.html");
}

function getPatientDashboardUrl() {
    return "/pages/patientDashboard.html";
}

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    if (isHomePage()) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = "";
        return;
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    let headerContent = `
        <header class="header">
            <div class="header-brand">
                <a href="/" class="logo-link" aria-label="Clinic home">
                    <span class="logo-text">Clinic Management System</span>
                </a>
            </div>
            <nav class="header-nav" aria-label="Main navigation">
    `;

    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn" type="button">Add Doctor</button>
            <a id="logoutBtn" href="#">Logout</a>`;
    } else if (role === "doctor") {
        headerContent += `
            <a id="doctorHomeBtn" href="/doctor/dashboard">Home</a>
            <a id="logoutBtn" href="#">Logout</a>`;
    } else if (role === "patient") {
        headerContent += `
            <button id="patientLoginBtn" class="header-btn" type="button">Login</button>
            <button id="patientSignupBtn" class="header-btn" type="button">Sign Up</button>`;
    } else if (role === "loggedPatient") {
        headerContent += `
            <a id="patientHomeBtn" href="${getPatientDashboardUrl()}">Home</a>
            <a id="appointmentsBtn" href="/patient/appointments">Appointments</a>
            <a id="logoutPatientBtn" href="#">Logout</a>`;
    } else {
        headerContent += `<a href="/">Home</a>`;
    }

    headerContent += `</nav></header>`;
    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
    const addDocBtn = document.getElementById("addDocBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const logoutPatientBtn = document.getElementById("logoutPatientBtn");
    const patientLoginBtn = document.getElementById("patientLoginBtn");
    const patientSignupBtn = document.getElementById("patientSignupBtn");

    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => {
            if (typeof window.openModal === "function") {
                window.openModal("addDoctor");
            } else {
                const modal = document.getElementById("modal");
                if (modal) modal.style.display = "flex";
            }
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener("click", (event) => {
            event.preventDefault();
            logout();
        });
    }

    if (logoutPatientBtn) {
        logoutPatientBtn.addEventListener("click", (event) => {
            event.preventDefault();
            logoutPatient();
        });
    }

    if (patientLoginBtn) {
        patientLoginBtn.addEventListener("click", () => {
            if (typeof window.openModal === "function") window.openModal("patientLogin");
        });
    }

    if (patientSignupBtn) {
        patientSignupBtn.addEventListener("click", () => {
            if (typeof window.openModal === "function") window.openModal("patientSignup");
        });
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = getPatientDashboardUrl();
}

window.renderHeader = renderHeader;
window.attachHeaderButtonListeners = attachHeaderButtonListeners;
window.logout = logout;
window.logoutPatient = logoutPatient;

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", renderHeader);
} else {
    renderHeader();
}
