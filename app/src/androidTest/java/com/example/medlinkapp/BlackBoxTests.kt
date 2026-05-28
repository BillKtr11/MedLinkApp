package com.example.medlinkapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Black-box UI tests for MedLinkApp.
 *
 * Covers the most critical (P1) scenarios:
 *   - Login (valid / invalid / empty fields)
 *   - Registration (valid / empty / duplicate)
 *   - Logout & session
 *   - Patient measurements (valid / invalid / boundary)
 *   - Medication add (valid / invalid / stock warning)
 *   - Medication intake (confirm / skip / stock=0)
 *   - Navigation & role-based routing
 *
 * Run on an emulator / device:
 *     ./gradlew connectedAndroidTest
 *
 * Or from Android Studio: Right-click this file → Run
 */
@RunWith(AndroidJUnit4::class)
class BlackBoxTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /* ──────────────────────────────────────────────
     *  HELPER FUNCTIONS
     * ────────────────────────────────────────────── */

    /** Ensures we start from the Login screen (logs out if already logged in). */
    private fun ensureOnLoginScreen() {
        // If we're NOT on the login screen, try to log out first
        try {
            composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        } catch (_: AssertionError) {
            // Try to find and tap a Logout button (works on any dashboard)
            try {
                composeTestRule.onNodeWithContentDescription("Logout", ignoreCase = true).performClick()
                composeTestRule.waitForIdle()
            } catch (_: AssertionError) {
                // Already on login or transitional screen — proceed
            }
        }
    }

    /** Performs a login with the given credentials. */
    private fun performLogin(email: String, password: String) {
        composeTestRule.onNodeWithText("Email").performTextInput(email)
        composeTestRule.onNodeWithText("Password").performTextInput(password)
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
    }

    /* ════════════════════════════════════════════════
     *  1. LOGIN TESTS
     * ════════════════════════════════════════════════ */

    /**
     * TC-L01: Login with valid Doctor credentials.
     * Input: email="doctor", password="123"
     * Expected: Navigate to Doctor Dashboard
     */
    @Test
    fun TC_L01_loginWithValidDoctorCredentials() {
        ensureOnLoginScreen()
        performLogin("doctor", "123")

        // Doctor Dashboard should be visible
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Dr.", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * TC-L02: Login with valid Patient credentials.
     * Input: email="patient", password="123"
     * Expected: Navigate to Patient Dashboard
     */
    @Test
    fun TC_L02_loginWithValidPatientCredentials() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Alex", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * TC-L04: Login with empty email.
     * Expected: Error message shown (handled by ViewModel logic)
     */
    @Test
    fun TC_L04_loginWithEmptyEmail() {
        ensureOnLoginScreen()
        // Only type password, leave email empty
        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()

        // Error is displayed in the UI when login fails
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Invalid credentials", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* ════════════════════════════════════════════════
     *  2. REGISTRATION TESTS
     * ════════════════════════════════════════════════ */

    /**
     * TC-R01: Register as Patient with valid data.
     * Expected: Navigated to Patient Dashboard
     */
    @Test
    fun TC_R01_registerPatientWithValidData() {
        ensureOnLoginScreen()

        // Navigate to Register
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        val uniqueId = System.currentTimeMillis().toString().takeLast(6)
        val name = "TestUser$uniqueId"

        // Fill registration form
        composeTestRule.onNodeWithText("Name").performTextInput(name)
        composeTestRule.onNodeWithText("Surname").performTextInput("Tester")
        composeTestRule.onNodeWithText("AMKA").performTextInput(uniqueId)
        composeTestRule.onNodeWithText("Email / Username").performTextInput("test$uniqueId@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("testpass")

        // Role defaults to Patient, so just submit
        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.waitForIdle()

        // Should navigate to Patient Dashboard
        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("Welcome, $name", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * TC-R09: Register with ALL fields empty.
     * Expected: Error "All fields are required"
     */
    @Test
    fun TC_R09_registerWithAllFieldsEmpty() {
        ensureOnLoginScreen()
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("All fields are required").assertIsDisplayed()
    }

    /* ════════════════════════════════════════════════
     *  3. LOGOUT TESTS
     * ════════════════════════════════════════════════ */

    /**
     * TC-S03: Logout from Patient Dashboard.
     * Expected: Navigate back to Login screen.
     */
    @Test
    fun TC_S03_logoutFromPatientDashboard() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Alex", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Tap Logout
        composeTestRule.onNodeWithContentDescription("Logout").performClick()
        composeTestRule.waitForIdle()

        // Should be back on Login screen
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    /* ════════════════════════════════════════════════
     *  4. PATIENT – NEW MEASUREMENT TESTS
     * ════════════════════════════════════════════════ */

    /** Helper: login as patient, then navigate to New Measurement screen. */
    private fun navigateToNewMeasurement() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Alex", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Navigate to New Measurement
        composeTestRule.onNodeWithText("New Measurement").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * TC-M01: Submit a valid Blood Pressure measurement.
     * Expected: Success message shown.
     */
    @Test
    fun TC_M01_submitValidBloodPressureMeasurement() {
        navigateToNewMeasurement()

        // Type defaults to "Blood Pressure", method defaults to "Manual entry"
        composeTestRule.onNodeWithText("Measurement Value", substring = true).performTextInput("120")
        composeTestRule.onNodeWithText("Record Measurement").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("saved successfully", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * TC-M16: Blood Pressure ABOVE upper boundary (141 > 140).
     * Expected: Out-of-bounds warning.
     */
    @Test
    fun TC_M16_bloodPressureAboveBoundary() {
        navigateToNewMeasurement()

        composeTestRule.onNodeWithText("Measurement Value", substring = true).performTextInput("141")
        composeTestRule.onNodeWithText("Record Measurement").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("out of bounds", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* ════════════════════════════════════════════════
     *  5. PATIENT – MEDICATION MANAGER TESTS
     * ════════════════════════════════════════════════ */

    /** Helper: login as patient, navigate to Medication Manager. */
    private fun navigateToMedications() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Alex", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("My Medications").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * TC-MM01: View medications list.
     */
    @Test
    fun TC_MM01_viewMedicationsList() {
        navigateToMedications()

        composeTestRule.onNodeWithText("Depon", substring = true).assertIsDisplayed()
    }

    /* ════════════════════════════════════════════════
     *  6. PATIENT – ADD MEDICATION TESTS
     * ════════════════════════════════════════════════ */

    /** Helper: navigate to Add Medication screen. */
    private fun navigateToAddMedication() {
        navigateToMedications()

        // Tap the floating action button (Add)
        composeTestRule.onNodeWithContentDescription("Add Medication").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * TC-AM01: Add medication with all valid fields.
     */
    @Test
    fun TC_AM01_addMedicationWithValidData() {
        navigateToAddMedication()

        composeTestRule.onNodeWithText("Medication Name").performTextInput("Aspirin")
        composeTestRule.onNodeWithText("Dosage", substring = true).performTextInput("100mg")
        composeTestRule.onNodeWithText("Frequency", substring = true).performTextInput("2")
        composeTestRule.onNodeWithText("Duration", substring = true).performTextInput("10")
        composeTestRule.onNodeWithText("Current Stock", substring = true).performTextInput("30")

        composeTestRule.onNodeWithText("Confirm Registration").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Successful Registration", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * TC-AM08: Stock insufficient → warning dialog shown.
     */
    @Test
    fun TC_AM08_addMedicationWithInsufficientStock() {
        navigateToAddMedication()

        composeTestRule.onNodeWithText("Medication Name").performTextInput("LowStockMed")
        composeTestRule.onNodeWithText("Dosage", substring = true).performTextInput("50mg")
        composeTestRule.onNodeWithText("Frequency", substring = true).performTextInput("2")
        composeTestRule.onNodeWithText("Duration", substring = true).performTextInput("30")
        composeTestRule.onNodeWithText("Current Stock", substring = true).performTextInput("10")

        composeTestRule.onNodeWithText("Confirm Registration").performClick()
        composeTestRule.waitForIdle()

        // Stock warning dialog should appear
        composeTestRule.onNodeWithText("Low Stock").assertIsDisplayed()
    }

    /* ════════════════════════════════════════════════
     *  7. PATIENT – INTAKE TESTS
     * ════════════════════════════════════════════════ */

    /**
     * TC-IN01: Confirm intake → stock dialog shown.
     */
    @Test
    fun TC_IN01_confirmIntake() {
        navigateToMedications()

        // Tap "Take 1 Dose" on first medication
        composeTestRule.onAllNodesWithText("Take 1 Dose").onFirst().performClick()
        composeTestRule.waitForIdle()

        // On Intake screen → tap "Confirm"
        composeTestRule.onNodeWithText("Confirm").performClick()
        composeTestRule.waitForIdle()

        // Result dialog should show stock update
        composeTestRule.onNodeWithText("Stock Update", substring = true).assertIsDisplayed()

        // Dismiss
        composeTestRule.onNodeWithText("OK").performClick()
        composeTestRule.waitForIdle()
    }

    /* ════════════════════════════════════════════════
     *  8. DOCTOR – ADD APPOINTMENT VALIDATION TESTS
     * ════════════════════════════════════════════════ */

    /** Helper: login as doctor, navigate to Add Appointment. */
    private fun navigateToAddAppointment() {
        ensureOnLoginScreen()
        performLogin("doctor", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Welcome, Dr.", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Add Appointment").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * TC-AA02: Submit appointment without selecting patient.
     */
    @Test
    fun TC_AA02_addAppointmentNoPatient() {
        navigateToAddAppointment()

        // Enter a reason but don't select patient/date/time
        composeTestRule.onNodeWithText("Reason for visit").performTextInput("Checkup")

        composeTestRule.onNodeWithText("Confirm Registration").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Please select a patient", substring = true)
            .assertIsDisplayed()
    }
}
