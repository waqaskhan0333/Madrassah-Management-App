package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class UserRole {
    ADMIN,
    TEACHER
}

object UserRoleManager {
    var currentRole by mutableStateOf(UserRole.ADMIN)
        private set

    val isAdmin: Boolean
        get() = currentRole == UserRole.ADMIN

    val isTeacher: Boolean
        get() = currentRole == UserRole.TEACHER

    fun setRole(role: UserRole) {
        currentRole = role
    }

    fun toggleRole() {
        currentRole = if (currentRole == UserRole.ADMIN) UserRole.TEACHER else UserRole.ADMIN
    }
}
