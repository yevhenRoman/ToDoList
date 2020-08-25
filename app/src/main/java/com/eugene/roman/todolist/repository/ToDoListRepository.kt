package com.eugene.roman.todolist.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.eugene.roman.todolist.common.ToDoListError
import com.eugene.roman.todolist.db.ToDoListDao
import com.eugene.roman.todolist.model.ToDoTask
import com.eugene.roman.todolist.api.ToDoRecordsApi
import com.eugene.roman.todolist.common.Event
import com.eugene.roman.todolist.common.combineWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ToDoListRepository(
    private val database: ToDoListDao,
    private val api: ToDoRecordsApi,
    private val coroutineScope: CoroutineScope
) {
    private val _error = MutableLiveData<Event<ToDoListError>>()
    val error: LiveData<Event<ToDoListError>> = _error

    private val cloudTasksLiveData = MutableLiveData<List<ToDoTask>>()
    private val localTasksLiveData = database.getTasks()
    private var networkChecker: NetworkChecker? = null

    fun setNetworkChecker(networkChecker: NetworkChecker?) {
        this.networkChecker = networkChecker
    }

    suspend fun addNewTask(record: ToDoTask) {
        // In future here can be call to API
        database.insertTask(record)
    }

    suspend fun updateTask(task: ToDoTask) {
        if (!task.isCloudTask()) {
            database.updateTask(task)
        } else {
            val newCloudTasksList = cloudTasksLiveData.value?.toMutableList()
            val indexOfUpdatingElement =
                newCloudTasksList?.indexOfFirst { it.remoteId == task.remoteId }
            if (indexOfUpdatingElement != null) {
                newCloudTasksList.removeAt(indexOfUpdatingElement)
                newCloudTasksList.add(indexOfUpdatingElement, task)
                cloudTasksLiveData.postValue(newCloudTasksList)
            } else {
                Log.w(TAG, "Unable to update cloud task locally.")
            }
        }
    }

    suspend fun deleteTask(task: ToDoTask) {
        if (!task.isCloudTask()) {
            database.deleteTask(task)
        } else {
            val newCloudTasksList = cloudTasksLiveData.value?.toMutableList()
            newCloudTasksList?.remove(task)
            cloudTasksLiveData.postValue(newCloudTasksList)
        }
    }

    fun getTasks(): LiveData<ArrayList<ToDoTask>> {
        return localTasksLiveData.combineWith(cloudTasksLiveData) { database, remote ->
            val arrayList = arrayListOf<ToDoTask>()

            database?.let { arrayList.addAll(it.reversed()) }
            remote?.let { arrayList.addAll(it) }

            arrayList
        }
    }

    fun updateCloudData() {
        loadTodoTasksIfPossible()
    }

    private fun loadTodoTasksIfPossible() {
        val isNetworkAvailable = networkChecker?.checkIfNetworkAccessible() ?: false

        if (!isNetworkAvailable) {
            Log.w(TAG, "No Internet connection.")
        } else {
            coroutineScope.launch {
                val cloudTasksList = getCloudTaskList()

                if (cloudTasksList == null) {
                    _error.postValue(Event(ToDoListError.FAILED_TO_LOAD_CLOUD_TASKS))
                    Log.w(TAG, "Failed to load data.")
                    return@launch
                }

                // Convert cloud modal object to local model
                val convertedCloudTasks = convertRemoteModelToLocal(cloudTasksList)

                // Post loaded devices
                cloudTasksLiveData.postValue(convertedCloudTasks)
            }
        }
    }

    private suspend fun getCloudTaskList(): List<ToDoRecordsApi.ToDoCloudTask>? {
        return try {
            api.getToDoRecords()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return null
        }
    }

    private fun convertRemoteModelToLocal(list: List<ToDoRecordsApi.ToDoCloudTask>): ArrayList<ToDoTask> {
        val convertedCloudTasks = ArrayList<ToDoTask>()

        list.forEach {
            convertedCloudTasks.add(
                ToDoTask(
                    remoteId = it.id,
                    toDoText = it.title,
                    isDone = it.isDone,
                    userId = it.userId
                )
            )
        }

        return convertedCloudTasks
    }

    interface NetworkChecker {
        /**
         * Must @return true if accessible, otherwise false.
         */
        fun checkIfNetworkAccessible(): Boolean
    }

    companion object {
        const val TAG = "ToDoListRepository"
    }
}