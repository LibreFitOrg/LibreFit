package org.librefit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.librefit.di.streamProvider.StreamProvider
import org.librefit.di.streamProvider.StreamProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreamProviderModule {
    @Provides
    @Singleton
    fun provideInputStreamProvider(impl: StreamProviderImpl): StreamProvider = impl
}
