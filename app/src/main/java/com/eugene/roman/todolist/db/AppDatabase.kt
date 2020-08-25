package com.eugene.roman.todolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eugene.roman.todolist.model.ToDoTask

/**
 * The Room database for this app.
 */
@Database(entities = [ToDoTask::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getToDoListDao(): ToDoListDao

    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val database =
                    Room.databaseBuilder(context, AppDatabase::class.java, "TODO_LIST").build()
                instance = database
                return instance as AppDatabase
            }
        }
    }
}