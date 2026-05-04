package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.data.local.FinanceDao
import com.example.financeapp.data.local.FinanceDatabase
import com.example.financeapp.network.LLMService
import com.example.financeapp.network.RetrofitLLMService
import com.example.financeapp.network.SuggestionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): FinanceDatabase =
        Room.databaseBuilder(context, FinanceDatabase::class.java, "finance.db").build()

    @Provides
    fun provideDao(db: FinanceDatabase): FinanceDao = db.dao()

    @Provides
    @Singleton
    fun provideSuggestionApi(): SuggestionApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl("https://example-llm.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SuggestionApi::class.java)
    }

    @Provides
    fun provideLlmService(api: SuggestionApi): LLMService = RetrofitLLMService(api)
}
