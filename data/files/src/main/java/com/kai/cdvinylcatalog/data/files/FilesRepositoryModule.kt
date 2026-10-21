package com.kai.cdvinylcatalog.data.files

import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FilesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotoStorage(impl: AppPhotoStorageImpl): PhotoStorage
}
