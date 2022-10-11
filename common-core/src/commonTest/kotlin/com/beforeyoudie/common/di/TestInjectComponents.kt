package com.beforeyoudie.common.di

import com.beforeyoudie.common.storage.IBydStorage
import com.squareup.sqldelight.db.SqlDriver
import io.mockk.mockkClass
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class MockStoragePlatformComponent : ApplicationStoragePlatformComponent {
  @ApplicationStoragePlatformScope
  @Provides
  fun provideMockStorage(): IBydStorage = mockkClass(IBydStorage::class)

  override fun provideIsInDbInMemory(databaseFileName: DatabaseFileName): IsDbInMemory = true

  override fun provideDatabaseFileName(): DatabaseFileName = ""

  // Unused since storage is mocked
  @Provides
  fun provideSqlDriver(): SqlDriver = mockkClass(SqlDriver::class)
}

expect fun createTestPlatformComponent(): BydPlatformComponent