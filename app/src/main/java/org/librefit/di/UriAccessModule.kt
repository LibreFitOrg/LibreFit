package org.librefit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.librefit.di.uriAccess.UriAccess
import org.librefit.di.uriAccess.UriAccessImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UriAccessModule {
    @Provides
    @Singleton
    fun provideUriAccess(impl: UriAccessImpl): UriAccess = impl
}
