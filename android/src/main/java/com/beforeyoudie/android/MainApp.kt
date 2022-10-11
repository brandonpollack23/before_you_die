package com.beforeyoudie.android

import android.app.Application
import android.content.Context
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.di.AndroidBydPlatformComponent
import com.beforeyoudie.common.di.AndroidPlatformSqlDelightStorageComponent
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.BydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DatabaseFileName
import com.beforeyoudie.common.di.DecomposeAppLogicComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.create
import kotlinx.coroutines.Dispatchers

class MainApp : Application() {
  override fun onCreate() {
    super.onCreate()

    Logger.setMinSeverity(Severity.Verbose)

    val app = kotlinInjectCreateApp(this, "beforeyoudie.db", Dispatchers.Main, Dispatchers.IO)

    // TODO(#12) Set up decompose lifecycle (in the case that is the library chosen in config).
  }
}

fun kotlinInjectCreateApp(
  context: Context,
  databaseFileName: DatabaseFileName,
  applicationCoroutineContext: ApplicationCoroutineContext,
  ioCoroutineContext: IOCoroutineContext,
  deepLink: DeepLink = DeepLink.None
): BydKotlinInjectAppComponent {
  val platformComponent =
    AndroidBydPlatformComponent::class.create(
      context,
      applicationCoroutineContext,
      ioCoroutineContext
    )
  val platformStorageComponent =
    AndroidPlatformSqlDelightStorageComponent::class.create(platformComponent, databaseFileName)
  val appLogicComponent =
    DecomposeAppLogicComponent::class.create(platformStorageComponent, platformComponent, deepLink)
  return BydKotlinInjectAppComponent::class.create(
    platformComponent,
    platformStorageComponent,
    appLogicComponent
  )
}
