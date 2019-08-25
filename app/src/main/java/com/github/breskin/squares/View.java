package com.github.breskin.squares;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface View {
    void update();
    void render(Canvas canvas);
    boolean onTouchEvent(MotionEvent event);
    boolean onBackPressed();
    void open();
}
