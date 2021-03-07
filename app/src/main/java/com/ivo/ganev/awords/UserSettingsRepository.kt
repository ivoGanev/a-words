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

class UserSettingsRepository(context: Context) {
    val settingsFlow = context.settingsDataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(Settings.getDefaultInstance())
        } else {
            throw exception
        }
    }

    companion object {
        @Volatile
        var INSTANCE: UserSettingsRepository? = null

        fun getInstance(context: Context): UserSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserSettingsRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}