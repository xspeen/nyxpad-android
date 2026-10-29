package com.nyxpad.app.di

import android.content.Context
import androidx.room.Room
import com.nyxpad.app.data.local.NoteDao
import com.nyxpad.app.data.local.NyxDatabase
import com.nyxpad.app.data.local.TemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NyxDatabase =
        Room.databaseBuilder(context, NyxDatabase::class.java, "nyxpad.db")
            .build()

    @Provides
    fun provideNoteDao(database: NyxDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideTemplateDao(database: NyxDatabase): TemplateDao = database.templateDao()
}
