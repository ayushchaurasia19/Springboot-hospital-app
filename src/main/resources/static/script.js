async function addPatient() {
    let name = document.getElementById("name").value;
    let age = document.getElementById("age").value;
    let gender = document.getElementById("gender").value;

    let patient = {
        name: name,
        age: age,
        gender: gender
    };

    let response = await fetch(
        "/patients",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json" // sends JSON data
            },
            body: JSON.stringify(patient)
        }
    );

    let data = await response.text();
    alert(data);
}

async function viewPatients() {
    let response = await fetch("/patients");
    let patients = await response.json();
    let output = "";

    for (let i = 0; i < patients.length; i++) {
        output +=
            "<p>" +
            patients[i].name +
            " | " +
            patients[i].age +
            " | " +
            patients[i].gender +
            "</p>";
    }

    document.getElementById("patientList").innerHTML = output;
}

async function deletePatient() {
    let name = document.getElementById("deleteName").value;

    let response = await fetch(
        "/patients/delete/" + name,
        {
            method: "DELETE"
        }
    );
    
    let data = await response.text();
    alert(data);
}