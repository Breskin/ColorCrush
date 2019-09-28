package com.github.breskin.squares.scoreboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.github.breskin.squares.R;
import com.github.breskin.squares.RenderView;

public class InfoButton {

    private ScoreboardView scoreboardView;

    private Bitmap icon;
    private Paint paint;

    private PointF touchPosition;
    private boolean touchDown = false;

    public float alpha = 0;
    public float topMargin = 0;
    private float leftMargin = 0, size = 0;

    public InfoButton(ScoreboardView scoreboardView) {
        this.scoreboardView = scoreboardView;

        paint = new Paint();
        touchPosition = new PointF(0, 0);
    }

    public void render(Canvas canvas) {
        if (touchDown) {
            paint.setColor(Color.argb((int)(255 * alpha), 128, 128, 128));
            canvas.drawRect(new RectF(leftMargin - size * 0.2f, topMargin - size * 0.2f, leftMargin + size * 1.2f, topMargin + size * 1.2f), paint);
        }

        canvas.drawBitmap(icon, new Rect(0, 0, icon.getWidth(), icon.getHeight()), new RectF(leftMargin, topMargin, leftMargin + size, topMargin + size), paint);
    }

    public void update() {
        size = RenderView.ViewWidth * 0.07f;
        leftMargin = RenderView.ViewWidth * 0.85f;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x > leftMargin && x < leftMargin + size && y > topMargin && y < topMargin + size) {
                    touchDown = true;
                    touchPosition.x = x;
                    touchPosition.y = y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (x < leftMargin || x > leftMargin + size || y < topMargin || y > topMargin + size) {
                    touchDown = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (touchDown) {
                    scoreboardView.openInfo();
                }
                break;
        }



        return false;
    }

    public void load(Context context) {
        icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_info);
    }
}
