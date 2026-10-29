package com.nyxpad.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideNoteDao(database: NyxDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideTemplateDao(database: NyxDatabase): TemplateDao = database.templateDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE notes ADD COLUMN tags TEXT NOT NULL DEFAULT ''",
            )
        }
    }
}
