package com.rdbb.zhiyu.feature.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardBackspace
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.ui.components.GradientButton
import com.rdbb.zhiyu.core.ui.components.InputBox
import com.rdbb.zhiyu.core.ui.components.StatusBarIconStyle
import com.rdbb.zhiyu.core.ui.theme.AvatarGradients
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.ui.theme.SuccessGreen

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    onGoLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    StatusBarIconStyle(lightIcons = false)
    var username by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.state.success) {
        if (viewModel.state.success) onRegisterSuccess()
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
        ) {
            // 顶栏
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.KeyboardBackspace,
                        contentDescription = "返回",
                        tint = Ink
                    )
                }
                Text(
                    "创建账号",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.weight(1f).padding(end = 38.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
            ) {
                // 头像占位
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp, bottom = 26.dp)
                        .size(84.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.linearGradient(AvatarGradients[3])),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nickname.trim().take(1).ifEmpty { "新" },
                            color = CardWhite,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brand)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = CardWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                FieldLabel("用户名")
                InputBox(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "3-20位字母、数字或下划线",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
                FieldHint("登录账号，注册后不可修改")

                Spacer(Modifier.height(16.dp))
                FieldLabel("昵称")
                InputBox(
                    value = nickname,
                    onValueChange = { if (it.length <= 20) nickname = it },
                    placeholder = "大家会怎么称呼你？"
                )

                Spacer(Modifier.height(16.dp))
                FieldLabel("密码")
                InputBox(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "6-20位，建议字母+数字",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                FieldHint("密码长度 6-20 位", color = Ink3)

                Spacer(Modifier.height(22.dp))
                GradientButton(
                    text = "注 册",
                    onClick = { viewModel.register(username, password, nickname) },
                    loading = viewModel.state.loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("已有账号？", fontSize = 13.sp, color = Ink2)
                    Text(
                        "直接登录",
                        fontSize = 13.sp,
                        color = Brand,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onGoLogin)
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Ink2,
        modifier = Modifier.padding(bottom = 7.dp)
    )
}

@Composable
private fun FieldHint(text: String, color: androidx.compose.ui.graphics.Color = Ink3) {
    Text(
        text,
        fontSize = 11.5.sp,
        color = color,
        modifier = Modifier.padding(top = 6.dp)
    )
}
