import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.CommonWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.App
import com.beforeyoudie.common.di.BydPlatformComponent
import com.beforeyoudie.common.di.CommonBydKotlinInjectAppComponent
import com.beforeyoudie.common.di.DatabaseFileName
import com.beforeyoudie.common.di.create
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun main() {
  // TODO(#6) PRIORITY create issue to have configurable stuff, including log severity + db file,  with config file/command line args, screen resolution.
  // This should also include selections for implementation (eg sqldelight, firebase storage, etc).
  Logger.setMinSeverity(Severity.Verbose)
  Logger.setLogWriters(CommonWriter())

  Logger.v("Test")

  val app = kotlinInjectCreateApp("beforeyoudie.db", Dispatchers.Main)
  app.rootLogic

  application {
    Window(onCloseRequest = ::exitApplication) {
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
      applicationCoroutineContext
    )
  )
