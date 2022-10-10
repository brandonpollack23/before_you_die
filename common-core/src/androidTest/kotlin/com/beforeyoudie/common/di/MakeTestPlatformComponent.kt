package com.beforeyoudie.common.di

import android.content.Context
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import me.tatarka.inject.annotations.Component
import java.io.File

@Component
abstract class TestBydPlatformAndroidComponent(
  context: Context,
  databaseFileName: DatabaseFileName,
  applicationCoroutineContext: ApplicationCoroutineContext
) : BydPlatformComponent(context, databaseFileName, applicationCoroutineContext) {
  override fun provideSqlDriver(
    databaseFileName: DatabaseFileName,
    isDbInMemory: IsDbInMemory,
    context: Context
  ): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
}

@OptIn(ExperimentalCoroutinesApi::class)
actual fun makeTestPlatformComponent(): BydPlatformComponent {
  val mockContext = mockkClass(Context::class)
  val mockFile = mockkClass(File::class)
  every { mockContext.getDatabasePath(any()) } returns mockFile

  return TestBydPlatformAndroidComponent::class.create(
    mockContext,
    "",
    StandardTestDispatcher(name = "TestMainDispatcher"),
    StandardTestDispatcher(name = "TestIODispatcher"),
  )
}
