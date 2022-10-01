import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.beforeyoudie.common.App
import com.beforeyoudie.common.di.startKoin

fun main() {
    Logger.setMinSeverity(Severity.Verbose)
    startKoin()

    application {
        Window(onCloseRequest = ::exitApplication) {
            MaterialTheme {
                App()
            }
        }
    }
}