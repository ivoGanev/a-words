package com.ivo.ganev.awords

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.catch
import java.io.IOException

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)

class UserSettingsRepository(val context: Context) {
    val settingsFlow = context.settingsDataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(Settings.getDefaultInstance())
        } else {
            throw exception
        }
    }
}