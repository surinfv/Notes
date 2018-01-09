package com.fed.notes.utils

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent

// Очень плохое название :) Лучше называть попонятнее, особенно классы
class EditTextModif(context: Context, attrs: AttributeSet) : android.support.v7.widget.AppCompatEditText(context, attrs) {

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
//            ViewUtil.hideKeyboard(this)
            clearFocus()
            return false
        }
        return super.dispatchKeyEvent(event)
    }
}
