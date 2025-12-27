package com.smartcommute.core.di

import com.smartcommute.BuildConfig
import com.smartcommute.feature.linestatus.data.remote.TflApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            val maxRetries = 3

            while (!response.isSuccessful && tryCount < maxRetries) {
                if (response.code in listOf(429, 500, 502, 503, 504)) {
                    tryCount++
                    val delayMillis = (2000L * (1 shl tryCount)).coerceAtMost(16000L)
                    Thread.sleep(delayMillis)
                    response.close()
                    response = chain.proceed(request)
                } else {
                    break
                }
            }
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(retryInterceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tfl.gov.uk/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTflApiService(retrofit: Retrofit): TflApiService {
        return retrofit.create(TflApiService::class.java)
    }
}
