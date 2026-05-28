package com.example.medlinkapp.data
import com.example.medlinkapp.model.UserRole
import com.example.medlinkapp.model.User
import kotlinx.coroutines.delay

class AuthRepository {
    suspend fun authenticateUser(email: String, password: String): Result<User> {
        delay(500) // Simulating network delay

        // Check persistent users first
        val users = DBManager.users.value
        val foundUser = users.find { it.email == email && it.password == password }
        
        if (foundUser != null) {
            DBManager.setCurrentUser(foundUser.amka)
            return Result.success(foundUser)
        }

        // Check hardcoded defaults
        return when {
            email.isEmpty() || password.isEmpty() -> {
                Result.failure(Exception("Fields cannot be empty"))
            }
            email == "doctor" && password == "123" -> {
                val doctor = users.find { it.email == "doctor" } ?: User("Dr. Lee", "George", "111111", "doctor", "123", UserRole.DOCTOR)
                DBManager.setCurrentUser(doctor.amka)
                Result.success(doctor)
            }
            email == "patient" && password == "123" -> {
                val patient = users.find { it.email == "patient" } ?: User("Demo", "Patient", "000000", "patient", "123", UserRole.PATIENT)
                DBManager.setCurrentUser(patient.amka)
                Result.success(patient)
            }
            email == "caregiver" && password == "123" -> {
                val caregiver = users.find { it.email == "caregiver" } ?: User("Anna", "Caregiver", "222222", "caregiver", "123", UserRole.CAREGIVER)
                DBManager.setCurrentUser(caregiver.amka)
                Result.success(caregiver)
            }
            else -> {
                Result.failure(Exception("Invalid credentials"))
            }
        }
    }

    suspend fun registerUser(userData: User): Result<Unit> {
        delay(500)
        
        // Check if user already exists
        val exists = DBManager.users.value.any { it.email == userData.email || it.amka == userData.amka }
        if (exists) {
            return Result.failure(Exception("User already exists with this Email or AMKA"))
        }

        DBManager.registerUser(userData)
        DBManager.setCurrentUser(userData.amka)
        return Result.success(Unit)
    }
}
