package com.rdbb.zhiyu.feature.mine

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.ui.components.GradientButton
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.NetworkImage
import com.rdbb.zhiyu.core.ui.theme.AvatarGradients
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.firstChar
import com.rdbb.zhiyu.core.ui.util.toImagePart

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }
    LaunchedEffect(state.error) {
        if (state.error != null) {
            android.widget.Toast.makeText(context, state.error, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.consumeError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(uri.toImagePart(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(Modifier.weight(1f))
            Text("编辑资料", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.weight(1f))
            Text(
                "保存",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !state.saving) { viewModel.save() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        if (state.loading) {
            LoadingBox()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.linearGradient(AvatarGradients.first())),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.avatar.isNullOrBlank()) {
                        NetworkImage(
                            url = state.avatar,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 43.dp
                        )
                    } else {
                        Text(
                            firstChar(state.nickname),
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (state.uploading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0x66000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brand)
                        .clickable {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = "更换头像",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("点击更换头像", fontSize = 12.sp, color = Ink3)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardWhite)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ProfileField("昵称", state.nickname, "给自己起个昵称", singleLine = true) {
                viewModel.onNickname(it)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Line)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("性别", fontSize = 14.sp, color = Ink2, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                GenderSegment(state.gender) { viewModel.onGender(it) }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Line)
            )
            ProfileField("简介", state.bio, "介绍一下自己吧～", singleLine = false) {
                viewModel.onBio(it)
            }
        }

        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            GradientButton(
                text = if (state.saving) "保存中…" else "保存修改",
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.saving
            )
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    placeholder: String,
    singleLine: Boolean,
    onChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (singleLine) 6.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Ink2,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(52.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Ink3) },
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        )
    }
}

@Composable
private fun GenderSegment(gender: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0 to "保密", 1 to "男", 2 to "女").forEach { (value, label) ->
            val selected = gender == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (selected) Brand else Color(0xFFF6F3EC))
                    .then(
                        if (!selected) Modifier.border(1.dp, Line, RoundedCornerShape(15.dp))
                        else Modifier
                    )
                    .clickable { onChange(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    label,
                    fontSize = 12.5.sp,
                    color = if (selected) Color.White else Ink2,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
