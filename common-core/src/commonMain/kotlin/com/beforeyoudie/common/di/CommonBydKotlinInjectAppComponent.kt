package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicEditFactory
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicEditFactoryImpl
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicRootDecomposeComponent
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicTaskGraphFactory
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicTaskGraphFactoryImpl
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.impl.BeforeYouDieDb
import com.beforeyoudie.common.storage.impl.SqlDelightBydStorage
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import kotlin.coroutines.CoroutineContext

/** Logger used during DI process and construction. */
internal val DILogger = Logger.withTag("kotlin-inject")

/** Scope of the common application component*/
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationScope

// Qualifiers
/** Qualifier typealias for database file name String.*/
typealias DatabaseFileName = String
/** Qualifier typealias for database in memory Boolean.*/
typealias IsDbInMemory = Boolean
/** Qualifier for top level coroutine context provided by the platform.*/
typealias ApplicationCoroutineContext = CoroutineContext
/** Qualifier for the IO dispatcher to be injected.*/
typealias IOCoroutineContext = CoroutineDispatcher

/** Make this an interface so that it can be constructed differently in tests vs non tests. */
interface ICommonBydKotlinInjectAppComponent {
  val rootLogic: AppLogicRoot
  val lifecycle: LifecycleRegistry

  // ========== Providers =============

  @ApplicationScope
  @Provides
  fun provideDecomposeLifecycle(): LifecycleRegistry = LifecycleRegistry()

  @ApplicationScope
  @Provides
  fun provideRootDefaultDecomposeComponentContext(lifecycle: Lifecycle): ComponentContext =
    DefaultComponentContext(lifecycle)

}

@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationPlatformScope

/** Platform subcomponent, provides things like platform specific sql driver, context, etc.*/
@ApplicationPlatformScope
interface BydPlatformComponent {
  @get:ApplicationPlatformScope
  val applicationCoroutineContext: ApplicationCoroutineContext

  @get:ApplicationPlatformScope
  val ioCoroutineContext: IOCoroutineContext
}

@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationStoragePlatformScope

// TODO NOW seperate no platform and platform storage
@ApplicationStoragePlatformScope
interface ApplicationStoragePlatformComponent {
  @get:ApplicationStoragePlatformScope
  val storage: IBydStorage

  @get:ApplicationStoragePlatformScope
  val databaseFileName: DatabaseFileName

  @get:ApplicationStoragePlatformScope
  val sqlDriver: SqlDriver

  @ApplicationStoragePlatformScope
  @Provides
  fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory
}

/** Component used by real implementations.*/
@ApplicationScope
@Component
abstract class CommonBydKotlinInjectAppComponent(
  @Component val platformComponent: BydPlatformComponent,
  @Component val storageComponent: ApplicationStoragePlatformComponent,
  @get:Provides val deepLink: DeepLink = DeepLink.None
) :
  ICommonBydKotlinInjectAppComponent {

  // TODO NOW move to decompose component
  val AppLogicRootDecomposeComponent.bind: AppLogicRoot
    @ApplicationScope
    @Provides
    get() = this

  val AppLogicEditFactoryImpl.bind: AppLogicEditFactory
    @ApplicationScope
    @Provides
    get() = this

  val AppLogicTaskGraphFactoryImpl.bind: AppLogicTaskGraphFactory
    @ApplicationScope
    @Provides
    get() = this

  val LifecycleRegistry.bind: Lifecycle
    @ApplicationScope
    @Provides
    get() = this

  }
