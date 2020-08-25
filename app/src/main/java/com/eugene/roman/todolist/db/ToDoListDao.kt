package com.eugene.roman.todolist.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.eugene.roman.todolist.model.ToDoTask

@Dao
interface ToDoListDao {
    @Insert
    suspend fun insertTask(record: ToDoTask)

    @Query("SELECT * FROM ToDoListTable")
    fun getTasks(): LiveData<List<ToDoTask>>

    @Update
    suspend fun updateTask(task: ToDoTask)

    @Delete
    suspend fun deleteTask(task: ToDoTask)
}