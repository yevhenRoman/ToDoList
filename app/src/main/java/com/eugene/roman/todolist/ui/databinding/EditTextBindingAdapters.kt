package com.eugene.roman.todolist.ui.databinding

import android.text.TextWatcher
import android.widget.EditText
import androidx.databinding.BindingAdapter

class EditTextBindingAdapters {
    companion object {
        @JvmStatic
        @BindingAdapter("textChangedListener")
        fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher?) {
            editText.addTextChangedListener(textWatcher)
        }
    }
}