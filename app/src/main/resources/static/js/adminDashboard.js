const API_URL = "/api/doctors";

let allDoctors = [];
let filteredDoctors = [];


document.addEventListener("DOMContentLoaded", () => {

    checkAdminLogin();

    loadDoctors();

    setupEventListeners();

});


/*
 * Check whether the admin is logged in.
 */
function checkAdminLogin() {

    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "/";
        return;
    }

    const adminName = localStorage.getItem("username");

    if (adminName) {
        document.getElementById("adminName").textContent = adminName;
    }
}


/*
 * Get all doctors from backend.
 */
async function loadDoctors() {

    const loadingMessage = document.getElementById("loadingMessage");
    const errorMessage = document.getElementById("errorMessage");

    loadingMessage.classList.remove("hidden");
    errorMessage.classList.add("hidden");

    try {

        const token = localStorage.getItem("token");

        const response = await fetch(API_URL, {
            method: "GET",

            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error("Unable to load doctors.");
        }

        allDoctors = await response.json();

        filteredDoctors = [...allDoctors];

        populateFilters();

        displayDoctors(filteredDoctors);

    } catch (error) {

        console.error(error);

        errorMessage.textContent =
            "Unable to load doctors. Please try again.";

        errorMessage.classList.remove("hidden");

    } finally {

        loadingMessage.classList.add("hidden");
    }
}


/*
 * Display doctor cards.
 */
function displayDoctors(doctors) {

    const doctorList = document.getElementById("doctorList");
    const doctorCount = document.getElementById("doctorCount");

    doctorList.innerHTML = "";

    doctorCount.textContent =
        `${doctors.length} doctor${doctors.length !== 1 ? "s" : ""}`;


    if (doctors.length === 0) {

        doctorList.innerHTML = `
            <div class="empty-message">
                <h3>No doctors found</h3>
                <p>Try changing your search or filters.</p>
            </div>
        `;

        return;
    }


    doctors.forEach(doctor => {

        const card = createDoctorCard(doctor);

        doctorList.appendChild(card);

    });
}


/*
 * Create a doctor card.
 */
function createDoctorCard(doctor) {

    const card = document.createElement("div");

    card.className = "doctor-card";


    const availableTimes =
        Array.isArray(doctor.availableTimes)
            ? doctor.availableTimes
            : [];


    card.innerHTML = `
        <div class="doctor-card-header">

            <div class="doctor-avatar">
                ${getInitials(doctor.name)}
            </div>

            <div>
                <h3>${escapeHtml(doctor.name || "Unknown Doctor")}</h3>

                <p class="specialty">
                    ${escapeHtml(doctor.specialty || "No specialty")}
                </p>
            </div>

        </div>


        <div class="doctor-details">

            <p>
                <strong>Email:</strong>
                ${escapeHtml(doctor.email || "Not available")}
            </p>

            <p>
                <strong>Phone:</strong>
                ${escapeHtml(doctor.phone || "Not available")}
            </p>

            <p>
                <strong>Available:</strong>
            </p>

            <div class="time-list">

                ${
                    availableTimes.length > 0
                        ? availableTimes
                            .map(time =>
                                `<span class="time-badge">
                                    ${escapeHtml(time)}
                                </span>`
                            )
                            .join("")
                        : `<span class="no-time">
                            No times available
                           </span>`
                }

            </div>

        </div>
    `;

    return card;
}


/*
 * Populate specialty and time filters.
 */
function populateFilters() {

    const specialtyFilter =
        document.getElementById("specialtyFilter");

    const timeFilter =
        document.getElementById("timeFilter");


    const specialties = new Set();

    const times = new Set();


    allDoctors.forEach(doctor => {

        if (doctor.specialty) {
            specialties.add(doctor.specialty);
        }


        if (Array.isArray(doctor.availableTimes)) {

            doctor.availableTimes.forEach(time => {
                times.add(time);
            });

        }

    });


    specialtyFilter.innerHTML =
        `<option value="">All Specialties</option>`;


    [...specialties]
        .sort()
        .forEach(specialty => {

            const option = document.createElement("option");

            option.value = specialty;
            option.textContent = specialty;

            specialtyFilter.appendChild(option);

        });


    timeFilter.innerHTML =
        `<option value="">All Times</option>`;


    [...times]
        .sort()
        .forEach(time => {

            const option = document.createElement("option");

            option.value = time;
            option.textContent = time;

            timeFilter.appendChild(option);

        });
}


/*
 * Search and filter doctors.
 */
function applyFilters() {

    const searchValue =
        document
            .getElementById("searchDoctor")
            .value
            .trim()
            .toLowerCase();


    const specialtyValue =
        document.getElementById("specialtyFilter").value;


    const timeValue =
        document.getElementById("timeFilter").value;


    filteredDoctors = allDoctors.filter(doctor => {

        const name =
            (doctor.name || "").toLowerCase();


        const matchesName =
            name.includes(searchValue);


        const matchesSpecialty =
            !specialtyValue ||
            doctor.specialty === specialtyValue;


        const availableTimes =
            Array.isArray(doctor.availableTimes)
                ? doctor.availableTimes
                : [];


        const matchesTime =
            !timeValue ||
            availableTimes.includes(timeValue);


        return (
            matchesName &&
            matchesSpecialty &&
            matchesTime
        );

    });


    displayDoctors(filteredDoctors);
}


/*
 * Add doctor.
 */
async function addDoctor(event) {

    event.preventDefault();


    const formError =
        document.getElementById("formError");


    formError.classList.add("hidden");


    const name =
        document.getElementById("doctorName").value.trim();


    const specialty =
        document.getElementById("doctorSpecialty").value.trim();


    const email =
        document.getElementById("doctorEmail").value.trim();


    const password =
        document.getElementById("doctorPassword").value;


    const phone =
        document.getElementById("doctorPhone").value.trim();


    const availableTimes =
        [...document.querySelectorAll(
            'input[name="availableTimes"]:checked'
        )].map(input => input.value);


    const doctor = {
        name: name,
        specialty: specialty,
        email: email,
        password: password,
        phone: phone,
        availableTimes: availableTimes
    };


    try {

        const token =
            localStorage.getItem("token");


        const response = await fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },

            body: JSON.stringify(doctor)

        });


        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                errorText || "Unable to add doctor."
            );
        }


        closeDoctorModal();

        document
            .getElementById("doctorForm")
            .reset();


        await loadDoctors();


    } catch (error) {

        console.error(error);

        formError.textContent =
            "Unable to add doctor. Please check the entered information.";

        formError.classList.remove("hidden");
    }
}


