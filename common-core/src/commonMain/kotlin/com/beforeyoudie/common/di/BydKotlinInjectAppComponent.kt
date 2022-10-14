package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.beforeyoudie.common.applogic.AppLogicRoot
import com.beforeyoudie.common.applogic.DeepLink
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicEditFactory
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicEditFactoryImpl
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicTaskGraphFactory
import com.beforeyoudie.common.applogic.impl.decompose.AppLogicTaskGraphFactoryImpl
import com.beforeyoudie.common.applogic.impl.decompose.RootDecomposeComponent
import com.beforeyoudie.common.state.TaskIdGenerator
import com.beforeyoudie.common.storage.IBydStorage
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineDispatcher
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import kotlin.coroutines.CoroutineContext

/** Logger used during DI process and construction. */
internal val DILogger = Logger.withTag("kotlin-inject")

// Qualifiers
/** Qualifier typealias for database file name String.*/
typealias DatabaseFileName = String

/** Qualifier typealias for database in memory Boolean.*/
typealias IsDbInMemory = Boolean

/** Qualifier for root component context.*/
typealias RootComponentContext = ComponentContext

/** Qualifier for top level coroutine context provided by the platform.*/
typealias ApplicationCoroutineContext = CoroutineContext

/** Qualifier for the IO dispatcher to be injected.*/
typealias IOCoroutineContext = CoroutineDispatcher

@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationPlatformScope

/** Platform subcomponent, provides things like coroutine execution contexts.*/
@ApplicationPlatformScope
interface BydPlatformComponent {
  @ApplicationPlatformScope
  @Provides
  fun provideApplicationCoroutineContext(): ApplicationCoroutineContext

  @ApplicationPlatformScope
  @Provides
  fun provideIoCoroutineContext(): IOCoroutineContext
}

@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationStoragePlatformScope

@ApplicationStoragePlatformScope
interface ApplicationStoragePlatformComponent {
  @get:ApplicationStoragePlatformScope
  val storage: IBydStorage

  @get:ApplicationStoragePlatformScope
  val sqlDriver: SqlDriver

  @ApplicationStoragePlatformScope
  @Provides
  fun provideDatabaseFileName(): DatabaseFileName

  @ApplicationStoragePlatformScope
  @Provides
  fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory
}

@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationAppLogicScope

/** AppLogic Component implemented with Decompose.*/
@ApplicationAppLogicScope
@Component
abstract class DecomposeAppLogicComponent(
  @Component val storagePlatformComponent: ApplicationStoragePlatformComponent,
  @Component val platformComponent: BydPlatformComponent,
  @get:Provides val deepLink: DeepLink = DeepLink.None
) {
  // There is only ever allowed to be one instance of this at a time, otherwise ChildStack will complain.
  val RootDecomposeComponent.bind: AppLogicRoot
    @ApplicationAppLogicScope
    @Provides
    get() = this

  val AppLogicEditFactoryImpl.bind: AppLogicEditFactory
    @ApplicationAppLogicScope
    @Provides
    get() = this

  val AppLogicTaskGraphFactoryImpl.bind: AppLogicTaskGraphFactory
    @ApplicationAppLogicScope
    @Provides
    get() = this

  @ApplicationAppLogicScope
  @Provides
  fun provideDecomposeLifecycle(): LifecycleRegistry = LifecycleRegistry()

  @ApplicationAppLogicScope
  @Provides
  fun provideRootDefaultDecomposeComponentContext(
    lifecycle: LifecycleRegistry
  ): RootComponentContext =
    DefaultComponentContext(lifecycle)

  @ApplicationAppLogicScope
  @Provides
  open fun provideTaskIdGenerator(): TaskIdGenerator = TaskIdGenerator()
}

/** Scope of the common application component*/
@Scope
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY
)
annotation class ApplicationScope

/** Component used by real implementations.*/
@ApplicationScope
@Component
abstract class BydKotlinInjectAppComponent(
  @Component val platformComponent: BydPlatformComponent,
  @Component val storageComponent: ApplicationStoragePlatformComponent,
  @Component val appLogicComponent: DecomposeAppLogicComponent
) {
  abstract val root: AppLogicRoot
  abstract val lifecycle: LifecycleRegistry
}
