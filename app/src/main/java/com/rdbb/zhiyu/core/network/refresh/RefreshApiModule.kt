package com.rdbb.zhiyu.core.network.refresh

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RefreshApiModule {

    @Provides
    @Singleton
    fun provideRefreshApi(
        @Named("refreshRetrofit") retrofit: Retrofit,
    ): RefreshApi {
        return retrofit.create(RefreshApi::class.java)
    }
}