import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.JvmDesktopPlatformComponent
import com.beforeyoudie.common.di.create
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val mainCoroutineContext = StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")
  return JvmDesktopPlatformComponent::class.create(mainCoroutineContext, ioCoroutineContext)
}