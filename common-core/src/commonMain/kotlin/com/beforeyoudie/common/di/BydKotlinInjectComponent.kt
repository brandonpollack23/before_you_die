package com.beforeyoudie.common.di

import co.touchlab.kermit.Logger
import com.beforeyoudie.common.applogic.impl.BydEditConstructor
import com.beforeyoudie.common.applogic.impl.BydGraphConstructor
import com.beforeyoudie.common.applogic.impl.EditDecomposeComponent
import com.beforeyoudie.common.applogic.impl.TodoGraphDecomposeComponent
import com.beforeyoudie.common.storage.IBydStorage
import com.beforeyoudie.common.storage.SqlDelightBydStorage
import com.beforeyoudie.common.util.getClassLogger
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

// TODO NOW migrate tests
// TODO NOW Document
// TODO NOW use javax annotaitions

internal val DILogger = Logger.withTag("kotlin-inject")

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ApplicationScope

@ApplicationScope
@Component
abstract class BydKotlinInjectComponent(
  @get:ApplicationScope @get:Provides
  val databaseFileName: DatabaseFileName = "",
  @Component val platformKotlinInjectComponent: BydPlatformInjectComponent
) {
  @Provides
  inline fun <reified T> provideClassLogger(): Logger = getClassLogger<T>()

  protected val SqlDelightBydStorage.bind: IBydStorage
    @ApplicationScope
    @Provides
    get() = this

  @ApplicationScope
  @Provides
  fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory =
    databaseFileName.trim('"').isEmpty()

  @ApplicationScope
  @Provides
  fun provideBydGraphConstructor(): BydGraphConstructor =
    { graphConfig, componentContext -> TodoGraphDecomposeComponent(graphConfig, componentContext) }

  @ApplicationScope
  @Provides
  fun provideBydEditConstructor(): BydEditConstructor =
    { editConfig, componentContext -> EditDecomposeComponent(editConfig, componentContext) }
}

typealias DatabaseFileName = String
typealias IsDbInMemory = Boolean

expect abstract class BydPlatformInjectComponent(databaseFileName: DatabaseFileName = "")
