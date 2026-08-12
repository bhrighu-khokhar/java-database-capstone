const APPOINTMENT_API = "/api/appointments";
const PRESCRIPTION_API = "/api/prescriptions";

let allAppointments = [];
let filteredAppointments = [];


document.addEventListener("DOMContentLoaded", function () {

    checkLogin();

    loadAppointments();

    setupEventListeners();

});


/* =========================================
   LOGIN CHECK
   ========================================= */

function checkLogin() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/";
        return;
    }


    const username =
        localStorage.getItem("username");


    if (username) {

        document.getElementById("doctorName")
            .textContent = username;

    }

}


/* =========================================
   LOAD APPOINTMENTS
   ========================================= */

async function loadAppointments() {

    const loading =
        document.getElementById("loadingMessage");

    const error =
        document.getElementById("errorMessage");


    loading.classList.remove("hidden");

    error.classList.add("hidden");


    try {

        const token =
            localStorage.getItem("token");


        const response =
            await fetch(APPOINTMENT_API, {

                method: "GET",

                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                }

            });


        if (!response.ok) {

            throw new Error(
                "Unable to load appointments"
            );

        }


        allAppointments =
            await response.json();


        filteredAppointments =
            [...allAppointments];


        displayAppointments(
            filteredAppointments
        );


    } catch (err) {

        console.error(err);


        error.textContent =
            "Unable to load appointments. Please try again.";


        error.classList.remove("hidden");


    } finally {

        loading.classList.add("hidden");

    }

}


/* =========================================
   DISPLAY APPOINTMENTS
   ========================================= */

function displayAppointments(appointments) {

    const container =
        document.getElementById("appointmentList");


    const count =
        document.getElementById("appointmentCount");


    container.innerHTML = "";


    count.textContent =
        `${appointments.length} appointment${appointments.length !== 1 ? "s" : ""}`;


    if (appointments.length === 0) {

        container.innerHTML = `
            <div class="empty-message">

                <h3>No appointments found</h3>

                <p>
                    No appointments match your search or filter.
                </p>

            </div>
        `;

        return;

    }


    appointments.forEach(function (appointment) {

        const card =
            createAppointmentCard(appointment);


        container.appendChild(card);

    });

}


/* =========================================
   CREATE APPOINTMENT CARD
   ========================================= */

function createAppointmentCard(appointment) {

    const card =
        document.createElement("div");


    card.className =
        "appointment-card";


    /*
     * The exact property names can vary depending
     * on your Appointment model.
     */

    const patient =
        appointment.patient || {};


    const patientName =
        patient.name ||
        appointment.patientName ||
        "Unknown Patient";


    const appointmentTime =
        appointment.appointmentTime ||
        appointment.dateTime ||
        appointment.time ||
        "";


    const status =
        appointment.status !== undefined
            ? appointment.status
            : 0;


    const statusText =
        getStatusText(status);


    const patientId =
        patient.id ||
        appointment.patientId ||
        "";


    card.innerHTML = `

        <div class="appointment-card-header">

            <div class="patient-avatar">
                ${getInitials(patientName)}
            </div>

            <div>

                <h3>
                    ${escapeHtml(patientName)}
                </h3>

                <span class="appointment-status">
                    ${statusText}
                </span>

            </div>

        </div>


        <div class="appointment-details">

            <p>
                <strong>Date:</strong>
                ${formatDate(appointmentTime)}
            </p>

            <p>
                <strong>Time:</strong>
                ${formatTime(appointmentTime)}
            </p>

        </div>


        <div class="appointment-actions">

            <button
                type="button"
                class="primary-btn prescription-btn">

                View Prescriptions

            </button>

        </div>

    `;


    const prescriptionButton =
        card.querySelector(".prescription-btn");


    prescriptionButton.addEventListener(
        "click",
        function () {

            openPrescriptionModal(
                patientId,
                patientName
            );

        }
    );


    return card;

}


/* =========================================
   STATUS
   ========================================= */

function getStatusText(status) {

    if (status === 0 || status === "0") {
        return "Scheduled";
    }

    if (status === 1 || status === "1") {
        return "Completed";
    }

    if (status === 2 || status === "2") {
        return "Cancelled";
    }

    return String(status);

}


/* =========================================
   FILTER APPOINTMENTS
   ========================================= */

function applyFilters() {

    const search =
        document
            .getElementById("searchPatient")
            .value
            .trim()
            .toLowerCase();


    const selectedDate =
        document
            .getElementById("appointmentDate")
            .value;


    filteredAppointments =
        allAppointments.filter(function (appointment) {


            const patient =
                appointment.patient || {};


            const patientName =
                (
                    patient.name ||
                    appointment.patientName ||
                    ""
                ).toLowerCase();


            const matchesPatient =
                patientName.includes(search);


            let matchesDate = true;


            if (selectedDate) {

                const appointmentTime =
                    appointment.appointmentTime ||
                    appointment.dateTime ||
                    "";


                if (appointmentTime) {

                    matchesDate =
                        appointmentTime.startsWith(
                            selectedDate
                        );

                } else {

                    matchesDate = false;

                }

            }


            return (
                matchesPatient &&
                matchesDate
            );

        });


    displayAppointments(
        filteredAppointments
    );

}


/* =========================================
   PRESCRIPTION MODAL
   ========================================= */

