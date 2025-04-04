package com.example.financeszan.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeszan.ui.screens.LaboralStatus
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    
    var firstName: String = sharedPrefs.getString("firstName", "") ?: ""
        private set
        
    var lastName: String = sharedPrefs.getString("lastName", "") ?: ""
        private set
        
    var laboralStatus: LaboralStatus? = sharedPrefs.getString("laboralStatus", null)?.let {
        try {
            LaboralStatus.valueOf(it)
        } catch (e: Exception) {
            null
        }
    }
        private set
        
    var profilePhotoUri: Uri? = sharedPrefs.getString("profilePhotoUri", null)?.let {
        try {
            Uri.parse(it)
        } catch (e: Exception) {
            null
        }
    }
        private set
    
    fun saveProfileData(
        firstName: String,
        lastName: String,
        laboralStatus: LaboralStatus?,
        photoUri: Uri?
    ) {
        viewModelScope.launch {
            this@ProfileViewModel.firstName = firstName
            this@ProfileViewModel.lastName = lastName
            this@ProfileViewModel.laboralStatus = laboralStatus
            this@ProfileViewModel.profilePhotoUri = photoUri
            
            sharedPrefs.edit().apply {
                putString("firstName", firstName)
                putString("lastName", lastName)
                putString("laboralStatus", laboralStatus?.name)
                putString("profilePhotoUri", photoUri?.toString())
                apply()
            }
        }
    }
} 