package com.rdbb.zhiyu.feature.splash

import androidx.lifecycle.ViewModel
import com.rdbb.zhiyu.core.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val session: SessionManager
) : ViewModel()
