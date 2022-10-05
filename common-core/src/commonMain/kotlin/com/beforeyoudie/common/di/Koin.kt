package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.beforeyoudie.common.applogic.impl.RootDecomposeComponent
import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.SqlDelightIBydStorage
import com.squareup.sqldelight.db.SqlDriver
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.fileProperties

/**
 * Main Koin entrypoint for injection.
 */
class BeforeYouDieApplication : KoinComponent

fun startKoin() = org.koin.core.context.startKoin {
  // TODO(#5) LOGGING make a koin configuration to set a log file etc
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
  // Storage interface module.
  module {
    single {
      val driver: SqlDriver = get()
      BeforeYouDieDb.Schema.create(driver)

      val dbFileName = getProperty<String>(Properties.LOCAL_DATABASE_FILENAME.name).trim('"')
      SqlDelightIBydStorage(
        database = BeforeYouDieDb(driver),
        isInMemory = dbFileName.isBlank()
      )
    } withOptions {
      createdAtStart()
    } bind IBydStorage::class
  },
  // Decompose module
  module {
    single {
      LifecycleRegistry()
    }
    single(named(Qualifiers.DefaultComponentContext)) {
      DefaultComponentContext(get())
    }
    single(named(Qualifiers.RootComponent)) {
      RootDecomposeComponent(get())
    }
  }
)

enum class Qualifiers {
  DefaultComponentContext,
  RootComponent,
}

/** Properties to be read from configuration file koin.properties. */
enum class Properties {
  LOCAL_DATABASE_FILENAME
}
