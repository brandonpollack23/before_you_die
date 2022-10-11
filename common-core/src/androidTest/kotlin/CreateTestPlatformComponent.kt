import android.content.Context
import com.beforeyoudie.common.di.AndroidBydPlatformComponent
import com.beforeyoudie.common.di.BydPlatformComponent
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val mainCoroutineContext = StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")
  val mockContext = mockkClass(Context::class)

  return AndroidBydPlatformComponent::class.create(
    mockContext,
    mainCoroutineContext,
    ioCoroutineContext
  )
}