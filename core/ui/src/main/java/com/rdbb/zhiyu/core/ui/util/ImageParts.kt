package com.rdbb.zhiyu.core.ui.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

/** content:// Uri 转 multipart 上传体。 */
fun Uri.toImagePart(context: Context): MultipartBody.Part {
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
