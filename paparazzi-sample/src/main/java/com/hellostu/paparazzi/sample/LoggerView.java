package com.hellostu.paparazzi.sample;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by stuartlynch on 23/05/16.
 */
public class LoggerView extends TextView {

    ///////////////////////////////////////////////////////////////
    // LIFECYCLE
    ///////////////////////////////////////////////////////////////

    public LoggerView(Context context) {
        this(context, null, 0);
    }

    public LoggerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoggerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMovementMethod(new ScrollingMovementMethod());
        setVerticalScrollBarEnabled(true);
    }

    ///////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////

    public void writeLog(String log) {
        Date date = new Date();
        setText(getText() + "[" + date.toString() + "]: " + log + "\n");

        final int scrollAmount = getLayout().getLineTop(getLineCount()) - getHeight();
        if (scrollAmount > 0)
            scrollTo(0, scrollAmount);
        else
            scrollTo(0, 0);
    }

}
