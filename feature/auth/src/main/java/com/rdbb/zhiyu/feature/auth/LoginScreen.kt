package com.rdbb.zhiyu.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.feature.auth.R
import com.rdbb.zhiyu.core.ui.components.GradientButton
import com.rdbb.zhiyu.core.ui.components.InputBox
import com.rdbb.zhiyu.core.ui.components.StatusBarIconStyle
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.BrandBrush
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Paper

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    StatusBarIconStyle(lightIcons = true)
    var username by rememberSaveable { mutableStateOf("demo") }
    var password by rememberSaveable { mutableStateOf("demo123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.state.success) {
        if (viewModel.state.success) onLoginSuccess()
    }
    LaunchedEffect(viewModel.state.error) {
        viewModel.state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 顶部氛围图
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.login_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x26201A14),
                                    Color(0x0D201A14),
                                    Color(0x00F7F4EE),
                                    Paper
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(BrandBrush)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("知", color = CardWhite, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(16.dp))
                Text("欢迎来到知遇", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text("记录校园生活，遇见同频的人", fontSize = 13.5.sp, color = Ink2)

                Spacer(Modifier.height(22.dp))
                InputBox(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "请输入账号",
                    leading = Icons.Outlined.Person
                )
                Spacer(Modifier.height(14.dp))
                InputBox(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "请输入密码",
                    leading = Icons.Outlined.Lock,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailing = {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = "显示密码",
                            tint = Ink3,
                            modifier = Modifier
                                .size(19.dp)
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 4.dp, end = 4.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("演示账号已自动填入", fontSize = 12.5.sp, color = Ink2)
                    Text(
                        "新用户注册 →",
                        fontSize = 12.5.sp,
                        color = Brand,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onGoRegister)
                    )
                }

                GradientButton(
                    text = "登 录",
                    onClick = { viewModel.login(username, password) },
                    loading = viewModel.state.loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    listOf("💬", "📱", "🎵").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(CardWhite)
                                .padding(0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 19.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "登录即代表同意 《用户协议》 和 《隐私政策》",
                    fontSize = 11.sp,
                    color = Ink3
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
