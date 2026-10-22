package com.rdbb.zhiyu.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rdbb.zhiyu.feature.auth.LoginScreen
import com.rdbb.zhiyu.feature.auth.RegisterScreen
import com.rdbb.zhiyu.feature.community.CommunitiesScreen
import com.rdbb.zhiyu.feature.community.CommunityDetailScreen
import com.rdbb.zhiyu.feature.compose.ComposeScreen
import com.rdbb.zhiyu.feature.home.HomeScreen
import com.rdbb.zhiyu.feature.mine.EditProfileScreen
import com.rdbb.zhiyu.feature.mine.MineScreen
import com.rdbb.zhiyu.feature.notification.NotificationScreen
import com.rdbb.zhiyu.feature.post.PostDetailScreen
import com.rdbb.zhiyu.feature.search.SearchScreen
import com.rdbb.zhiyu.feature.splash.SplashScreen
import com.rdbb.zhiyu.feature.user.FollowListScreen
import com.rdbb.zhiyu.feature.user.UserProfileScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.tabRoutes

    // 进入消息页清零角标，其它页面刷新未读数
    LaunchedEffect(currentRoute) {
        if (currentRoute == Routes.NOTIFICATIONS) {
            mainViewModel.clearUnread()
        } else if (currentRoute in Routes.tabRoutes) {
            mainViewModel.refreshUnread()
        }
    }

    // 401 全局失效：回登录页并清空栈
    LaunchedEffect(Unit) {
        mainViewModel.authExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        // 各页面自行处理状态栏/导航栏 inset，避免重复 padding
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    unreadCount = mainViewModel.unreadCount,
                    onTab = { tab ->
                        val target = when (tab) {
                            TabRoute.HOME -> Routes.HOME
                            TabRoute.COMMUNITY -> Routes.COMMUNITIES
                            TabRoute.NOTIFICATION -> Routes.NOTIFICATIONS
                            TabRoute.MINE -> Routes.MINE
                        }
                        navController.navigate(target) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onPublish = { navController.navigate(Routes.COMPOSE) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onLoggedIn = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onGoRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onGoLogin = { navController.popBackStack() }
                )
            }

            // ---------- 底部 Tab ----------
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenCommunity = { navController.navigate(Routes.communityDetail(it)) },
                    onOpenCommunitiesTab = {
                        navController.navigate(Routes.COMMUNITIES) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.COMMUNITIES) {
                CommunitiesScreen(
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenCommunity = { navController.navigate(Routes.communityDetail(it)) }
                )
            }

            composable(Routes.NOTIFICATIONS) {
                NotificationScreen(
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) }
                )
            }

            composable(Routes.MINE) {
                MineScreen(
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onOpenFollowList = { userId, type ->
                        navController.navigate(Routes.followList(userId, type))
                    },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ---------- 二级页面 ----------
            composable(Routes.COMPOSE) {
                ComposeScreen(
                    onCancel = { navController.popBackStack() },
                    onPublished = { navController.popBackStack() }
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) },
                    onOpenCommunity = { navController.navigate(Routes.communityDetail(it)) }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.POST_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_POST_ID) { type = NavType.IntType })
            ) {
                PostDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) }
                )
            }

            composable(
                route = Routes.USER_PROFILE,
                arguments = listOf(navArgument(Routes.ARG_USER_ID) { type = NavType.IntType })
            ) {
                UserProfileScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onOpenFollowList = { userId, type ->
                        navController.navigate(Routes.followList(userId, type))
                    }
                )
            }

            composable(
                route = Routes.COMMUNITY_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_COMMUNITY_ID) { type = NavType.IntType })
            ) {
                CommunityDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPost = { navController.navigate(Routes.postDetail(it)) },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) }
                )
            }

            composable(
                route = Routes.FOLLOW_LIST,
                arguments = listOf(
                    navArgument(Routes.ARG_USER_ID) { type = NavType.IntType },
                    navArgument(Routes.ARG_FOLLOW_TYPE) { type = NavType.StringType }
                )
            ) {
                FollowListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenUser = { navController.navigate(Routes.userProfile(it)) }
                )
            }
        }
    }
}
