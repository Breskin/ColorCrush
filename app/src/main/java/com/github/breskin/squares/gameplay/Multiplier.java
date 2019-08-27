package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.modes.GameMode;

public class Multiplier {

    private Paint paint;
    private float translation = 0, alpha = 0, lastMargin = 0, radius = 0, markerPosition = 1;
    private GameMode gameMode;

    public Multiplier() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void update() {
        radius = RenderView.ViewWidth * 0.01f;

        markerPosition += (gameMode.getMultiplier() - markerPosition) * 0.15f;
    }

    public void render(Canvas canvas, float margin) {
        paint.setColor(Color.WHITE);

        float x = RenderView.ViewWidth * 0.1f;

        for (int i = 0; i < gameMode.getMaxMultiplierValue(); i++) {
            float distance = (i - markerPosition + 1);

            if (distance <= 0)
                paint.setColor(Color.argb((int) (alpha * 255), 255, 255, 255));
            else if (distance < 1){
                int color = (int)((1 - distance) * 207) + 48;
                paint.setColor(Color.argb((int) (alpha * 255), color, color, color));
            } else {
                paint.setColor(Color.argb((int) (alpha * 255), 48, 48, 48));
            }

            canvas.drawCircle(x + (float)RenderView.ViewWidth * 0.8f * i / (gameMode.getMaxMultiplierValue() - 1), translation + margin, radius, paint);
        }

        paint.setColor(Color.argb((int) (alpha * 255), 255, 255, 255));
        canvas.drawRect(new RectF(x, translation + margin - radius / 4, x + (float)RenderView.ViewWidth * 0.8f / (gameMode.getMaxMultiplierValue() - 1) * (markerPosition - 1), translation + margin + radius / 4), paint);

        lastMargin = margin;
    }

    public  boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_MOVE:
                if (y > translation + lastMargin - radius * 5 && y < translation + lastMargin + radius * 5) {
                    moveMarker(x);
                }
                break;

            case MotionEvent.ACTION_UP:

                break;
        }

        return false;
    }

    private void moveMarker(float x) {
        int index = Math.round((x - RenderView.ViewWidth * 0.1f) / (RenderView.ViewWidth * 0.8f / (gameMode.getMaxMultiplierValue() - 1))) + 1;

        if (index < 1) index = 1;
        if (index > gameMode.getMaxMultiplierValue()) index = gameMode.getMaxMultiplierValue();

        gameMode.setMultiplier(index);
    }

    public void setTranslation(float translation) {
        this.translation = translation;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}
