package com.example.insertcode.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.insertcode.data.GuardianPreferences
import com.example.insertcode.data.GuardianRepository

class GuardianViewModelFactory(
    private val repository: GuardianRepository,
    private val preferences: GuardianPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuardianViewModel::class.java)) {
            return GuardianViewModel(repository, preferences) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
