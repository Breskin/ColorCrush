package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;

public class Block {
    public static final float SIZE_MULTIPLIER = 0.166667f;

    private static Paint paint = new Paint();

    private Board board;

    private BlockColor currentColor, targetColor;
    private Point targetPosition;
    private PointF currentPosition;

    private PointF translation, previousTouch;

    private float currentExpansion = 0, targetExpansion = 0, colorChangeProgress = 0;

    private boolean selected = false;

    public Block(Board board, BlockColor color, int x, int y) {
        this.board = board;

        currentColor = color;
        targetColor = color;
        currentPosition = new PointF(x, y);
        targetPosition = new Point(x, y);

        translation = new PointF(0, 0);
        previousTouch = new PointF(0, 0);

        paint.setAntiAlias(true);
    }

    public void update() {
        currentExpansion += (targetExpansion - currentExpansion) * 0.2f;

        updateColor();

        if (!selected) {
            currentPosition.x += (targetPosition.x - currentPosition.x) * 0.175f;
            currentPosition.y += (targetPosition.y - currentPosition.y) * 0.175f;
        }
    }

    private void updateColor() {
        if (targetColor != currentColor) {
            colorChangeProgress += (1 - colorChangeProgress) * 0.1f;

            if (colorChangeProgress > 0.95f)
                currentColor = targetColor;
        }
    }

    public void render(Canvas canvas) {
        paint.setColor(BlockColor.calculateColor(currentColor, targetColor, colorChangeProgress));

        PointF position = getCalculatedPosition();

        canvas.drawRoundRect(position.x - currentExpansion * getSize(), position.y - currentExpansion * getSize(), position.x + (1 + currentExpansion) * getSize(), position.y + (1 + currentExpansion) * getSize(), getSize() * 0.1f, getSize() * 0.1f, paint);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        PointF position = getCalculatedPosition();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x > position.x && y > position.y && x < position.x + getSize() && y < position.y + getSize()) {
                    targetExpansion = 0.07f;

                    selected = true;
                    board.setSelectedBlock(this);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (selected) {
                    currentPosition.x += (x - previousTouch.x) / getSize();
                    currentPosition.y += (y - previousTouch.y) / getSize();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (selected) {
                    targetExpansion = 0;

                    selected = false;
                }
                break;
        }

        previousTouch.x = x;
        previousTouch.y = y;

        return false;
    }

    public boolean goingHome() {
        return !selected && (Math.abs(currentPosition.x - targetPosition.x) > 0.05f || Math.abs(currentPosition.y - targetPosition.y) > 0.05f);
    }

    public void moveTo(int x, int y) {
        targetPosition.x = x;
        targetPosition.y = y;
    }

    public void nextColor() {
        if (targetColor != currentColor) {
            currentColor = targetColor;
            targetColor = currentColor.nextColor();
        } else {
            targetColor = currentColor.nextColor();
        }

        colorChangeProgress = 0;
    }

    public void setTranslation(PointF translation) {
        this.translation = translation;
    }

    public PointF getCalculatedPosition() {
        return new PointF(translation.x + (targetPosition.x * SIZE_MULTIPLIER + currentPosition.x) * getSize(),
                            translation.y + (targetPosition.y * SIZE_MULTIPLIER + currentPosition.y) * getSize());
    }

    public PointF getCurrentPosition() {
        return currentPosition;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    public static float getSize() {
        return RenderView.ViewWidth * SIZE_MULTIPLIER;
    }
}