/*
 * Open modal.
 */
function openDoctorModal() {

    document
        .getElementById("doctorModal")
        .classList.remove("hidden");
}


/*
 * Close modal.
 */
function closeDoctorModal() {

    document
        .getElementById("doctorModal")
        .classList.add("hidden");

}


/*
 * Event listeners.
 */
function setupEventListeners() {

    document
        .getElementById("searchDoctor")
        .addEventListener("input", applyFilters);


    document
        .getElementById("specialtyFilter")
        .addEventListener("change", applyFilters);


    document
        .getElementById("timeFilter")
        .addEventListener("change", applyFilters);


    document
        .getElementById("clearFiltersBtn")
        .addEventListener("click", () => {

            document.getElementById("searchDoctor").value = "";

            document.getElementById("specialtyFilter").value = "";

            document.getElementById("timeFilter").value = "";

            applyFilters();

        });


    document
        .getElementById("openAddDoctorBtn")
        .addEventListener("click", openDoctorModal);


    document
        .getElementById("closeDoctorModal")
        .addEventListener("click", closeDoctorModal);


    document
        .getElementById("cancelDoctorBtn")
        .addEventListener("click", closeDoctorModal);


    document
        .getElementById("doctorForm")
        .addEventListener("submit", addDoctor);


    document
        .getElementById("logoutBtn")
        .addEventListener("click", logout);


    document
        .getElementById("doctorModal")
        .addEventListener("click", event => {

            if (event.target.id === "doctorModal") {
                closeDoctorModal();
            }

        });
}


/*
 * Logout.
 */
function logout() {

    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");

    window.location.href = "/";
}


/*
 * Get initials for avatar.
 */
function getInitials(name) {

    if (!name) {
        return "DR";
    }

    return name
        .split(" ")
        .filter(part => part.length > 0)
        .slice(0, 2)
        .map(part => part[0].toUpperCase())
        .join("");
}


/*
 * Prevent HTML injection when displaying API data.
 */
function escapeHtml(value) {

    const div = document.createElement("div");

    div.textContent = value;

    return div.innerHTML;
}
