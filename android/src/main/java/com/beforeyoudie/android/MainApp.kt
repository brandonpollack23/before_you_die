package com.beforeyoudie.android

import android.app.Application
import android.content.Context
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.CommonBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DatabaseFileName
import com.beforeyoudie.common.di.kotlinInjectCreateApp
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class MainApp : Application() {
  override fun onCreate() {
    super.onCreate()

    Logger.setMinSeverity(Severity.Verbose)

    val app = kotlinInjectCreateApp(this, "beforeyoudie.db", Dispatchers.Main)
  }
}

fun kotlinInjectCreateApp(
  context: Context,
  databaseFileName: DatabaseFileName,
  applicationCoroutineContext: CoroutineContext
): CommonBydKotlinInjectAppComponent =
  CommonBydKotlinInjectAppComponent::class.create(
    BydPlatformComponent::class.create(
      context,
      databaseFileName,
      applicationCoroutineContext
    )
  )
