import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.beforeyoudie.common.App
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.CommonBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DatabaseFileName
import com.beforeyoudie.common.di.create
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalDecomposeApi::class)
fun main() {
  // TODO(#6) PRIORITY create issue to have configurable stuff, including log severity + db file,
  // with config file/command line args, screen resolution.
  //
  // This should also include selections for implementation (eg sqldelight, firebase storage, etc).
  // You'll also need to restucture the DI system into seperate components.
  Logger.setMinSeverity(Severity.Verbose)
  Logger.setLogWriters(CommonWriter())

  application {
    val coroutineScope = rememberCoroutineScope()
    val app = kotlinInjectCreateApp("beforeyoudie.db", coroutineScope.coroutineContext)

    val windowState = rememberWindowState()
    LifecycleController(app.lifecycle, windowState)

    // TODO UI Look at all these options.
    Window(onCloseRequest = ::exitApplication, state = windowState) {
      MaterialTheme {
        App()
      }
    }
  }
}

fun kotlinInjectCreateApp(
  databaseFileName: DatabaseFileName = "",
  applicationCoroutineContext: CoroutineContext
): CommonBydKotlinInjectAppComponent =
  CommonBydKotlinInjectAppComponent::class.create(
    BydPlatformComponent::class.create(
      databaseFileName,
      applicationCoroutineContext,
      Dispatchers.IO
    )
  )
