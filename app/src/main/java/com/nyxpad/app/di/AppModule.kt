package com.nyxpad.app.di

import com.nyxpad.app.data.repository.NoteRepositoryImpl
import com.nyxpad.app.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindNoteRepository(implementation: NoteRepositoryImpl): NoteRepository
}
