package com.ivo.ganev.awords

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.proto",
    serializer = SettingsSerializer
)