package com.miso.vinilo.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object GlobalRoleState {
    var selectedRole by mutableStateOf("Usuario")

    fun updateRole(newRole: String) {
        selectedRole = newRole
    }
}