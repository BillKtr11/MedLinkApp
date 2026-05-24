package com.example.medlinkapp.data
import com.example.medlinkapp.model.UserRole
import kotlinx.coroutines.delay

class AuthRepository {
    suspend fun authenticateUser(email: String, password: String): Result<UserRole> {
        delay(1500) // latency

        return when {
            email.isEmpty() || password.isEmpty() -> {
                Result.failure(Exception("Fields cannot be empty"))
            }
            email == "doctor" && password == "123" -> {
                Result.success(UserRole.DOCTOR)
            }
            email == "patient" && password == "123" -> {
                Result.success(UserRole.PATIENT)
            }
            else -> {
                Result.failure(Exception("Invalid credentials"))
            }
        }
    }
}