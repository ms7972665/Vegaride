// ==========================================================================
// VEGARIDE - CLIENT APPLICATION CONTROLLER (PURE BASIC JAVASCRIPT)
// ==========================================================================

// Global Session Variables
let currentUser = null;
let activeLocation = "Karnataka, Bangalore"; // Default starting location
let activeTab = "rent";
let selectedEmergencyIssue = "";
let selectedEmergencyCost = 0;

// Splash and Init Sequence
document.addEventListener("DOMContentLoaded", () => {
    // 1. Splash Screen Timeout
    setTimeout(() => {
        const splash = document.getElementById("splash-screen");
        const appContainer = document.getElementById("app-container");
        
        // Graceful CSS Fade and Zoom Transition
        splash.style.opacity = "0";
        splash.style.visibility = "hidden";
        
        appContainer.classList.remove("app-hidden");
        appContainer.classList.add("app-visible");
        
        // Start Auto-sliding background carousel
        startCarouselLoop();
    }, 2200);

    // 2. Setup Form Submissions
    setupAuthForms();

    // 3. Location Auto-detector
    document.getElementById("btn-detect-location").addEventListener("click", autoDetectLocation);
});

// ==========================================================================
// BACKGROUND CAROUSEL SLIDESHOW
// ==========================================================================
let currentSlideIndex = 0;
let carouselTimer = null;

function startCarouselLoop() {
    const slides = document.querySelectorAll(".carousel-slide");
    if (slides.length === 0) return;

    carouselTimer = setInterval(() => {
        // Remove active class from current
        slides[currentSlideIndex].classList.remove("active");
        
        // Increment index
        currentSlideIndex = (currentSlideIndex + 1) % slides.length;
        
        // Add active class to next
        slides[currentSlideIndex].classList.add("active");
    }, 4500);
}

// ==========================================================================
// AUTHENTICATION INTERACTION
// ==========================================================================
function toggleAuthCard(type) {
    const loginCard = document.getElementById("login-card");
    const registerCard = document.getElementById("register-card");

    // Reset alert notifications
    document.getElementById("login-error").style.display = "none";
    document.getElementById("login-success").style.display = "none";
    document.getElementById("register-error").style.display = "none";
    document.getElementById("register-success").style.display = "none";

    if (type === "login") {
        registerCard.classList.remove("active");
        loginCard.classList.add("active");
    } else {
        loginCard.classList.remove("active");
        registerCard.classList.add("active");
    }
}

function setupAuthForms() {
    // Login form submission
    const loginForm = document.getElementById("login-form");
    loginForm.addEventListener("submit", (e) => {
        e.preventDefault();
        
        const email = document.getElementById("login-email").value.trim();
        const password = document.getElementById("login-password").value;
        const errBox = document.getElementById("login-error");
        const succBox = document.getElementById("login-success");

        errBox.style.display = "none";
        succBox.style.display = "none";

        fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: email, password: password })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                succBox.innerText = data.message;
                succBox.style.display = "block";
                
                // Save user profile state
                currentUser = data.user;
                
                setTimeout(() => {
                    transitionToDashboard();
                }, 1000);
            } else {
                errBox.innerText = data.message;
                errBox.style.display = "block";
            }
        })
        .catch(err => {
            errBox.innerText = "Connection lost. Please make sure the Java server is running.";
            errBox.style.display = "block";
        });
    });

    // Registration form submission
    const registerForm = document.getElementById("register-form");
    registerForm.addEventListener("submit", (e) => {
        e.preventDefault();

        const name = document.getElementById("reg-name").value.trim();
        const phone = document.getElementById("reg-phone").value.trim();
        const email = document.getElementById("reg-email").value.trim();
        const password = document.getElementById("reg-password").value;
        const errBox = document.getElementById("register-error");
        const succBox = document.getElementById("register-success");

        errBox.style.display = "none";
        succBox.style.display = "none";

        // Client side validation
        if (phone.length < 10) {
            errBox.innerText = "Please enter a valid 10-digit mobile number.";
            errBox.style.display = "block";
            return;
        }

        fetch("/api/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name, phone: phone, email: email, password: password })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                succBox.innerText = data.message + " You can now log in.";
                succBox.style.display = "block";
                registerForm.reset();
                setTimeout(() => {
                    toggleAuthCard("login");
                }, 1800);
            } else {
                errBox.innerText = data.message;
                errBox.style.display = "block";
            }
        })
        .catch(err => {
            errBox.innerText = "Connection lost. Please verify the Java server is running.";
            errBox.style.display = "block";
        });
    });
}

