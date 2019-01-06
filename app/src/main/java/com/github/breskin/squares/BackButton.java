package com.github.breskin.squares;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.github.breskin.squares.Logo;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;
import com.github.breskin.squares.game.Overlord;
import com.github.breskin.squares.game.ScoreboardButton;

/**
 * Created by Kuba on 03.12.2018.
 */

public class BackButton {
    private enum AnimationType { None, Show, Hide }

    private AnimationType CurrentAnimation = AnimationType.None;
    private Scoreboard mScoreboard;
    private Bitmap icon;
    private Paint mPaint;

    private PointF size, position;

    private int Alpha = 0, progress, maxProgress = 500;
    private float margin = 0;
    private long time = 0;
    private boolean touchDown = false, showAnimation = false;

    public BackButton(Scoreboard o) {
        mScoreboard = o;
        size = new PointF(0, 0);
        position = new PointF(0, 0);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    public void update() {
        size.y = Logo.Size.y * 0.5f;
        size.x = icon.getWidth() * size.y / icon.getHeight();
        position.x = MainView.ViewWidth - size.x - Logo.Size.y * 5 / 8 + size.y;
        position.y = MainView.ViewHeight - Logo.Size.y * 9 / 16 + Logo.Size.y / 2 - size.y;

        if (touchDown)
            progress += System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        if (progress > maxProgress) {
            mScoreboard.hide();

            progress = 0;
            touchDown = false;
        }

        if (showAnimation) {
            setShow();
            showAnimation = false;
        }

        updateAnimations();
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Color.argb(Alpha, 255, 255, 255));

        canvas.drawBitmap(icon, new Rect(0, 0, icon.getWidth(), icon.getHeight()), new RectF(position.x, position.y + margin, position.x + size.x, position.y + size.y + margin), null);

        mPaint.setStrokeWidth(MainView.ViewWidth / 100);
        canvas.drawArc(MainView.ViewWidth - Logo.Size.y, MainView.ViewHeight - Logo.Size.y, MainView.ViewWidth + Logo.Size.y, MainView.ViewHeight + Logo.Size.y, 270, -90f * progress / maxProgress, false, mPaint);
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (CurrentAnimation == AnimationType.None && x > MainView.ViewWidth - size.x * 2 &&  y > MainView.ViewHeight - Logo.Size.y) {
                    touchDown = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
                touchDown = false;
                progress = 0;
                break;
        }
    }

    public void show() {
        if (Alpha == 0) {
            showAnimation = true;
        }
    }

    private void setShow() {
        margin = MainView.ViewHeight * 0.1f;
        CurrentAnimation = AnimationType.Show;
    }

    public void hide() {
        CurrentAnimation = AnimationType.Hide;
    }

    void updateAnimations() {
        switch (CurrentAnimation) {
            case Hide:
                margin += (Logo.Size.y - margin) * 0.1f;
                Alpha -= 20;

                if (Alpha < 0) {
                    Alpha = 0;
                    CurrentAnimation = AnimationType.None;
                }
                break;

            case Show:
                margin -= (margin + 5) * 0.15f;
                Alpha += 15;

                if (Alpha > 255)
                    Alpha = 255;

                if (margin < 1) {
                    margin = 0;
                    Alpha = 255;
                    CurrentAnimation = AnimationType.None;
                }
                break;
        }
    }

    public void load(Context context) {
        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.back);
    }
}
