package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.github.breskin.squares.RenderView;

import java.util.Random;

public class FloatingPoint {

    private static Paint paint = new Paint();
    private static Random random = new Random();

    private PointF origin, velocity;
    private int points;

    private float alpha = 1;

    public FloatingPoint(float x, float y, int points) {
        paint.setAntiAlias(true);

        origin = new PointF(x, y);
        this.points = points;

        velocity = new PointF(RenderView.ViewWidth * 0.01f * (random.nextFloat() - 0.5f), -RenderView.ViewHeight * 0.0075f * (random.nextFloat() + 0.2f));
    }

    public void update() {
        alpha -= 0.01f;

        origin.x += velocity.x * RenderView.FrameTime / 16f;
        origin.y += velocity.y * RenderView.FrameTime / 16f;

        velocity.y += RenderView.ViewWidth * 0.001f * RenderView.FrameTime / 16f;
    }

    public void render(Canvas canvas) {
        paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
        paint.setTextSize(RenderView.ViewWidth * 0.0525f);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("+" + points, origin.x - paint.measureText("+" + points) * 0.5f, origin.y, paint);
    }

    public boolean alive() {
        return origin.x > -RenderView.ViewWidth * 0.05f && origin.x < RenderView.ViewWidth * 1.05f && origin.y > 0 && origin.y < RenderView.ViewHeight * 1.05f;
    }
}
