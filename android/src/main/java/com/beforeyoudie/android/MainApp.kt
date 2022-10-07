package com.beforeyoudie.android

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.di.kotlinInjectCreateApp

class MainApp : Application() {
  override fun onCreate() {
    super.onCreate()

    Logger.setMinSeverity(Severity.Verbose)

    val app = kotlinInjectCreateApp(this, "beforeyoudie.db")
  }
}
