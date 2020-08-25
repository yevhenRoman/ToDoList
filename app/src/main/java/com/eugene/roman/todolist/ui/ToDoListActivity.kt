package com.eugene.roman.todolist.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.eugene.roman.todolist.*
import com.eugene.roman.todolist.common.ConnectionLiveData
import com.eugene.roman.todolist.databinding.ActivityTodoListBinding
import com.eugene.roman.todolist.model.ToDoTask
import com.eugene.roman.todolist.api.ToDoRecordsApi
import com.eugene.roman.todolist.common.ToDoListError
import com.eugene.roman.todolist.db.AppDatabase
import com.eugene.roman.todolist.repository.ToDoListRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ToDoListActivity : AppCompatActivity(), LifecycleOwner,
    ToDoListRecyclerViewAdapter.OnToDoCheckedStateChanged {

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {/*Do nothing.*/ }

        override fun afterTextChanged(s: Editable) { /*Do nothing.*/
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.handleAllowanceToSaveTask(s.toString())
        }
    }

    private lateinit var binding: ActivityTodoListBinding
    private lateinit var todoListAdapter: ToDoListRecyclerViewAdapter
    private lateinit var viewModel: ToDoListViewModel
    private lateinit var networkAvailability: ConnectionLiveData

    // For avoiding annoying messages on the same actions
    private var isNoInternetMessageWasShown = false
    private var cloudTasksCantBeEditedMessageShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkAvailability = ConnectionLiveData(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_todo_list)
        binding.lifecycleOwner = this
        setupRecyclerView()
        initViewModel()
        binding.setVariable(BR.viewModel, viewModel)
        binding.setVariable(BR.activity, this)
        observeVariables()
    }

    override fun onCheckedChanged(item: ToDoTask, isChecked: Boolean) {
        if (item.isCloudTask() && !cloudTasksCantBeEditedMessageShown) {
            showSnackBar(getString(R.string.Cloud_tasks_can_be_edited_only_locally_and_during))
            cloudTasksCantBeEditedMessageShown = true
        }
        viewModel.updateNote(item, isChecked)
    }

    private fun initViewModel() {
        val factory = ToDoListViewModelFactory(
            ToDoListRepository(
                AppDatabase.getInstance(this).getToDoListDao(),
                ToDoRecordsApi.create(),
                CoroutineScope(Dispatchers.IO)
            )
        )

        viewModel = ViewModelProvider(this, factory).get(ToDoListViewModel::class.java)
    }

    private fun observeVariables() {

        networkAvailability.observe(this, Observer {
            handleNoInternetConnection(it)
        })

        viewModel.getToDoRecords().observe(this, Observer {
            handleNewTasksList(it)
        })

        viewModel.getErrors().observe(this, Observer {
            it.getContentIfNotHandled()?.let { error ->
                handleError(error)
            }
        })
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = binding.rvTodoList
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        todoListAdapter = ToDoListRecyclerViewAdapter(this)
        recyclerView.adapter = todoListAdapter

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val itemToRemove = todoListAdapter.getData()[position]
                viewModel.deleteTask(itemToRemove)
                todoListAdapter.getData().removeAt(position)
                todoListAdapter.notifyItemRemoved(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun handleNewTasksList(newList: ArrayList<ToDoTask>) {
        val diffUtilCallback = ToDoTaskDiffUtilCallback(todoListAdapter.getData(), newList)
        val productDiffResult = DiffUtil.calculateDiff(diffUtilCallback)
        todoListAdapter.setData(newList)
        todoListAdapter.notifyDataSetChanged()
        productDiffResult.dispatchUpdatesTo(todoListAdapter)
    }

    private fun handleNoInternetConnection(isNetworkAvailable: Boolean) {
        viewModel.setInternetState(isNetworkAvailable)

        if (isNetworkAvailable) {
            viewModel.refreshCloudNotes()
        } else if (!isNoInternetMessageWasShown && !isNetworkAvailable) {
            isNoInternetMessageWasShown = true
            showSnackBar(getString(R.string.No_Internet_Some_tasks_may_))
        }
    }

    private fun handleError(error: ToDoListError) = when (error) {
        ToDoListError.FAILED_TO_LOAD_CLOUD_TASKS -> showToastMessage(getString(R.string.Failed_to_load_data_from))
    }

    private fun showToastMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    private fun showSnackBar(text: String) {
        val snackbar = Snackbar.make(binding.root, text, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction(getString(android.R.string.ok)) {
            snackbar.dismiss()
        }

        snackbar.show()
    }
}
