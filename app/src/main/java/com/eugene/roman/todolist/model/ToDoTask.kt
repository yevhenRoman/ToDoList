package com.eugene.roman.todolist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ToDoListTable")
data class ToDoTask(
    @SerializedName("userId")
    val userId: Long? = null,

    @SerializedName("id")
    val remoteId: Long? = null,

    @ColumnInfo(name = "toDoText")
    @SerializedName("title")
    val toDoText: String,

    @ColumnInfo(name = "isDone")
    @SerializedName("completed")
    var isDone: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
) {
    fun isCloudTask() = userId != null && remoteId != null
}