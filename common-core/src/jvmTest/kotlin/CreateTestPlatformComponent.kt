import com.beforeyoudie.common.di.ApplicationCoroutineContext
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.IOCoroutineContext
import com.beforeyoudie.common.di.JvmDesktopPlatformComponent
import com.beforeyoudie.common.di.create
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
actual fun createTestPlatformComponent(): BydPlatformComponent {
  val applicationCoroutineContext: ApplicationCoroutineContext =
    StandardTestDispatcher(name = "MainCoroutineContext")
  val ioCoroutineContext: IOCoroutineContext = StandardTestDispatcher(name = "IoCoroutineContext")

  return JvmDesktopPlatformComponent::class.create(applicationCoroutineContext, ioCoroutineContext)
}