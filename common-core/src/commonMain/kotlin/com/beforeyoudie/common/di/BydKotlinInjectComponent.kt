package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.beforeyoudie.common.applogic.IBydRoot
import com.beforeyoudie.common.applogic.impl.BydEditConstructor
import com.beforeyoudie.common.applogic.impl.BydGraphConstructor
import com.beforeyoudie.common.applogic.impl.EditDecomposeComponent
import com.beforeyoudie.common.applogic.impl.RootDecomposeComponent
import com.beforeyoudie.common.applogic.impl.TodoGraphDecomposeComponent
import com.beforeyoudie.common.storage.BeforeYouDieDb
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.SqlDelightBydStorage
import com.beforeyoudie.common.util.getClassLogger
import com.squareup.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

// TODO NOW migrate tests
// TODO NOW Document
// TODO NOW use javax annotaitions (see readme)
// TODO NOW remove koin

internal val DILogger = Logger.withTag("kotlin-inject")

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationScope

// Qualifiers
typealias DatabaseFileName = String
typealias IsDbInMemory = Boolean

@ApplicationScope
@Component
abstract class BydKotlinInjectComponent(
  @Component val platformKotlinInjectComponent: BydPlatformInjectComponent
) {
  abstract val rootLogic: IBydRoot

  // ========== Bindings =============

  // Bind IBydRoot to the actual Decompose library implementation
  protected val RootDecomposeComponent.bind: IBydRoot
    @Provides get() = this

  protected val SqlDelightBydStorage.bind: IBydStorage
    @ApplicationScope
    @Provides
    get() = this

  // ========== Providers =============

  @Provides
  inline fun <reified T> provideClassLogger(): Logger = getClassLogger<T>()


  @ApplicationScope
  @Provides
  fun provideBydGraphConstructor(): BydGraphConstructor =
    { graphConfig, componentContext -> TodoGraphDecomposeComponent(graphConfig, componentContext) }

  @ApplicationScope
  @Provides
  fun provideBydEditConstructor(): BydEditConstructor =
    { editConfig, componentContext -> EditDecomposeComponent(editConfig, componentContext) }

  @ApplicationScope
  @Provides
  fun provideDecomposeLifecycle(): Lifecycle = LifecycleRegistry()
  @ApplicationScope
  @Provides
  fun provideRootDefaultDecomposeComponentContext(lifecycle: Lifecycle): ComponentContext =
    DefaultComponentContext(lifecycle)

  /** Must set up the database schema, creating tables etc. before returning the database. */
  @ApplicationScope
  @Provides
  fun provideBeforeYouDieDb(driver: SqlDriver): BeforeYouDieDb {
    BeforeYouDieDb.Schema.create(driver)
    return BeforeYouDieDb(driver)
  }
}

expect abstract class BydPlatformInjectComponent(databaseFileName: DatabaseFileName = "")
