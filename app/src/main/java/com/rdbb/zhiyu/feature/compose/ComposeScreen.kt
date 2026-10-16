package com.rdbb.zhiyu.feature.compose

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.ui.components.CommunityTile
import com.rdbb.zhiyu.core.ui.components.NetworkImage
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.CommunityGradients
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    onCancel: () -> Unit,
    onPublished: () -> Unit,
    viewModel: ComposeViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.published) { if (state.published) onPublished() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.uploadImage(uri.toImagePart(context))
    }

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CardWhite
        ) {
            Text(
                "选择圈子",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )
            CommunityPickerRow(null, state.selected == null) {
                viewModel.selectCommunity(null); showPicker = false
            }
            state.communities.forEach { c ->
                CommunityPickerRow(c, state.selected?.id == c.id) {
                    viewModel.selectCommunity(c); showPicker = false
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    Scaffold(
        containerColor = Paper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 顶栏
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "取消",
                    fontSize = 14.sp,
                    color = Ink2,
                    modifier = Modifier.clickable(onClick = onCancel)
                )
                Text(
                    "发布动态",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (state.publishing) Ink3 else Brand)
                        .clickable(enabled = !state.publishing) { viewModel.publish(title, content) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (state.publishing) {
                        CircularProgressIndicator(
                            color = CardWhite,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("发布", color = CardWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp)
            ) {
                // 选择圈子
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = true }
                        .padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selected = state.selected
                    if (selected != null) {
                        CommunityTile(selected, size = 26.dp, cornerRadius = 8.dp)
                    } else {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(CommunityGradients[0]))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        selected?.name ?: "选择圈子（可选）",
                        fontSize = 14.sp,
                        color = if (selected != null) Ink else Ink3
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Ink3, modifier = Modifier.size(17.dp))
                }
                Box(Modifier.fillMaxWidth().height(0.8.dp).background(Line))

                BasicTextField(
                    value = title,
                    onValueChange = { if (it.length <= 100) title = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 10.dp),
                    decorationBox = { inner ->
                        if (title.isEmpty()) {
                            Text("好标题能让更多人看到（必填）", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB4AEA1))
                        }
                        inner()
                    }
                )
                Box(Modifier.fillMaxWidth().height(0.8.dp).background(Line))

                BasicTextField(
                    value = content,
                    onValueChange = { if (it.length <= 5000) content = it },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 25.sp, color = Ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 12.dp),
                    decorationBox = { inner ->
                        if (content.isEmpty()) {
                            Text("分享新鲜事…正文最多 5000 字", fontSize = 15.sp, color = Color(0xFFB4AEA1))
                        }
                        inner()
                    }
                )

                // 图片选择（4 列网格）
                val cells: List<String> = state.images +
                    if (state.images.size < 9) listOf(ADD_SLOT) else emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cells.chunked(4).forEach { rowCells ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCells.forEach { cell ->
                                if (cell == ADD_SLOT) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFBFAF6))
                                            .border(1.5.dp, Color(0xFFCFC9BC), RoundedCornerShape(12.dp))
                                            .clickable {
                                                imagePicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.uploading) {
                                            CircularProgressIndicator(
                                                strokeWidth = 2.dp,
                                                color = Ink3,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Outlined.Add,
                                                    contentDescription = "添加图片",
                                                    tint = Color(0xFFC0BAA9),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Text("添加图片", fontSize = 11.sp, color = Color(0xFFABA595))
                                                Text(
                                                    "${state.images.size}/9",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFC0BAA9)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        NetworkImage(
                                            url = cell,
                                            modifier = Modifier.fillMaxSize(),
                                            cornerRadius = 12.dp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(0x80000000))
                                                .clickable { viewModel.removeImage(cell) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Outlined.Close,
                                                contentDescription = "删除",
                                                tint = CardWhite,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(4 - rowCells.size) {
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }

                // 底部工具条
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ToolItem(Icons.Outlined.AlternateEmail, "@好友") {
                        // 仅视觉
                    }
                    ToolItem(Icons.AutoMirrored.Outlined.Article, "#话题") {}
                    ToolItem(Icons.Outlined.Place, "位置") {}
                    ToolItem(Icons.Outlined.Image, "图片") {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityPickerRow(community: Community?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (community != null) CommunityTile(community, size = 40.dp, cornerRadius = 12.dp)
        else Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFEAE0)),
            contentAlignment = Alignment.Center
        ) { Text("无", fontSize = 13.sp, color = Ink3) }
        Spacer(Modifier.width(12.dp))
        Text(
            community?.name ?: "不选择圈子",
            fontSize = 15.sp,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
        if (selected) Icon(Icons.Outlined.Check, contentDescription = null, tint = Brand)
    }
}

@Composable
private fun ToolItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(icon, contentDescription = label, tint = Ink2, modifier = Modifier.size(23.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.5.sp, color = Ink2)
    }
}

private const val ADD_SLOT = "__add_image_slot__"

/** content:// Uri 转 multipart 上传体。 */
internal fun Uri.toImagePart(context: Context): MultipartBody.Part {
    val resolver = context.contentResolver
    val mime = resolver.getType(this) ?: "image/jpeg"
    val requestBody = object : RequestBody() {
        override fun contentType() = mime.toMediaType()

        override fun writeTo(sink: BufferedSink) {
            resolver.openInputStream(this@toImagePart)?.use { input ->
                sink.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }
    val ext = if (mime.contains("png")) "png" else "jpg"
    return MultipartBody.Part.createFormData("file", "upload.$ext", requestBody)
}
