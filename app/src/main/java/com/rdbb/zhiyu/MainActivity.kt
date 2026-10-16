package com.rdbb.zhiyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rdbb.zhiyu.core.ui.components.AppDensityProvider
import com.rdbb.zhiyu.core.ui.components.StatusBarIconStyle
import com.rdbb.zhiyu.core.ui.theme.ZhiYuTheme
import com.rdbb.zhiyu.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppDensityProvider {
                ZhiYuTheme {
                    StatusBarIconStyle(lightIcons = false)
                    AppNavHost()
                }
            }
        }
    }
}
