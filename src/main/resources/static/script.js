const slotOrder = {
    "09:00 AM - 10:00 AM": 1,
    "10:00 AM - 11:00 AM": 2,
    "11:00 AM - 12:00 PM": 3,
    "02:00 PM - 03:00 PM": 4,
    "03:00 PM - 04:00 PM": 5,
    "04:00 PM - 05:00 PM": 6
};

let isPatientsVisible = false;
let isScheduleVisible = false;

async function addPatient() {
    let name = document.getElementById("name").value.trim();
    let age = document.getElementById("age").value.trim();
    let gender = document.getElementById("gender").value.trim();
    let timeSlot = document.getElementById("timeSlot").value;

    if (!name || !age || !gender || !timeSlot) {
        alert("Please fill in all fields and select a time slot.");
        return;
    }

    let containsDigits = /\d/;
    if (containsDigits.test(name)) {
        alert("Name must not be blank and cannot contain numbers.");
        return;
    }

    let ageVal = parseInt(age);
    if (isNaN(ageVal) || ageVal < 1 || ageVal > 150) {
        alert("Age must be between 1 and 150.");
        return;
    }

    let patient = {
        name: name,
        age: ageVal,
        gender: gender,
        timeSlot: timeSlot
    };

    let response = await fetch(
        "/patients",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(patient)
        }
    );

    let data = await response.text();
    alert(data);

    if (response.ok) {
        // Clear form inputs only on successful additions
        document.getElementById("name").value = "";
        document.getElementById("age").value = "";
        document.getElementById("gender").value = "";
        document.getElementById("timeSlot").selectedIndex = 0;

        // Refresh views only if they are currently showing data
        if (isPatientsVisible) {
            viewPatients();
        }
        if (isScheduleVisible) {
            viewScheduleBoard();
        }
    }
}

async function viewPatients() {
    isPatientsVisible = true;
    let response = await fetch("/patients");
    let patients = await response.json();
    let output = "";

    if (patients.length === 0) {
        output = "<p class='subtitle'>No patients registered yet.</p>";
    } else {
        for (let i = 0; i < patients.length; i++) {
            let p = patients[i];
            output += `
                <div class="patient-card">
                    <p class="patient-name">${p.name}</p>
                    <p class="patient-meta">Age: ${p.age} | Gender: ${p.gender}</p>
                    <span class="time-badge">${p.timeSlot || 'No slot selected'}</span>
                </div>
            `;
        }
    }

    document.getElementById("patientList").innerHTML = output;
}

async function viewScheduleBoard() {
    isScheduleVisible = true;
    let response = await fetch("/patients");
    let patients = await response.json();
    let output = "";

    if (patients.length === 0) {
        output = "<p class='subtitle'>No appointments scheduled yet.</p>";
    } else {
        // Sort patients by scheduled time slot chronologically
        patients.sort((a, b) => {
            let orderA = slotOrder[a.timeSlot] || 99;
            let orderB = slotOrder[b.timeSlot] || 99;
            return orderA - orderB;
        });

        for (let i = 0; i < patients.length; i++) {
            let p = patients[i];
            output += `
                <div class="schedule-item">
                    <span class="schedule-patient"><strong>${p.name}</strong> (${p.gender}, ${p.age} yrs)</span>
                    <span class="schedule-time">${p.timeSlot || 'TBD'}</span>
                </div>
            `;
        }
    }

    document.getElementById("scheduleBoard").innerHTML = output;
}

async function searchPatient() {
    let name = document.getElementById("searchName").value.trim();
    if (!name) {
        alert("Please enter a name to search.");
        return;
    }

    let response = await fetch("/patients/search/" + encodeURIComponent(name));
    let patient = null;
    
    // Check if response has valid JSON
    try {
        if (response.ok) {
            let text = await response.text();
            if (text) {
                patient = JSON.parse(text);
            }
        }
    } catch (e) {
        console.error("Error parsing search results", e);
    }

    let output = "";
    if (patient) {
        output = `
            <div class="patient-card" style="border-left: 4px solid var(--primary); margin-top: 12px;">
                <p class="patient-name">${patient.name}</p>
                <p class="patient-meta">Age: ${patient.age} | Gender: ${patient.gender}</p>
                <span class="time-badge">${patient.timeSlot || 'No slot selected'}</span>
            </div>
        `;
    } else {
        output = "<p class='subtitle' style='color: red; margin-top: 12px;'>No matching patient found.</p>";
    }

    document.getElementById("searchResult").innerHTML = output;
}

async function deletePatient() {
    let name = document.getElementById("deleteName").value.trim();
    if (!name) {
        alert("Please enter a name to delete.");
        return;
    }

    let response = await fetch(
        "/patients/delete/" + encodeURIComponent(name),
        {
            method: "DELETE"
        }
    );
    
    let data = await response.text();
    alert(data);

    document.getElementById("deleteName").value = "";

    // Refresh views only if they are currently showing data
    if (isPatientsVisible) {
        viewPatients();
    }
    if (isScheduleVisible) {
        viewScheduleBoard();
    }
}