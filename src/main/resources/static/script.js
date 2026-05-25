// Global Authentication State
let token = localStorage.getItem("hospital_token") || "";
let username = localStorage.getItem("hospital_username") || "";
let role = localStorage.getItem("hospital_role") || "";

// Cache local lists to help populate forms
let patientsCache = [];
let doctorsCache = [];

// Slot index order for sorting
const slotOrder = {
    "09:00 AM - 10:00 AM": 1,
    "10:00 AM - 11:00 AM": 2,
    "11:00 AM - 12:00 PM": 3,
    "02:00 PM - 03:00 PM": 4,
    "03:00 PM - 04:00 PM": 5,
    "04:00 PM - 05:00 PM": 6
};

// On Window Load
window.addEventListener("DOMContentLoaded", () => {
    checkAuthAndInit();
});

// Helper: Wrapper for fetch with JWT headers
async function authFetch(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    if (token) {
        options.headers["Authorization"] = `Bearer ${token}`;
    }
    
    // Default Content-Type to JSON if sending a body and it is not already set
    if (options.body && !options.headers["Content-Type"]) {
        options.headers["Content-Type"] = "application/json";
    }

    try {
        const response = await fetch(url, options);
        if (response.status === 401 || response.status === 403) {
            // Unauthorized or Forbidden: clear auth and redirect to login
            handleLogout();
            throw new Error("Session expired. Please sign in again.");
        }
        return response;
    } catch (e) {
        console.error("Fetch API error:", e);
        throw e;
    }
}

// ------------------ AUTHENTICATION LOGIC ------------------

function checkAuthAndInit() {
    if (token && username && role) {
        document.getElementById("authSection").style.display = "none";
        document.getElementById("dashboardSection").style.display = "block";
        document.getElementById("displayUsername").innerText = username;
        document.getElementById("displayRole").innerText = role;

        // Apply admin panel locks if receptionist
        const docPanel = document.getElementById("doctorPanel");
        if (role === "ADMIN") {
            docPanel.classList.remove("locked");
        } else {
            docPanel.classList.add("locked");
        }

        // Initialize lists
        fetchFullPatientsList();
        fetchDoctorsList();
        fetchSchedulesBoard();
    } else {
        document.getElementById("authSection").style.display = "block";
        document.getElementById("dashboardSection").style.display = "none";
    }
}

function switchAuthTab(tab) {
    const tabLogin = document.getElementById("tabLogin");
    const tabRegister = document.getElementById("tabRegister");
    const loginSection = document.getElementById("loginFormSection");
    const registerSection = document.getElementById("registerFormSection");

    if (tab === "login") {
        tabLogin.classList.add("active");
        tabRegister.classList.remove("active");
        loginSection.style.display = "block";
        registerSection.style.display = "none";
    } else {
        tabLogin.classList.remove("active");
        tabRegister.classList.add("active");
        loginSection.style.display = "none";
        registerSection.style.display = "block";
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const userVal = document.getElementById("loginUsername").value.trim();
    const passVal = document.getElementById("loginPassword").value;

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: userVal, password: passVal })
        });

        if (response.ok) {
            const data = await response.json();
            token = data.token;
            username = data.username;
            role = data.role;

            localStorage.setItem("hospital_token", token);
            localStorage.setItem("hospital_username", username);
            localStorage.setItem("hospital_role", role);

            // Reset inputs
            document.getElementById("loginUsername").value = "";
            document.getElementById("loginPassword").value = "";

            checkAuthAndInit();
        } else {
            const errText = await response.text();
            alert("Login Failed: " + errText);
        }
    } catch (e) {
        alert("Server communication error occurred during login.");
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const userVal = document.getElementById("regUsername").value.trim();
    const passVal = document.getElementById("regPassword").value;
    const roleVal = document.getElementById("regRole").value;

    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: userVal, password: passVal, role: roleVal })
        });

        if (response.ok) {
            const data = await response.json();
            token = data.token;
            username = data.username;
            role = data.role;

            localStorage.setItem("hospital_token", token);
            localStorage.setItem("hospital_username", username);
            localStorage.setItem("hospital_role", role);

            // Reset inputs
            document.getElementById("regUsername").value = "";
            document.getElementById("regPassword").value = "";
            document.getElementById("regRole").selectedIndex = 0;

            alert("Registration successful! Welcome to Hospital App.");
            checkAuthAndInit();
        } else {
            const errText = await response.text();
            alert("Registration Failed: " + errText);
        }
    } catch (e) {
        alert("Server communication error occurred during registration.");
    }
}

