package com.rdbb.zhiyu.data.auth.di

import com.rdbb.zhiyu.data.auth.remote.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AuthApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(
        @Named("mainRetrofit") retrofit: Retrofit,
    ): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}