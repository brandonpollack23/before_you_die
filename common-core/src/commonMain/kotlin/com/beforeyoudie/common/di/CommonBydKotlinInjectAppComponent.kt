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
import com.squareup.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

// TODO NOW use javax annotaitions (see readme)
// TODO NOW use dumpGraph

/** Logger used during DI process and construction. */
internal val DILogger = Logger.withTag("kotlin-inject")

/** Scope of the common application component*/
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationScope

/** Scope used by the underlying platform component that implements [BydPlatformComponent]. */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationPlatformScope

// Qualifiers
/** Qualifier typealias for database file name String.*/
typealias DatabaseFileName = String
/** Qualifier typealias for database in memory Boolean.*/
typealias IsDbInMemory = Boolean

/** Make this an interface so that it can be constructed differently in tests vs non tests. */
interface ICommonBydKotlinInjectAppComponent {
  val rootLogic: IBydRoot

  // ========== Bindings =============

  // Bind IBydRoot to the actual Decompose library implementation
  val RootDecomposeComponent.bind: IBydRoot
    @ApplicationScope
    @Provides
    get() = this

  val SqlDelightBydStorage.bind: IBydStorage
    @ApplicationScope
    @Provides
    get() = this

  // ========== Providers =============

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

/** Common component between platforms, in tests use [TestBydKotlinInjectAppComponent] instead, which contains the ability to override etc.*/
@ApplicationScope
@Component
abstract class CommonBydKotlinInjectAppComponent(@Component val platformComponent: BydPlatformComponent) :
  ICommonBydKotlinInjectAppComponent

/** Platform subcomponent, provides things like platform specific sql driver, context, etc.*/
expect abstract class BydPlatformComponent