function handleLogout() {
    token = "";
    username = "";
    role = "";
    localStorage.removeItem("hospital_token");
    localStorage.removeItem("hospital_username");
    localStorage.removeItem("hospital_role");
    checkAuthAndInit();
}

// ------------------ DOCTOR SLOTS CHECKBOX BADGES ------------------

function toggleSlotBadge(slotId) {
    const lbl = document.getElementById("lbl_" + slotId);
    const cb = lbl.querySelector("input");
    if (cb.checked) {
        lbl.classList.add("selected");
    } else {
        lbl.classList.remove("selected");
    }
}

// ------------------ PATIENT REGISTRATION ------------------

async function registerPatient(event) {
    event.preventDefault();
    const name = document.getElementById("patName").value.trim();
    const age = parseInt(document.getElementById("patAge").value);
    const gender = document.getElementById("patGender").value;
    const timeSlot = document.getElementById("patTimeSlot").value;

    const patient = { name, age, gender, timeSlot };

    try {
        const response = await authFetch("/patients", {
            method: "POST",
            body: JSON.stringify(patient)
        });

        const statusText = await response.text();
        alert(statusText);

        if (response.ok) {
            // Reset fields
            document.getElementById("patName").value = "";
            document.getElementById("patAge").value = "";
            document.getElementById("patGender").selectedIndex = 0;
            document.getElementById("patTimeSlot").selectedIndex = 0;

            // Refresh list
            fetchFullPatientsList();
        }
    } catch (e) {
        console.error("Error registering patient:", e);
    }
}

async function fetchFullPatientsList() {
    try {
        const response = await authFetch("/patients");
        if (response.ok) {
            const patients = await response.json();
            patientsCache = patients;
            displayPatientsList(patients);
            populatePatientSchedulingDropdown(patients);
        }
    } catch (e) {
        console.error("Error fetching patient registry:", e);
    }
}