function transitionToDashboard() {
    // Hide Auth container, display dashboard
    document.getElementById("auth-page").classList.add("hidden");
    document.getElementById("landing-page").classList.remove("hidden");
    
    // Set user profile visuals
    document.getElementById("profile-name").innerText = currentUser.name;
    document.getElementById("profile-phone").innerText = currentUser.phone;
    
    // Custom avatar letters (Initials)
    const initials = currentUser.name.split(" ").map(n => n[0]).join("").toUpperCase().substring(0, 2);
    document.getElementById("avatar-letters").innerText = initials;
    
    document.getElementById("user-display-name").innerText = currentUser.name;
    document.getElementById("user-display-email").innerText = currentUser.email;

    // Vehicle display
    updateVehicleDisplay();

    // Default location setup
    document.getElementById("selected-location-text").innerText = activeLocation;

    // Load initial vendor list
    loadVendors();
    loadBookingHistory();
}

function signOut() {
    currentUser = null;
    document.getElementById("login-form").reset();
    document.getElementById("register-form").reset();
    
    document.getElementById("landing-page").classList.add("hidden");
    document.getElementById("auth-page").classList.remove("hidden");
    toggleAuthCard("login");
}

// ==========================================================================
// USER VEHICLE SETTINGS
// ==========================================================================
function updateVehicleDisplay() {
    const textEl = document.getElementById("current-vehicle-text");
    if (currentUser.vehicle && currentUser.vehicle !== "None") {
        textEl.innerText = currentUser.vehicle;
    } else {
        textEl.innerText = "No vehicle registered";
    }
}

function showVehicleForm() {
    document.getElementById("vehicle-status-box").classList.add("hidden");
    document.getElementById("vehicle-form-box").classList.remove("hidden");
    document.getElementById("input-vehicle-model").value = currentUser.vehicle === "None" ? "" : currentUser.vehicle;
}

function hideVehicleForm() {
    document.getElementById("vehicle-form-box").classList.add("hidden");
    document.getElementById("vehicle-status-box").classList.remove("hidden");
}

function saveVehicleProfile() {
    const model = document.getElementById("input-vehicle-model").value.trim();
    if (!model) return;

    fetch("/api/update-profile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: currentUser.email, vehicle: model })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            currentUser.vehicle = model;
            updateVehicleDisplay();
            hideVehicleForm();
        }
    });
}

