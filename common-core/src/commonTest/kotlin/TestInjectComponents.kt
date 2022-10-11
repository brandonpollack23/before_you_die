import com.beforeyoudie.common.di.ApplicationStoragePlatformComponent
import com.beforeyoudie.common.di.ApplicationStoragePlatformScope
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.DatabaseFileName
import com.beforeyoudie.common.di.IsDbInMemory
import com.beforeyoudie.common.storage.IBydStorage
import io.mockk.mockkClass
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
abstract class MockStoragePlatformComponent : ApplicationStoragePlatformComponent {
    @ApplicationStoragePlatformScope
    @Provides
    fun provideMockStorage(): IBydStorage = mockkClass(IBydStorage::class)

  override fun provideIsInDbInMemory(): IsDbInMemory = true
}

expect fun createTestPlatformComponent(): BydPlatformComponent