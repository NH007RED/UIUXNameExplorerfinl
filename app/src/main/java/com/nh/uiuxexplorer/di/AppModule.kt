package com.nh.uiuxexplorer.di

import android.content.Context
import androidx.room.Room
import com.nh.uiuxexplorer.data.local.AiResponseDao
import com.nh.uiuxexplorer.data.local.AppDatabase
import com.nh.uiuxexplorer.data.local.FavoriteDao
import com.nh.uiuxexplorer.data.local.RecentViewDao
import com.nh.uiuxexplorer.data.local.SavedScreenDao
import com.nh.uiuxexplorer.data.local.SearchHistoryDao
import com.nh.uiuxexplorer.data.remote.AiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "uiux_explorer.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideRecentDao(db: AppDatabase): RecentViewDao = db.recentViewDao()
    @Provides fun provideSearchDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun provideAiDao(db: AppDatabase): AiResponseDao = db.aiResponseDao()
    @Provides fun provideSavedScreenDao(db: AppDatabase): SavedScreenDao = db.savedScreenDao()

    // No logging interceptor on purpose: request headers contain the user's API key.
    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder().baseUrl("https://localhost/").client(client).build()

    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi = retrofit.create(AiApi::class.java)
}