// ==========================================================================
// PINPOINT ACCURATE GEOLOCATION SYSTEM (NO API KEYS REQUIRED)
// ==========================================================================
function autoDetectLocation() {
    const locText = document.getElementById("selected-location-text");
    locText.innerText = "Accessing GPS...";

    // 1. Check if browser supports HTML5 Geolocation API
    if (!navigator.geolocation) {
        alert("Your browser does not support satelite location. Please enter your neighborhood manually!");
        locText.innerText = activeLocation;
        return;
    }

    // 2. Query GPS satellites
    navigator.geolocation.getCurrentPosition(
        (position) => {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            
            locText.innerText = "Resolving address...";

            // 3. Free Reverse Geocoding using OpenStreetMap Nominatim Engine (NO KEYS NEEDED)
            fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&accept-language=en`)
            .then(res => res.json())
            .then(data => {
                if (data && data.address) {
                    const addr = data.address;
                    let addressParts = [];

                    // Extract highly specific local neighborhood parameters
                    const neighborhood = addr.suburb || addr.neighbourhood || addr.quarter || addr.village || addr.road;
                    if (neighborhood) addressParts.push(neighborhood);

                    // Extract city or town
                    const city = addr.city || addr.town || addr.city_district || addr.county;
                    if (city) addressParts.push(city);

                    // Extract state
                    const state = addr.state;
                    if (state) addressParts.push(state);

                    if (addressParts.length > 0) {
                        activeLocation = addressParts.join(", ");
                    } else {
                        activeLocation = data.display_name.split(", ").slice(0, 3).join(", ");
                    }

                    locText.innerText = activeLocation;
                    
                    // Fill forms to match
                    if (state) {
                        document.getElementById("state-select").value = matchStateSelect(state);
                    }
                    if (neighborhood) {
                        document.getElementById("city-manual-input").value = neighborhood;
                    } else if (city) {
                        document.getElementById("city-manual-input").value = city;
                    }

                    // Reload expanded list of options
                    loadVendors();
                    alert("GPS Locked! Dynamic address detected as: " + activeLocation);
                } else {
                    fallbackSatellites();
                }
            })
            .catch(err => {
                console.error("Reverse Geocode failed: ", err);
                fallbackSatellites();
            });
        },
        (error) => {
            console.warn("Satellite request rejected or timed out: ", error.message);
            alert("Satellites offline or permission denied. Snapping to nearby Bengaluru neighborhood!");
            fallbackSatellites();
        },
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
    );
}

function fallbackSatellites() {
    const locText = document.getElementById("selected-location-text");
    
    // Choose a random Bengaluru test locality
    const neighborhoods = [
        "Indiranagar, Bangalore",
        "Koramangala, Bangalore",
        "HSR Layout, Bangalore",
        "Whitefield, Bangalore",
        "Jayanagar, Bangalore"
    ];
    const detectedLoc = neighborhoods[Math.floor(Math.random() * neighborhoods.length)];
    
    activeLocation = detectedLoc;
    locText.innerText = activeLocation;
    
    document.getElementById("state-select").value = "Karnataka";
    const parts = detectedLoc.split(", ");
    document.getElementById("city-manual-input").value = parts[0];
    
    loadVendors();
}

function matchStateSelect(stateName) {
    if (!stateName) return "";
    stateName = stateName.toLowerCase();
    if (stateName.contains("karnataka")) return "Karnataka";
    if (stateName.contains("maharashtra")) return "Maharashtra";
    if (stateName.contains("delhi")) return "Delhi";
    if (stateName.contains("goa")) return "Goa";
    if (stateName.contains("kerala")) return "Kerala";
    if (stateName.contains("tamil nadu")) return "Tamil Nadu";
    if (stateName.contains("west bengal")) return "West Bengal";
    if (stateName.contains("rajasthan")) return "Rajasthan";
    return "";
}

// polyfill String.contains if needed
if (!String.prototype.contains) {
    String.prototype.contains = function(str) {
        return this.indexOf(str) !== -1;
    };
}

function handleStateChange() {
    const state = document.getElementById("state-select").value;
    if (state) {
        // Set fallback city based on selection to make manual typing easier
        const cityInput = document.getElementById("city-manual-input");
        if (state === "Karnataka") cityInput.value = "Bangalore";
        else if (state === "Maharashtra") cityInput.value = "Mumbai";
        else if (state === "Delhi") cityInput.value = "New Delhi";
        else if (state === "Goa") cityInput.value = "Panaji";
        else if (state === "Kerala") cityInput.value = "Kochi";
        else if (state === "Tamil Nadu") cityInput.value = "Chennai";
        else if (state === "West Bengal") cityInput.value = "Kolkata";
        else if (state === "Rajasthan") cityInput.value = "Jaipur";
    }
}

function handleCitySearch(event) {
    if (event.key === "Enter") {
        applyManualLocation();
    }
}

function applyManualLocation() {
    const state = document.getElementById("state-select").value;
    const city = document.getElementById("city-manual-input").value.trim();

    if (!city) {
        alert("Please select a state and specify a city or locality name.");
        return;
    }

    if (state) {
        activeLocation = state + ", " + city;
    } else {
        activeLocation = city;
    }

    document.getElementById("selected-location-text").innerText = activeLocation;
    loadVendors();
}

// ==========================================================================
// TABS AND VENDORS LOADING
// ==========================================================================
function switchDashboardTab(tab) {
    activeTab = tab;
    
    // Manage tab buttons
    document.querySelectorAll(".tab-btn").forEach(btn => btn.classList.remove("active"));
    document.getElementById("tab-btn-" + tab).classList.add("active");

    // Manage panels
    document.querySelectorAll(".tab-content").forEach(panel => panel.classList.remove("active"));
    document.getElementById("panel-" + tab).classList.add("active");

    if (tab === "rent" || tab === "mechanic") {
        loadVendors();
    } else if (tab === "history") {
        loadBookingHistory();
    }
}

function loadVendors() {
    const rentContainer = document.getElementById("rentals-list");
    const mechanicContainer = document.getElementById("mechanics-list");

    const queryLocation = activeLocation;
    const serviceType = activeTab === "rent" ? "Rent" : "Mechanic";

    // Set loading indicator
    if (activeTab === "rent") {
        rentContainer.innerHTML = "<div class='no-vendors-box'>Searching rental suppliers...</div>";
    } else {
        mechanicContainer.innerHTML = "<div class='no-vendors-box'>Searching repair workshops...</div>";
    }

    // Fetch from Java back-end
    fetch(`/api/vendors?location=${encodeURIComponent(queryLocation)}&serviceType=${serviceType}`)
    .then(res => res.json())
    .then(vendors => {
        const targetContainer = activeTab === "rent" ? rentContainer : mechanicContainer;
        targetContainer.innerHTML = "";

        if (vendors.length === 0) {
            targetContainer.innerHTML = `
                <div class="no-vendors-box">
                    <div class="no-vendors-icon">🔍</div>
                    <h4>No direct matches in ${queryLocation}</h4>
                    <p>Change your location criteria above or try searching "Koramangala" or "Indiranagar".</p>
                </div>
            `;
            return;
        }

        // Render vendor cards
        vendors.forEach(vendor => {
            const card = document.createElement("div");
            card.className = "vendor-card";

            const actionLabel = activeTab === "rent" ? "Rent Bike" : "Schedule Service";
            const priceUnit = activeTab === "rent" ? "per day" : "base rate";

            card.innerHTML = `
                <div class="vendor-header">
                    <div>
                        <h4 class="vendor-title">${vendor.name}</h4>
                        <div class="vendor-tag">${vendor.serviceType === "Rent" ? "Rental Fleet" : "Standard Garage"}</div>
                    </div>
                    <span class="vendor-rating">★ ${vendor.rating}</span>
                </div>
                <p class="vendor-description">${vendor.description}</p>
                <div class="vendor-details-list">
                    <div class="vendor-detail-row">
                        <span class="vendor-detail-label">Base Location:</span>
                        <span class="vendor-detail-val">${vendor.city}, ${vendor.state}</span>
                    </div>
                    <div class="vendor-detail-row">
                        <span class="vendor-detail-label">Distance:</span>
                        <span class="vendor-detail-val">${vendor.distance} km away</span>
                    </div>
                    <div class="vendor-detail-row">
                        <span class="vendor-detail-label">Helpline:</span>
                        <span class="vendor-detail-val">${vendor.contact}</span>
                    </div>
                </div>
                <div class="vendor-price-box">
                    <span class="vendor-price">₹${vendor.price}</span>
                    <span class="vendor-price-unit"> ${priceUnit}</span>
                </div>
                <button class="btn btn-primary btn-block" onclick="processVendorBooking('${vendor.name}', '${vendor.contact}', ${vendor.price}, '${vendor.serviceType}')">
                    ${actionLabel}
                </button>
            `;
            targetContainer.appendChild(card);
        });
    })
    .catch(err => {
        const targetContainer = activeTab === "rent" ? rentContainer : mechanicContainer;
        targetContainer.innerHTML = `
            <div class="no-vendors-box">
                <h4>Unable to communicate with the application center</h4>
                <p>Ensure the Java backend is active.</p>
            </div>
        `;
    });
}

// ==========================================================================
// BOOKING LOGIC FLOW
// ==========================================================================
function processVendorBooking(vendorName, contact, price, serviceType) {
    // Determine booking date
    let selectedDate = "";
    if (serviceType === "Rent") {
        selectedDate = document.getElementById("rental-date").value;
        if (!selectedDate) {
            alert("Please select a Rental Date before booking!");
            document.getElementById("rental-date").focus();
            return;
        }
    } else {
        selectedDate = document.getElementById("service-date").value;
        if (!selectedDate) {
            alert("Please select a Service Date before scheduling!");
            document.getElementById("service-date").focus();
            return;
        }
    }

    const details = serviceType === "Rent" ? "Motorcycle rental reservation" : "Scheduled maintenance check";

    // Submit booking request
    fetch("/api/book", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: currentUser.email,
            vendorName: vendorName,
            vendorContact: contact,
            date: selectedDate,
            serviceType: serviceType === "Rent" ? "Bike Rental" : "Scheduled Service",
            cost: price.toString(),
            details: details
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Populate Modal Dialog
            document.getElementById("modal-title").innerText = serviceType === "Rent" ? "Rental Reserved!" : "Service Scheduled!";
            document.getElementById("modal-message").innerText = "Your request was processed successfully. Here are the booking details:";
            
            document.getElementById("modal-details").innerHTML = `
                <div class="modal-detail-line">
                    <span class="modal-detail-label">Supplier:</span>
                    <span class="modal-detail-val">${vendorName}</span>
                </div>
                <div class="modal-detail-line">
                    <span class="modal-detail-label">Contact helpline:</span>
                    <span class="modal-detail-val">${contact}</span>
                </div>
                <div class="modal-detail-line">
                    <span class="modal-detail-label">Scheduled Date:</span>
                    <span class="modal-detail-val">${selectedDate}</span>
                </div>
                <div class="modal-detail-line">
                    <span class="modal-detail-label">Amount Due:</span>
                    <span class="modal-detail-val">₹${price}</span>
                </div>
            `;

            // Display Success Dialog
            document.getElementById("success-modal").classList.remove("hidden");
        } else {
            alert("Failed to create booking: " + data.message);
        }
    });
}

// ==========================================================================
// EMERGENCY & "NEED HELP" MECHANIC FLOW
// ==========================================================================
function toggleHelpCenter() {
    const box = document.getElementById("help-center-box");
    box.classList.toggle("hidden");
}

function selectIssue(issueName, cost) {
    selectedEmergencyIssue = issueName;
    selectedEmergencyCost = cost;

    // Reset button outlines
    document.querySelectorAll(".btn-outline-issue").forEach(btn => {
        btn.classList.remove("selected");
        if (btn.innerText === issueName || (issueName === "Brake & Chain Fix" && btn.innerText === "Chain/Brake Issue")) {
            btn.classList.add("selected");
        }
    });
}

function submitEmergencyRequest() {
    let finalIssue = selectedEmergencyIssue;
    let finalCost = selectedEmergencyCost;

    const desc = document.getElementById("custom-issue-desc").value.trim();
    
    if (desc) {
        finalIssue = finalIssue ? finalIssue + " (" + desc + ")" : desc;
        if (finalCost === 0) finalCost = 350; // default emergency call price if custom
    }

    if (!finalIssue) {
        alert("Please select one of the issue buttons or describe the problem in the text field.");
        return;
    }

    const today = new Date().toISOString().split("T")[0];

    // Find custom simulated mechanics in this location
    fetch(`/api/vendors?location=${encodeURIComponent(activeLocation)}&serviceType=Mechanic`)
    .then(res => res.json())
    .then(mechanics => {
        let mechanic = { name: "VegaRide Emergency Rescue Team", contact: "+91 99999 00000" };
        if (mechanics.length > 0) {
            mechanic = mechanics[0]; // Pick the first closest one
        }

        // Post emergency booking
        fetch("/api/book", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: currentUser.email,
                vendorName: mechanic.name,
                vendorContact: mechanic.contact,
                date: today,
                serviceType: "Emergency Support",
                cost: finalCost.toString(),
                details: "Roadside Rescue: " + finalIssue
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                // Populate Modal Dialog
                document.getElementById("modal-title").innerText = "Emergency Help Dispatched!";
                document.getElementById("modal-message").innerText = "Help is on the way! We found a specialist close to your location.";
                
                document.getElementById("modal-details").innerHTML = `
                    <div class="modal-detail-line">
                        <span class="modal-detail-label">Assigned Helper:</span>
                        <span class="modal-detail-val">${mechanic.name}</span>
                    </div>
                    <div class="modal-detail-line">
                        <span class="modal-detail-label">Direct Helpline:</span>
                        <span class="modal-detail-val">${mechanic.contact}</span>
                    </div>
                    <div class="modal-detail-line">
                        <span class="modal-detail-label">Current Location:</span>
                        <span class="modal-detail-val">${activeLocation}</span>
                    </div>
                    <div class="modal-detail-line">
                        <span class="modal-detail-label">Assistance Call Cost:</span>
                        <span class="modal-detail-val">₹${finalCost}</span>
                    </div>
                `;

                // Display Success Dialog
                document.getElementById("success-modal").classList.remove("hidden");
                
                // Hide Help center and clean up selections
                document.getElementById("help-center-box").classList.add("hidden");
                document.getElementById("custom-issue-desc").value = "";
                document.querySelectorAll(".btn-outline-issue").forEach(btn => btn.classList.remove("selected"));
                selectedEmergencyIssue = "";
                selectedEmergencyCost = 0;
            } else {
                alert("Could not process rescue dispatch: " + data.message);
            }
        });
    });
}

// ==========================================================================
// HISTORY TIMELINE LOGS RENDER
// ==========================================================================
function loadBookingHistory() {
    const list = document.getElementById("history-list");
    list.innerHTML = "<div class='no-vendors-box'>Syncing safety history logs...</div>";

    fetch(`/api/history?email=${encodeURIComponent(currentUser.email)}`)
    .then(res => res.json())
    .then(bookings => {
        list.innerHTML = "";
        if (bookings.length === 0) {
            list.innerHTML = `
                <div class="no-vendors-box" style="border:none; padding: 20px;">
                    <div class="no-vendors-icon">📜</div>
                    <h4>No logs in your history file yet</h4>
                    <p>Book a rental or request emergency roadside mechanic to see logs here.</p>
                </div>
            `;
            return;
        }

        // Render bookings reverse-chronologically (newest first)
        bookings.reverse().forEach(booking => {
            const card = document.createElement("div");
            
            const isEmergency = booking.serviceType === "Emergency Support";
            card.className = "history-card" + (isEmergency ? " type-emergency" : "");

            card.innerHTML = `
                <div class="history-card-header">
                    <div>
                        <h4 class="history-card-title">${booking.vendorName}</h4>
                        <span class="history-card-date">${booking.date}</span>
                    </div>
                    <span class="history-card-status">${booking.serviceType}</span>
                </div>
                <div class="history-card-details">
                    <strong>Service description:</strong> ${booking.details} <br>
                    <strong>Provider mobile number:</strong> ${booking.vendorContact}
                </div>
                <div class="history-card-footer">
                    <span class="history-card-cost">Amount: ₹${booking.cost}</span>
                    <span class="history-card-status" style="background-color: #d1e7dd; color: #0f5132;">Confirmed</span>
                </div>
            `;
            list.appendChild(card);
        });
    })
    .catch(err => {
        list.innerHTML = "<div class='no-vendors-box'>Could not sync history file with backend.</div>";
    });
}

// Close confirmation dialogue and refresh active tabs
function closeSuccessModal() {
    document.getElementById("success-modal").classList.add("hidden");
    // Switch to history tab to show the new ticket instantly!
    switchDashboardTab("history");
}
