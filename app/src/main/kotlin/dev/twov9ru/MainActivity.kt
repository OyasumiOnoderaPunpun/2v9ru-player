package dev.twov9ru

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.twov9ru.ui.navigation.NavigationLayout
import dev.twov9ru.ui.theme.DynamicPalette
import dev.twov9ru.ui.theme.TwoV9RUTheme
import dev.twov9ru.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: transparent system bars so our floating dock can extend freely
        enableEdgeToEdge(
            statusBarStyle  = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            TwoV9RUApp()
        }
    }
}

@Composable
fun TwoV9RUApp(
    playerViewModel: PlayerViewModel = viewModel()
) {
    val dynamicPalette by playerViewModel.dynamicPalette.collectAsState()

    TwoV9RUTheme(dynamicPalette = dynamicPalette) {
        // WindowSizeClass drives the Phone ↔ Tablet layout metamorphosis
        NavigationLayout(playerViewModel = playerViewModel)
    }
}
