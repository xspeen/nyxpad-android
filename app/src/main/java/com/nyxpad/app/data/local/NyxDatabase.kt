package com.nyxpad.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, TemplateEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class NyxDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun templateDao(): TemplateDao
}
