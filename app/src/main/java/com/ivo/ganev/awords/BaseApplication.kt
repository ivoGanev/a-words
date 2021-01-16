package com.ivo.ganev.awords

import android.app.Application
import dagger.Component

// appComponent lives in the Application class to share its lifecycle
@Component
interface ApplicationComponent {}

class AWordsApplication: Application() {
    // Reference to the application graph that is used across the whole app
    val appComponent = DaggerApplicationComponent.create()
}