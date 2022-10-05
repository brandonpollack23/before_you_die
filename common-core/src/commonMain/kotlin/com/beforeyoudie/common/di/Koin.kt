package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.fileProperties

// TODO NOW make only the SqlDriver platform specific and make the SqlDelight construction shared.  Use named  or something boolean for isInMemory
/**
 * Load all platform specific dependencies, including database driver etc.
 */
expect fun loadPlatformSpecificModule(): Module

/**
 * Main Koin entrypoint for injection.
 */
class BeforeYouDieApplication : KoinComponent

fun startKoin() = org.koin.core.context.startKoin {
  // TODO LOGGING make a koin configuration to set a log file etc
  val kermit = Logger.withTag("koin")
  logger(KermitKoinLogger(kermit))

  fileProperties()

  modules(loadKoinModules())
}

fun loadKoinModules() = listOf(loadPlatformSpecificModule())

enum class Properties {
  LOCAL_DATABASE_FILENAME
}
