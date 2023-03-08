package com.limosys.test.tripostestapp.repo

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.limosys.test.tripostestapp.utils.Constants
import javax.inject.Inject

class SharedPrefServiceImpl @Inject constructor(private val context: Context): ISharedPref {
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun setIdentifier(identifier: String) {
        pref.edit(commit = true) {
            putString(Constants.KEY_DEVICE_IDENTIFIER, identifier)
        }
    }

    override fun getIdentifier(): String {
        return pref.getString(Constants.KEY_DEVICE_IDENTIFIER, "")!!
    }
}