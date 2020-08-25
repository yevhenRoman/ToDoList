package com.eugene.roman.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eugene.roman.todolist.repository.ToDoListRepository

class ToDoListViewModelFactory(
    private val repository: ToDoListRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ToDoListViewModel(repository) as T
    }
}