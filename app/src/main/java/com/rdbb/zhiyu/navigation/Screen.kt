package com.rdbb.zhiyu.navigation

sealed class Screen (
    val route: String
){
    data object Splash : Screen("splash")
}