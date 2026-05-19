package com.example.medlinkapp.data
import com.example.medlinkapp.model.UserRole
import kotlinx.coroutines.delay

class AuthRepository {
    // Simulates a network call to your backend API
    suspend fun authenticateUser(email: String, password: String): Result<UserRole> {
        delay(1500) // Simulate network latency

        return when {
            email.isEmpty() || password.isEmpty() -> {
                Result.failure(Exception("Fields cannot be empty"))
            }
            // Mock authentication logic
            email == "doctor@hospital.com" && password == "password123" -> {
                Result.success(UserRole.DOCTOR)
            }
            email == "patient@clinic.com" && password == "password123" -> {
                Result.success(UserRole.PATIENT)
            }
            else -> {
                Result.failure(Exception("Invalid credentials"))
            }
        }
    }
}