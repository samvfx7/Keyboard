package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LearnedWordEntity::class,
        LearnedPhraseEntity::class,
        PersonalCorrectionEntity::class,
        ClipboardEntity::class,
        SnippetEntity::class,
        TouchOffsetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AetherDatabase : RoomDatabase() {

    abstract fun aetherDao(): AetherDao

    companion object {
        @Volatile
        private var INSTANCE: AetherDatabase? = null

        fun getInstance(context: Context): AetherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AetherDatabase::class.java,
                    "aether_privacy_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
