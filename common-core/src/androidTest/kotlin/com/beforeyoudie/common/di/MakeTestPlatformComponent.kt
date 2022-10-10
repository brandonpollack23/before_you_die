package com.beforeyoudie.common.di

import android.content.Context
import io.mockk.every
import io.mockk.mockkClass
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
actual fun makeTestPlatformComponent(): BydPlatformComponent {
  val mockContext = mockkClass(Context::class)
  val mockFile = mockkClass(File::class)
  every { mockContext.getDatabasePath(any()) } returns mockFile
  every { mockFile.path } returns ""

  return BydPlatformComponent::class.create(
    mockContext,
    SQLiteDatabaseConfiguration.MEMORY_DB_PATH,
    StandardTestDispatcher()
  )
}
