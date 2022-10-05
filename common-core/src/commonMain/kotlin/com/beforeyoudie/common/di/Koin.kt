package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.BeforeYouDieStorageInterface
import com.beforeyoudie.common.storage.SqlDelightBeforeYouDieStorage
import com.squareup.sqldelight.db.SqlDriver
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.fileProperties

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

/** Load all koin modules, first platform independent shared ones, then platform specific ones, then the ones that depend on platform. */
fun loadKoinModules() =
  loadPlatformIndependentSharedModules() +
    loadPlatformSpecificModule() +
    loadPlatformDependentSharedModules()

/** Platform independent modules that DO NOT depend on things constructed in the platform module. */
fun loadPlatformIndependentSharedModules() = listOf<Module>()

/**
 * Load all platform specific dependencies, including database driver etc.
 */
expect fun loadPlatformSpecificModule(): Module

/** Platform independent modules that depend on things constructed in the platform module. */
fun loadPlatformDependentSharedModules() = listOf(
  module {
    single {
      val driver: SqlDriver = get()
      BeforeYouDieDb.Schema.create(driver)

      val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name).trim('"')
      SqlDelightBeforeYouDieStorage(
        database = BeforeYouDieDb(driver),
        isInMemory = dbFileName.isBlank()
      )
    } withOptions {
      createdAtStart()
    } bind BeforeYouDieStorageInterface::class
  }
)

/** Properties to be read from configuration file koin.properties. */
enum class Properties {
  LOCAL_DATABASE_FILENAME
}
