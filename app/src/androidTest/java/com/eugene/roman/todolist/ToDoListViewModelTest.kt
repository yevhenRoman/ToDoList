package com.eugene.roman.todolist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.eugene.roman.todolist.api.ToDoRecordsApi
import com.eugene.roman.todolist.db.AppDatabase
import com.eugene.roman.todolist.repository.ToDoListRepository
import com.eugene.roman.todolist.ui.ToDoListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

class ToDoListViewModelTest {
    private lateinit var appDatabase: AppDatabase
    private lateinit var viewModel: ToDoListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        val repository = ToDoListRepository(
            appDatabase.getToDoListDao(),
            ToDoRecordsApi.create(),
            CoroutineScope(Dispatchers.IO)
        )
        viewModel = ToDoListViewModel(repository, CoroutineScope(Dispatchers.IO))
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Test
    @Throws(InterruptedException::class)
    fun testAddAndReturnLocalValues() {
        viewModel.addNote("1")
        viewModel.addNote("2")
        viewModel.addNote("3")

        sleep(1000)
        val tasks = getValue(viewModel.getToDoRecords())
        assertEquals(3, tasks.filter { !it.isCloudTask() }.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testDeleteLocalValues() {
        viewModel.addNote("1")
        viewModel.addNote("2")
        viewModel.addNote("3")

        sleep(1000)
        viewModel.deleteTask(getValue(viewModel.getToDoRecords()).first())
        sleep(500)
        val tasks = getValue(viewModel.getToDoRecords())
        assertEquals(2, tasks.filter { !it.isCloudTask() }.size)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testUpdateValue() {
        viewModel.addNote("1")

        sleep(1000)
        val task = getValue(viewModel.getToDoRecords()).first()
        val taskIsDone = task.isDone

        viewModel.updateNote(task, !taskIsDone)
        sleep(500)

        assert(getValue(viewModel.getToDoRecords()).first().isDone != taskIsDone)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testIfItIsPossibleToAddEmptyNote() {
        viewModel.addNote("")

        sleep(1000)
        val task = getValue(viewModel.getToDoRecords()).firstOrNull()

        assert(task == null)
    }
}