function displayPatientsList(patients) {
    const container = document.getElementById("directoryListContainer");
    if (patients.length === 0) {
        container.innerHTML = `<div class="empty-state">No patients currently registered in directory.</div>`;
        return;
    }

    let html = "";
    patients.forEach(p => {
        html += `
            <div class="custom-card">
                <div class="card-details">
                    <h4>${p.name}</h4>
                    <p>Age: ${p.age} | Gender: ${p.gender}</p>
                    <span class="badge" style="margin-top: 8px;">Preferred: ${p.timeSlot || "TBD"}</span>
                </div>
                <div>
                    <button class="btn-danger" onclick="deletePatientByName('${p.name}')">Discharge</button>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

function populatePatientSchedulingDropdown(patients) {
    const dropdown = document.getElementById("apptPatient");
    let html = `<option value="" disabled selected>Select a patient</option>`;
    patients.forEach(p => {
        html += `<option value="${p.id}">${p.name} (Age: ${p.age})</option>`;
    });
    dropdown.innerHTML = html;
}

// ------------------ PATIENT SEARCH & DISCHARGE ------------------

async function triggerPatientSearch() {
    const query = document.getElementById("directorySearchInput").value.trim();
    if (!query) {
        fetchFullPatientsList();
        return;
    }

    try {
        const response = await authFetch("/patients/search/" + encodeURIComponent(query));
        if (response.ok) {
            const text = await response.text();
            if (text) {
                const patient = JSON.parse(text);
                displayPatientsList([patient]);
            } else {
                document.getElementById("directoryListContainer").innerHTML = 
                    `<div class="empty-state" style="color: var(--accent-danger);">No matching patient found in registry.</div>`;
            }
        }
    } catch (e) {
        console.error("Search error:", e);
    }
}

async function deletePatientByName(name) {
    if (!confirm(`Are you sure you want to discharge and delete patient: ${name}?`)) {
        return;
    }

    try {
        const response = await authFetch("/patients/delete/" + encodeURIComponent(name), {
            method: "DELETE"
        });

        const statusText = await response.text();
        alert(statusText);

        if (response.ok) {
            fetchFullPatientsList();
            fetchSchedulesBoard(); // Refresh board in case they had scheduled appointments
        }
    } catch (e) {
        console.error("Discharge error:", e);
    }
}

// ------------------ DOCTOR ADMINISTRATION ------------------

async function registerDoctor(event) {
    event.preventDefault();
    if (role !== "ADMIN") {
        alert("Restricted: Receptionists cannot register doctors.");
        return;
    }

    const name = document.getElementById("docName").value.trim();
    const specialization = document.getElementById("docSpec").value.trim();
    
    // Get checked hours
    const checkboxes = document.querySelectorAll(".slots-selector input[type='checkbox']:checked");
    const activeSlots = Array.from(checkboxes).map(cb => cb.value).join(", ");

    if (!activeSlots) {
        alert("Please select at least one available hour for the doctor.");
        return;
    }

    const doctor = { name, specialization, availableSlots: activeSlots };

    try {
        const response = await authFetch("/api/doctors", {
            method: "POST",
            body: JSON.stringify(doctor)
        });

        const statusText = await response.text();
        alert(statusText);

        if (response.ok) {
            // Reset inputs
            document.getElementById("docName").value = "";
            document.getElementById("docSpec").value = "";
            
            document.querySelectorAll(".slots-selector input[type='checkbox']").forEach(cb => {
                cb.checked = false;
            });
            document.querySelectorAll(".slot-checkbox-label").forEach(lbl => {
                lbl.classList.remove("selected");
            });

            // Refresh directories
            fetchDoctorsList();
        }
    } catch (e) {
        console.error("Doctor registration failure:", e);
    }
}

async function fetchDoctorsList() {
    try {
        const response = await authFetch("/api/doctors");
        if (response.ok) {
            const doctors = await response.json();
            doctorsCache = doctors;
            displayDoctorsRoster(doctors);
            populateDoctorSchedulingDropdown(doctors);
        }
    } catch (e) {
        console.error("Error fetching doctors:", e);
    }
}

function displayDoctorsRoster(doctors) {
    const container = document.getElementById("doctorsRosterContainer");
    if (doctors.length === 0) {
        container.innerHTML = `<div class="empty-state">No medical doctors registered.</div>`;
        return;
    }

    let html = "";
    doctors.forEach(d => {
        const slotsBadges = d.availableSlots
            ? d.availableSlots.split(",").map(slot => `<span class="badge" style="margin-right: 4px; margin-top: 6px;">${slot.trim()}</span>`).join("")
            : `<span class="badge badge-danger">No slots available</span>`;

        html += `
            <div class="custom-card" style="align-items: flex-start; flex-direction: column; gap: 8px;">
                <div style="width: 100%; display: flex; justify-content: space-between; align-items: center;">
                    <h4 style="color: var(--accent-primary);">${d.name}</h4>
                    <span class="badge badge-success" style="font-weight: 700;">${d.specialization}</span>
                </div>
                <div style="display: flex; flex-wrap: wrap;">
                    ${slotsBadges}
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

function populateDoctorSchedulingDropdown(doctors) {
    const dropdown = document.getElementById("apptDoctor");
    let html = `<option value="" disabled selected>Select a doctor</option>`;
    doctors.forEach(d => {
        html += `<option value="${d.id}">${d.name} (${d.specialization})</option>`;
    });
    dropdown.innerHTML = html;
}

// ------------------ APPOINTMENT SCHEDULING ------------------

function updateAppointmentTimeSlots() {
    const docId = parseInt(document.getElementById("apptDoctor").value);
    const slotDropdown = document.getElementById("apptTimeSlot");

    const doctor = doctorsCache.find(d => d.id === docId);
    if (!doctor || !doctor.availableSlots) {
        slotDropdown.innerHTML = `<option value="" disabled selected>No slots available</option>`;
        return;
    }

    const slots = doctor.availableSlots.split(",");
    let html = `<option value="" disabled selected>Choose a time slot</option>`;
    slots.forEach(slot => {
        html += `<option value="${slot.trim()}">${slot.trim()}</option>`;
    });
    slotDropdown.innerHTML = html;
}

async function scheduleAppointment(event) {
    event.preventDefault();
    const patientId = parseInt(document.getElementById("apptPatient").value);
    const doctorId = parseInt(document.getElementById("apptDoctor").value);
    const timeSlot = document.getElementById("apptTimeSlot").value;

    const request = { patientId, doctorId, timeSlot };

    try {
        const response = await authFetch("/api/appointments", {
            method: "POST",
            body: JSON.stringify(request)
        });

        if (response.ok) {
            alert("Visit Scheduled successfully!");
            // Reset scheduling dropdowns
            document.getElementById("apptPatient").selectedIndex = 0;
            document.getElementById("apptDoctor").selectedIndex = 0;
            document.getElementById("apptTimeSlot").innerHTML = `<option value="" disabled selected>Select a doctor first</option>`;

            // Refresh schedules
            fetchSchedulesBoard();
        } else {
            const errText = await response.text();
            alert("Booking Rejected: " + errText);
        }
    } catch (e) {
        console.error("Booking error:", e);
    }
}

async function fetchSchedulesBoard() {
    try {
        const response = await authFetch("/api/appointments");
        if (response.ok) {
            const appts = await response.json();
            displaySchedulesBoard(appts);
        }
    } catch (e) {
        console.error("Schedules fetch error:", e);
    }
}

function displaySchedulesBoard(appointments) {
    const container = document.getElementById("scheduleListContainer");
    if (appointments.length === 0) {
        container.innerHTML = `<div class="empty-state">No active schedules booked.</div>`;
        return;
    }

    // Sort appointments chronologically by scheduled time slot
    appointments.sort((a, b) => {
        const orderA = slotOrder[a.timeSlot] || 99;
        const orderB = slotOrder[b.timeSlot] || 99;
        return orderA - orderB;
    });

    let html = "";
    appointments.forEach(a => {
        const isCancelled = a.status === "CANCELLED";
        const badgeClass = isCancelled ? "badge-danger" : "badge-success";
        const cancelBtn = isCancelled
            ? ""
            : `<button class="btn-danger" style="padding: 4px 10px; font-size: 0.75rem;" onclick="cancelAppointment(${a.id})">Cancel</button>`;

        html += `
            <div class="custom-card" style="gap: 12px; opacity: ${isCancelled ? '0.5' : '1'};">
                <div class="card-details" style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <h4 style="margin: 0;">${a.patient.name}</h4>
                        <span class="badge ${badgeClass}">${a.status}</span>
                    </div>
                    <p style="margin-top: 4px; font-size: 0.825rem;">Physician: <strong style="color: #a5b4fc;">${a.doctor.name}</strong></p>
                    <p style="font-size: 0.8rem; margin-top: 2px;">Time: <strong>${a.timeSlot}</strong></p>
                </div>
                <div>
                    ${cancelBtn}
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

async function cancelAppointment(id) {
    if (!confirm("Are you sure you want to cancel this appointment?")) {
        return;
    }

    try {
        const response = await authFetch(`/api/appointments/${id}/cancel`, {
            method: "PATCH"
        });

        if (response.ok) {
            alert("Appointment cancelled.");
            fetchSchedulesBoard();
        } else {
            const errText = await response.text();
            alert("Cancellation failed: " + errText);
        }
    } catch (e) {
        console.error("Cancel error:", e);
    }
}