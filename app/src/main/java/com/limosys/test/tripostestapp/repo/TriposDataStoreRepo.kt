package com.limosys.test.tripostestapp.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.limosys.test.tripostestapp.utils.Constants.Companion.KEY_DEVICE_IDENTIFIER
import com.limosys.test.tripostestapp.utils.Constants.Companion.PREFERENCE_DATASTORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

val Context.dataStore :DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_DATASTORE_NAME)

@ViewModelScoped
class TriposDataStoreRepo @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferenceKey {
        val deviceIdentifierKey =  stringPreferencesKey(name = KEY_DEVICE_IDENTIFIER)
    }

    private val dataStore = context.dataStore

    suspend fun storeDeviceIdentifier(identifier: String) {
        this.dataStore.edit { preference ->
            preference[PreferenceKey.deviceIdentifierKey] = identifier
        }
    }

    val getDeviceIdentifier: Flow<String> = this.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {preference ->
            val deviceIdentifier = preference[PreferenceKey.deviceIdentifierKey] ?: ""
            deviceIdentifier
        }
}