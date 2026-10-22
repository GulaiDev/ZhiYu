package com.rdbb.zhiyu.navigation

/** 全应用路由常量。 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // 底部 Tab 四个一级页
    const val HOME = "home"
    const val COMMUNITIES = "communities"
    const val NOTIFICATIONS = "notifications"
    const val MINE = "mine"

    const val COMPOSE = "compose"
    const val SEARCH = "search"
    const val EDIT_PROFILE = "edit_profile"

    const val POST_DETAIL = "post/{postId}"
    const val USER_PROFILE = "user/{userId}"
    const val COMMUNITY_DETAIL = "community/{communityId}"
    const val FOLLOW_LIST = "user/{userId}/follow/{followType}"

    const val ARG_POST_ID = "postId"
    const val ARG_USER_ID = "userId"
    const val ARG_COMMUNITY_ID = "communityId"
    const val ARG_FOLLOW_TYPE = "followType"

    const val FOLLOW_TYPE_FOLLOWERS = "followers"
    const val FOLLOW_TYPE_FOLLOWING = "following"

    /** SavedStateHandle 读取参数时的别名。 */
    object Args {
        const val POST_ID = ARG_POST_ID
        const val USER_ID = ARG_USER_ID
        const val COMMUNITY_ID = ARG_COMMUNITY_ID
        const val FOLLOW_TYPE = ARG_FOLLOW_TYPE
    }

    fun postDetail(id: Int) = "post/$id"
    fun userProfile(id: Int) = "user/$id"
    fun communityDetail(id: Int) = "community/$id"
    fun followList(userId: Int, type: String) = "user/$userId/follow/$type"

    val tabRoutes = listOf(HOME, COMMUNITIES, NOTIFICATIONS, MINE)
}
