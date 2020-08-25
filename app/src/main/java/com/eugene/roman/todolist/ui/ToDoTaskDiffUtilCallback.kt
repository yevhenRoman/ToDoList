package com.eugene.roman.todolist.ui

import androidx.recyclerview.widget.DiffUtil
import com.eugene.roman.todolist.model.ToDoTask

class ToDoTaskDiffUtilCallback(
    private val oldList: List<ToDoTask>,
    private val newList: List<ToDoTask>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        val isRemoteIdsSame = if (oldItem.remoteId != null && newItem.remoteId != null) {
            oldItem.remoteId == newItem.remoteId
        } else {
            false
        }

        val isLocalIdsSame = if (oldItem.id != null && newItem.id != null) {
            oldItem.id == newItem.id
        } else {
            false
        }

        return isRemoteIdsSame && isLocalIdsSame
    }

}