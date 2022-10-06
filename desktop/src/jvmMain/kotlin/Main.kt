import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.App
import com.beforeyoudie.common.di.kotlinInjectCreateApp

fun main() {
  // TODO(#6) PRIORITY create issue to have configurable stuff, including log severity + db file,  with config file/command line args.
  Logger.setMinSeverity(Severity.Verbose)
  val app = kotlinInjectCreateApp("beforeyoudie.db")
  val x = app.rootLogic

  application {
    Window(onCloseRequest = ::exitApplication) {
      MaterialTheme {
        App()
      }
    }
  }
}
