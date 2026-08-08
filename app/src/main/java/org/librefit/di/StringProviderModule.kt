package org.librefit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.librefit.di.stringProvider.StringProvider
import org.librefit.di.stringProvider.StringProviderImpl

@Module
@InstallIn(SingletonComponent::class)
object StringProviderModule {

    @Provides
    @Singleton
    fun provideStringProvider(impl: StringProviderImpl): StringProvider = impl
}
