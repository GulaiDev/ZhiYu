package com.rdbb.zhiyu.feature.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.CommunityRepository
import com.rdbb.zhiyu.core.model.Community
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunitiesState(
    val communities: List<Community> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CommunitiesViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    var state by mutableStateOf(CommunitiesState())
        private set

    init { refresh() }

    fun refresh() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            communityRepository.list()
                .onSuccess { state = state.copy(loading = false, communities = it) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun toggleJoin(community: Community) {
        val join = !community.isJoined
        update(community.id) { it.copy(isJoined = join, memberCount = it.memberCount + if (join) 1 else -1) }
        viewModelScope.launch {
            communityRepository.join(community.id, join)
                .onSuccess { fresh -> update(community.id) { fresh } }
        }
    }

    private fun update(id: Int, transform: (Community) -> Community) {
        state = state.copy(
            communities = state.communities.map { if (it.id == id) transform(it) else it }
        )
    }
}
