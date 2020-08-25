package com.eugene.roman.todolist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eugene.roman.todolist.common.getViewModelScope
import com.eugene.roman.todolist.model.ToDoTask
import com.eugene.roman.todolist.repository.ToDoListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ToDoListViewModel(
    private val repository: ToDoListRepository,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {

    private var networkAvailability = false
    private val _isNewToDoRecordCanBeSaved = MutableLiveData<Boolean>(false)
    val isNewToDoRecordCanBeSaved: LiveData<Boolean> = _isNewToDoRecordCanBeSaved

    init {
        repository.setNetworkChecker(networkChecker = object : ToDoListRepository.NetworkChecker {
            override fun checkIfNetworkAccessible(): Boolean = networkAvailability
        })
    }

    private val coroutineScope = getViewModelScope(coroutineScopeProvider)

    fun getToDoRecords() = repository.getTasks()

    fun getErrors() = repository.error

    fun setInternetState(isInternetAvailable: Boolean) {
        networkAvailability = isInternetAvailable
    }

    // Can be also used from Pull-to-Refresh in future
    fun refreshCloudNotes() {
        repository.updateCloudData()
    }

    fun addNote(newTask: String) {
        // TODO: For future: check if job is active.
        if (isPotentialNewTaskValid(newTask))
        coroutineScope.launch {
            repository.addNewTask(ToDoTask(toDoText = newTask))
        }
    }

    fun updateNote(toDoTask: ToDoTask, isChecked: Boolean) {
        toDoTask.isDone = isChecked
        // TODO: For future: check if job is active.
        coroutineScope.launch {
            repository.updateTask(toDoTask)
        }
    }

    fun handleAllowanceToSaveTask(task: String) {
        _isNewToDoRecordCanBeSaved.postValue(isPotentialNewTaskValid(task))
    }

    fun deleteTask(taskToRemove: ToDoTask) {
        coroutineScope.launch {
            repository.deleteTask(taskToRemove)
        }
    }

    private fun isPotentialNewTaskValid(task: String): Boolean {
        return task.isNotEmpty()
    }
}