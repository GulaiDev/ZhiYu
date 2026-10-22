package com.rdbb.zhiyu.feature.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.CommunityRepository
import com.rdbb.zhiyu.core.data.repository.PostRepository
import com.rdbb.zhiyu.core.model.Community
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class ComposeState(
    val communities: List<Community> = emptyList(),
    val selected: Community? = null,
    val images: List<String> = emptyList(),
    val uploading: Boolean = false,
    val publishing: Boolean = false,
    val published: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    var state by mutableStateOf(ComposeState())
        private set

    init {
        viewModelScope.launch {
            communityRepository.list().onSuccess { state = state.copy(communities = it) }
        }
    }

    fun selectCommunity(community: Community?) {
        state = state.copy(selected = community)
    }

    fun uploadImage(part: MultipartBody.Part) {
        if (state.images.size >= 9 || state.uploading) return
        state = state.copy(uploading = true)
        viewModelScope.launch {
            postRepository.uploadImage(part)
                .onSuccess { state = state.copy(uploading = false, images = state.images + it.url) }
                .onFailure { state = state.copy(uploading = false, error = it.message) }
        }
    }

    fun removeImage(url: String) {
        state = state.copy(images = state.images - url)
    }

    fun publish(title: String, content: String) {
        when {
            title.isBlank() -> state = state.copy(error = "请填写标题")
            content.isBlank() -> state = state.copy(error = "请填写正文")
            title.length > 100 -> state = state.copy(error = "标题不能超过 100 字")
            content.length > 5000 -> state = state.copy(error = "正文不能超过 5000 字")
            else -> {
                state = state.copy(publishing = true, error = null)
                viewModelScope.launch {
                    postRepository.create(title, content, state.images, state.selected?.id)
                        .onSuccess { state = state.copy(publishing = false, published = true) }
                        .onFailure { state = state.copy(publishing = false, error = it.message) }
                }
            }
        }
    }

    fun consumeError() {
        state = state.copy(error = null)
    }
}
