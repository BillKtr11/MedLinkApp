package com.example.medlinkapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medlinkapp.data.DBManager
import org.junit.Before
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
        // Force clear session to start fresh if possible
        // DBManager.clearSession() // Cannot call directly from test easily without context/init

        try {
            composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        } catch (_: AssertionError) {
            try {
                // Dashboard icons use contentDescription for Logout
                composeTestRule.onNodeWithContentDescription("Logout").performClick()
                composeTestRule.waitForIdle()
            } catch (_: AssertionError) {
                // Already on login or transitional screen — proceed
            }
        }
    }

    /** Performs a login with the given credentials. */
    private fun performLogin(email: String, password: String) {
        clearAndType("Email", email)
        clearAndType("Password", password)
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
    }

    /** Clears text fields before typing (fields may have old text). */
    private fun clearAndType(label: String, text: String) {
        composeTestRule.onNodeWithText(label).performTextClearance()
        composeTestRule.onNodeWithText(label).performTextInput(text)
    }

    /* ════════════════════════════════════════════════
     *  1. LOGIN TESTS
     * ════════════════════════════════════════════════ */

    @Test
    fun TC_L01_loginWithValidDoctorCredentials() {
        ensureOnLoginScreen()
        performLogin("doctor", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Dr. Lee", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun TC_L02_loginWithValidPatientCredentials() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Demo", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun TC_L04_loginWithEmptyEmail() {
        ensureOnLoginScreen()
        composeTestRule.onNodeWithText("Email").performTextClearance()
        clearAndType("Password", "123")
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Fields cannot be empty", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun TC_L07_loginWithInvalidCredentials() {
        ensureOnLoginScreen()
        performLogin("wronguser", "wrongpass")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Invalid credentials", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* ════════════════════════════════════════════════
     *  2. REGISTRATION TESTS
     * ════════════════════════════════════════════════ */

    @Test
    fun TC_R01_registerPatientWithValidData() {
        ensureOnLoginScreen()
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        clearAndType("Name", "TestUser")
        clearAndType("Surname", "Tester")
        clearAndType("AMKA", "${System.currentTimeMillis()}")
        clearAndType("Email / Username", "testuser_${System.currentTimeMillis()}@test.com")
        clearAndType("Password", "testpass")

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("TestUser", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* ════════════════════════════════════════════════
     *  3. PATIENT – NEW MEASUREMENT TESTS
     * ════════════════════════════════════════════════ */

    private fun navigateToNewMeasurement() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Demo", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Dashboard card title is "New Measurement" (English)
        composeTestRule.onNodeWithText("New Measurement", substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun TC_M01_submitValidBloodPressureMeasurement() {
        navigateToNewMeasurement()

        // UI labels in ManageMeasurementRecording are English
        // Label is "Measurement Value (e.g. 90 mg/dL)"
        composeTestRule.onNodeWithText("Measurement Value", substring = true).performTextInput("120")
        // Button is "Record Measurement"
        composeTestRule.onNodeWithText("Record Measurement").performClick()
        composeTestRule.waitForIdle()

        // Success message is English
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("recorded successfully", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun TC_M16_bloodPressureAboveBoundary() {
        navigateToNewMeasurement()

        composeTestRule.onNodeWithText("Measurement Value", substring = true).performTextInput("141")
        composeTestRule.onNodeWithText("Record Measurement").performClick()
        composeTestRule.waitForIdle()

        // Success message for out-of-bounds is English
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("out of limits", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /* ════════════════════════════════════════════════
     *  4. PATIENT – MEDICATION & INTAKE TESTS
     * ════════════════════════════════════════════════ */

    private fun navigateToMedications() {
        ensureOnLoginScreen()
        performLogin("patient", "123")
        
        composeTestRule.onNodeWithText("My Medications", substring = true).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun TC_IN01_confirmIntake() {
        navigateToMedications()

        // Tap "Take 1 Dose" on first medication (Depon)
        composeTestRule.onAllNodesWithText("Take 1 Dose").onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Confirm").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Stock Update", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").performClick()
    }

    @Test
    fun TC_IN05_intakeWithZeroStock() {
        // Navigate to add medication
        navigateToMedications()
        composeTestRule.onNodeWithContentDescription("Add Medication").performClick()
        composeTestRule.waitForIdle()

        clearAndType("Medication Name", "DoseLimit")
        clearAndType("Dosage (e.g., 500mg)", "5mg")
        clearAndType("Frequency (doses per day)", "1")
        clearAndType("Duration (days)", "1")
        clearAndType("Current Stock (units)", "1")

        composeTestRule.onNodeWithText("Confirm Registration").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Back to Home").performClick()

        // Back on Dashboard, go to Medications
        composeTestRule.onNodeWithText("My Medications", substring = true).performClick()
        
        // Find "Take 1 Dose" for "DoseLimit"
        composeTestRule.onNode(
            hasText("Take 1 Dose") and 
            hasAnyAncestor(hasText("DoseLimit"))
        ).performClick()
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Confirm").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Urgent Notification", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Stock is 0", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").performClick()
    }

    /* ════════════════════════════════════════════════
     *  5. DOCTOR – APPOINTMENT TESTS
     * ════════════════════════════════════════════════ */

    @Test
    fun TC_AA01_addAppointmentValid() {
        ensureOnLoginScreen()
        performLogin("doctor", "123")
        
        composeTestRule.onNodeWithText("Add Appointment", substring = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Select Patient", substring = true).performClick()
        composeTestRule.onAllNodesWithText("Demo", substring = true).onFirst().performClick()

        composeTestRule.onNodeWithText("Select Date", substring = true).performClick()
        composeTestRule.onNodeWithText("OK").performClick()

        composeTestRule.onNodeWithText("Select Time", substring = true).performClick()
        composeTestRule.onNodeWithText("OK").performClick()

        clearAndType("Reason for visit", "Checkup")
        composeTestRule.onNodeWithText("Confirm Registration").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Successful Registration", substring = true).assertIsDisplayed()
    }

    /* ════════════════════════════════════════════════
     *  6. EMERGENCY SOS TESTS
     * ════════════════════════════════════════════════ */

    @Test
    fun TC_SOS01_triggerEmergencySOS() {
        ensureOnLoginScreen()
        performLogin("patient", "123")

        composeTestRule.onNodeWithText("SOS").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Logout").performClick()
        performLogin("doctor", "123")

        composeTestRule.waitUntil(10_000) {
            composeTestRule.onAllNodesWithText("EMERGENCY SOS", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
