import android.content.Context
import com.beforeyoudie.common.di.AndroidBydPlatformComponent
import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import io.mockk.mockkClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val applicationCoroutineContext: ApplicationCoroutineContext =
    StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext: IOCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")
  val mockContext = mockkClass(Context::class)

  return AndroidBydPlatformComponent::class.create(
    mockContext,
    applicationCoroutineContext,
    ioCoroutineContext
  )
}