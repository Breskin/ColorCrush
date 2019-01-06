package com.github.breskin.squares.game.context;

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

import com.github.breskin.squares.game.Board;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;

/**
 * Created by Kuba on 13.11.2018.
 */

public class EndGameButton {

    private Results mResults;

    private Bitmap cross;
    private PointF position;
    private Paint mPaint;

    private boolean touchDown = false;
    private float size;
    private int progress, maxProgress = 1000;
    private long time;

    public EndGameButton(Results r) {
        mResults = r;

        position = new PointF(0, 0);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
    }

    public void update() {
        size = MainView.ViewWidth * 0.07f;

        if (touchDown)
            progress += System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        if (progress > maxProgress) {
            mResults.finishGame();

            progress = 0;
            touchDown = false;
        }
    }

    public void render(Canvas canvas) {
        if (touchDown) {
            canvas.drawBitmap(cross, new Rect(0, 0, cross.getWidth(), cross.getHeight()), new RectF(position.x, position.y, position.x +  size, position.y + size), mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(MainView.ViewWidth / 100);
            canvas.drawArc(position.x - size, position.y - size, position.x + size * 2, position.y + size * 2, 270, 360 * progress / maxProgress - maxProgress / 4 + 270, false, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
        }
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown = true;
                position.x = x - size / 2;
                position.y = y - size / 2;
                progress = 0;

                break;

            case MotionEvent.ACTION_MOVE:
                if (x < position.x || x > position.x + size || y < position.y || y > position.y + size) {
                    touchDown = false;
                    progress = 0;
                }
                break;

            case MotionEvent.ACTION_UP:
                progress = 0;
                touchDown = false;
                break;
        }
    }

    public void reset() {
        touchDown = false;
        progress = 0;
    }

    public void load(Context context) {
        cross = BitmapFactory.decodeResource(context.getResources(), R.drawable.cross);
    }
}
