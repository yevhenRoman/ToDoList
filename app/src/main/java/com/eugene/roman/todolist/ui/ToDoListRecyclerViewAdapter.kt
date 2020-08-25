package com.eugene.roman.todolist.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eugene.roman.todolist.R
import com.eugene.roman.todolist.model.ToDoTask

class ToDoListRecyclerViewAdapter(private val checkedListener: OnToDoCheckedStateChanged) :
    RecyclerView.Adapter<ToDoListRecyclerViewAdapter.ToDoListItemViewHolder>() {

    private var tasksDataList: ArrayList<ToDoTask> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ToDoListItemViewHolder(
            checkedListener,
            layoutInflater.inflate(R.layout.todo_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int = tasksDataList.size

    override fun onBindViewHolder(holder: ToDoListItemViewHolder, position: Int) {
        holder.bind(tasksDataList[position])
    }

    fun setData(data: ArrayList<ToDoTask>) {
        tasksDataList = data
    }

    fun getData() = tasksDataList

    //
    // ViewHolder
    //

    class ToDoListItemViewHolder(
        private val checkedListener: OnToDoCheckedStateChanged,
        private val view: View
    ) :
        RecyclerView.ViewHolder(view) {
        private val taskText: TextView = view.findViewById(R.id.tvTask)
        private val checkBox: CheckBox = view.findViewById(R.id.cbIsCompletedTask)
        private var isCollapsed = false

        fun bind(data: ToDoTask) {
            setBackground(data.isDone)
            styleText(data.isDone)

            taskText.text = data.toDoText
            initCheckedListener(data)

            view.setOnClickListener {
                handleCollapsingAndExpandingTextViewTask()
            }
        }

        private fun initCheckedListener(item: ToDoTask) {
            // Need to avoid calling OnCheckedChangeListener after isChecked state will be set.
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isDone
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                setBackground(isChecked)
                styleText(isChecked)
                checkedListener.onCheckedChanged(item, isChecked)
            }
        }

        private fun setBackground(isDone: Boolean) {
            view.setBackgroundColor(
                if (isDone) getColor(R.color.colorBackgroundTaskIsDone) else getColor(R.color.colorBackgroundTask)
            )
        }

        private fun styleText(isDone: Boolean) {
            if (isDone) {
                taskText.paintFlags = taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                taskText.paintFlags = 0
            }
        }

        private fun handleCollapsingAndExpandingTextViewTask() {
            isCollapsed = if (isCollapsed) {
                expandTextView(taskText)
                false
            } else {
                collapseTextView(taskText)
                true
            }
        }

        private fun expandTextView(tv: TextView) {
            tv.isSingleLine = false
        }

        private fun collapseTextView(tv: TextView) {
            tv.isSingleLine = true
        }

        private fun getColor(resourceId: Int): Int {
            return ContextCompat.getColor(itemView.context, resourceId)
        }
    }

    interface OnToDoCheckedStateChanged {
        fun onCheckedChanged(item: ToDoTask, isChecked: Boolean)
    }
}