import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.beforeyoudie.common.App
import com.beforeyoudie.common.di.startKoin

fun main() {
    startKoin()

    application {
        Window(onCloseRequest = ::exitApplication) {
            MaterialTheme {
                App()
            }
        }
    }
}