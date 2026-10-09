package com.example.lywsd02bledashboard

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.lywsd02bledashboard.theme.LYWSD02BLEDashboardTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("lywsd02_preferences", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_language", "system") ?: "system"
        val targetLocale = when (savedLang) {
            "ko" -> Locale.KOREAN
            "en" -> Locale.ENGLISH
            else -> null // 시스템 기본 로케일 사용 (비한국어는 values/strings.xml 영어로 자동 폴백)
        }

        val context = if (targetLocale != null) {
            Locale.setDefault(targetLocale)
            val config = Configuration(newBase.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(targetLocale)
                newBase.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                config.locale = targetLocale
                @Suppress("DEPRECATION")
                newBase.resources.updateConfiguration(config, newBase.resources.displayMetrics)
                newBase
            }
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LYWSD02BLEDashboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}
