package com.github.breskin.squares.game.toaster;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.TextPaint;

import com.github.breskin.squares.MainView;

/**
 * Created by Kuba on 12.11.2018.
 */

public class Toast {
    private String Text;
    private PointF Position;
    private float Speed;
    private int Alpha;

    private static TextPaint mTextPaint = new TextPaint();

    public Toast(String text, PointF pos) {
        Text = text;
        Position = pos;
        Speed = MainView.ViewHeight * 0.005f;
        Alpha = 255;
    }

    public void update() {
        Alpha -= (255 - Alpha) * 0.05 + 1;
        if (Alpha < 0)
            Alpha = 0;

        Position.y -= Speed;
        Speed *= 0.95f;
    }

    public void render(Canvas canvas) {
        mTextPaint.setColor(Color.argb(Alpha, 255, 255, 255));
        mTextPaint.setTextSize(MainView.ViewWidth / 17);

        canvas.drawText(Text, Position.x - mTextPaint.measureText(Text) / 2, Position.y + mTextPaint.getTextSize() / 2, mTextPaint);
    }

    public boolean isVisible() {
        return Alpha != 0;
    }
}