async function openPrescriptionModal(
    patientId,
    patientName
) {

    const modal =
        document.getElementById(
            "prescriptionModal"
        );


    const nameElement =
        document.getElementById(
            "prescriptionPatientName"
        );


    const list =
        document.getElementById(
            "prescriptionList"
        );


    const loading =
        document.getElementById(
            "prescriptionLoading"
        );


    const error =
        document.getElementById(
            "prescriptionError"
        );


    nameElement.textContent =
        patientName;


    list.innerHTML = "";

    error.classList.add("hidden");

    loading.classList.remove("hidden");

    modal.classList.remove("hidden");


    try {

        const token =
            localStorage.getItem("token");


        /*
         * If your backend uses a different prescription
         * endpoint, only change this URL.
         */

        const response =
            await fetch(
                `${PRESCRIPTION_API}/patient/${patientId}`,
                {

                    method: "GET",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Authorization":
                            `Bearer ${token}`
                    }

                }
            );


        if (!response.ok) {

            throw new Error(
                "Unable to load prescriptions"
            );

        }


        const prescriptions =
            await response.json();


        displayPrescriptions(
            prescriptions
        );


    } catch (err) {

        console.error(err);


        error.textContent =
            "Unable to load prescription history.";


        error.classList.remove(
            "hidden"
        );


    } finally {

        loading.classList.add(
            "hidden"
        );

    }

}


/* =========================================
   DISPLAY PRESCRIPTIONS
   ========================================= */

function displayPrescriptions(
    prescriptions
) {

    const container =
        document.getElementById(
            "prescriptionList"
        );


    container.innerHTML = "";


    if (
        !Array.isArray(prescriptions) ||
        prescriptions.length === 0
    ) {

        container.innerHTML = `
            <div class="empty-message">

                <h3>No Previous Prescriptions</h3>

                <p>
                    This patient has no prescription records.
                </p>

            </div>
        `;

        return;

    }


    prescriptions.forEach(
        function (prescription) {

            const item =
                document.createElement("div");


            item.className =
                "prescription-item";


            item.innerHTML = `

                <h4>
                    ${escapeHtml(
                        prescription.medication ||
                        "Medication"
                    )}
                </h4>

                <p>
                    <strong>Patient:</strong>
                    ${escapeHtml(
                        prescription.patientName ||
                        "Not available"
                    )}
                </p>

                <p>
                    <strong>Appointment ID:</strong>
                    ${escapeHtml(
                        String(
                            prescription.appointmentId ||
                            "Not available"
                        )
                    )}
                </p>

                <p>
                    <strong>Doctor Notes:</strong>
                    ${escapeHtml(
                        prescription.doctorNotes ||
                        "No notes"
                    )}
                </p>

            `;


            container.appendChild(item);

        }
    );

}


/* =========================================
   CLOSE MODAL
   ========================================= */

function closePrescriptionModal() {

    document
        .getElementById("prescriptionModal")
        .classList.add("hidden");

}


/* =========================================
   EVENT LISTENERS
   ========================================= */

function setupEventListeners() {

    document
        .getElementById("searchPatient")
        .addEventListener(
            "input",
            applyFilters
        );


    document
        .getElementById("appointmentDate")
        .addEventListener(
            "change",
            applyFilters
        );


    document
        .getElementById("clearFiltersBtn")
        .addEventListener(
            "click",
            function () {

                document
                    .getElementById(
                        "searchPatient"
                    )
                    .value = "";


                document
                    .getElementById(
                        "appointmentDate"
                    )
                    .value = "";


                applyFilters();

            }
        );


    document
        .getElementById(
            "closePrescriptionModal"
        )
        .addEventListener(
            "click",
            closePrescriptionModal
        );


    document
        .getElementById(
            "closePrescriptionBtn"
        )
        .addEventListener(
            "click",
            closePrescriptionModal
        );


    document
        .getElementById("logoutBtn")
        .addEventListener(
            "click",
            logout
        );


    document
        .getElementById("prescriptionModal")
        .addEventListener(
            "click",
            function (event) {

                if (
                    event.target.id ===
                    "prescriptionModal"
                ) {

                    closePrescriptionModal();

                }

            }
        );

}


/* =========================================
   LOGOUT
   ========================================= */

function logout() {

    localStorage.removeItem("token");

    localStorage.removeItem("username");

    localStorage.removeItem("role");


    window.location.href = "/";

}


/* =========================================
   DATE FORMAT
   ========================================= */

function formatDate(dateTime) {

    if (!dateTime) {
        return "Not available";
    }


    const date =
        new Date(dateTime);


    if (isNaN(date.getTime())) {
        return dateTime;
    }


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );

}


/* =========================================
   TIME FORMAT
   ========================================= */

function formatTime(dateTime) {

    if (!dateTime) {
        return "Not available";
    }


    const date =
        new Date(dateTime);


    if (isNaN(date.getTime())) {
        return dateTime;
    }


    return date.toLocaleTimeString(
        "en-IN",
        {
            hour: "2-digit",
            minute: "2-digit"
        }
    );

}


/* =========================================
   INITIALS
   ========================================= */

function getInitials(name) {

    if (!name) {
        return "PT";
    }


    return name
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map(
            word =>
                word.charAt(0).toUpperCase()
        )
        .join("");

}


/* =========================================
   HTML ESCAPING
   ========================================= */

function escapeHtml(value) {

    const div =
        document.createElement("div");


    div.textContent =
        value;


    return div.innerHTML;

}
