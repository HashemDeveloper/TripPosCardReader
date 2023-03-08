package com.limosys.test.tripostestapp.di

import android.content.Context
import com.limosys.test.tripostestapp.TriposTestApp
import com.limosys.test.tripostestapp.repo.ISharedPref
import com.limosys.test.tripostestapp.repo.SharedPrefServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideShardPref(@ApplicationContext context: Context): ISharedPref {
        return SharedPrefServiceImpl(context)
    }
}