package com.beforeyoudie.common.di

import com.beforeyoudie.common.state.TaskId
import com.beforeyoudie.common.state.TaskIdGenerator
import com.beforeyoudie.common.storage.IBydStorage
import com.benasher44.uuid.uuidOf
import com.squareup.sqldelight.db.SqlDriver
import io.mockk.mockkClass
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import java.nio.ByteBuffer

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

/** Same as real [DecomposeAppLogicComponent] but with deterministic UUID generation.*/
@Component
abstract class TestDecomposeAppLogicComponent(
  storagePlatformComponent: ApplicationStoragePlatformComponent,
  platformComponent: BydPlatformComponent
) : DecomposeAppLogicComponent(storagePlatformComponent, platformComponent) {
  override fun provideTaskIdGenerator(): TaskIdGenerator = object : TaskIdGenerator() {
    var msbs = 0L
    var lsbs = 1L

    override fun generateTaskId(): TaskId {
      val lastIdGenerated = TaskId(
        uuidOf(ByteBuffer.allocate(16).putLong(msbs).putLong(lsbs).array())
      )
      lsbs += 1
      if (lsbs == 0L) {
        // Overflow, inc msbs
        msbs += 1
      }

      return lastIdGenerated
    }
  }
}

expect fun createTestPlatformComponent(): BydPlatformComponent
