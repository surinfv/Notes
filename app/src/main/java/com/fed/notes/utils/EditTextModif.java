package com.fed.notes.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by f on 22.06.2017.
 */

public class EditTextModif extends android.support.v7.widget.AppCompatEditText {
    public EditTextModif(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {

//            ViewUtil.hideKeyboard(this);
                clearFocus();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
