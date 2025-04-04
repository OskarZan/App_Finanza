package com.example.financeszan.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.financeszan.ui.screens.CurrencyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    
    val currencyType: CurrencyType = sharedPrefs.getString("currencyType", null)?.let {
        try {
            CurrencyType.valueOf(it)
        } catch (e: Exception) {
            CurrencyType.EURO // Default currency
        }
    } ?: CurrencyType.EURO // Default currency if not set
    
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("isDarkMode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    fun saveCurrencyType(currencyType: CurrencyType) {
        sharedPrefs.edit().putString("currencyType", currencyType.name).apply()
    }
    
    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        sharedPrefs.edit().putBoolean("isDarkMode", newValue).apply()
    }
    
    companion object {
        private var instance: SettingsViewModel? = null
        
        fun getInstance(application: Application): SettingsViewModel {
            if (instance == null) {
                instance = SettingsViewModel(application)
            }
            return instance!!
        }
    }
} 