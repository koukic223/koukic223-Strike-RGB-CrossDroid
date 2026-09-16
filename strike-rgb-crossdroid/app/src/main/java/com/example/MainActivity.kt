package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.model.AppThemeMode
import com.example.model.RgbColor
import com.example.ui.screens.MainScreen
import com.example.ui.theme.KoukouRgbTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RgbViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RgbViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val ledState by viewModel.ledState.collectAsState()
            val hardwareStatus by viewModel.hardwareStatus.collectAsState()
            val appSettings by viewModel.appSettings.collectAsState()
            val statusMessage by viewModel.statusMessage.collectAsState()

            val isDark = when (appSettings.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            KoukouRgbTheme(
                darkTheme = isDark,
                dynamicColor = appSettings.dynamicColor,
                ledColor = ledState.currentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        ledState = ledState,
                        hardwareStatus = hardwareStatus,
                        appSettings = appSettings,
                        statusMessage = statusMessage
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
