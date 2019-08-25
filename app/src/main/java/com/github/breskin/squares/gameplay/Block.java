package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
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

    private boolean spawnAnimation = false, selected = false;

    public Block(Board board, BlockColor color, int x, int y) {
        this.board = board;

        currentColor = color;
        targetColor = color;
        currentPosition = new PointF(x, y);
        targetPosition = new Point(x, y);

        translation = new PointF(0, 0);
        previousTouch = new PointF(0, 0);

        spawnAnimation = true;

        paint.setAntiAlias(true);
    }

    public void update() {
        currentExpansion += (targetExpansion - currentExpansion) * 0.175f * (RenderView.FrameTime / 16f);

        updateColor();

        if (!selected) {
            float speed = RenderView.FrameTime / 16f;
            if (spawnAnimation && speed > 1.75)
                speed = 1.75f;

            currentPosition.x += (targetPosition.x - currentPosition.x) * ((spawnAnimation) ? 0.11f : 0.175f) * speed;
            currentPosition.y += (targetPosition.y - currentPosition.y) * ((spawnAnimation) ? 0.11f : 0.175f) * speed;
        }

        if (selected || (Math.abs(currentPosition.x - targetPosition.x) < 0.1f && Math.abs(currentPosition.y - targetPosition.y) < 0.1f))
            spawnAnimation = false;
    }

    private void updateColor() {
        if (targetColor != currentColor) {
            colorChangeProgress += (1 - colorChangeProgress) * 0.125f * (RenderView.FrameTime / 16f);

            if (colorChangeProgress > 0.9f)
                currentColor = targetColor;
        }
    }

    public void render(Canvas canvas) {
        if (currentColor == BlockColor.None)
            return;

        paint.setColor(BlockColor.calculateColor(currentColor, targetColor, colorChangeProgress));

        PointF position = getCalculatedPosition();

        canvas.drawRoundRect(position.x - currentExpansion * getSize(), position.y - currentExpansion * getSize(), position.x + (1 + currentExpansion) * getSize(), position.y + (1 + currentExpansion) * getSize(), getSize() * 0.1f, getSize() * 0.1f, paint);
    }

    public boolean onTouchEvent(GameLogic logic, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        PointF position = getCalculatedPosition();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!logic.getCurrentMode().isLocked() && x > position.x && y > position.y && x < position.x + getSize() && y < position.y + getSize()) {
                    targetExpansion = 0.07f;

                    selected = true;
                    board.setSelectedBlock(this);

                    previousTouch.x = x;
                    previousTouch.y = y;

                    return true;
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

    public boolean isSelected() {
        return selected;
    }

    public void deselect() {
        selected = false;
        targetExpansion = 0;
    }

    public void destroy(GameLogic logic) {
        PointF position = getCalculatedPosition();
        int color = currentColor.getColor();

        logic.getParticleSystem().createInArea(new RectF(position.x, position.y, position.x + getSize(), position.y + getSize()), getSize() * 0.3f, (android.os.Build.VERSION.SDK_INT >= 26) ? 15 : 7, (color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);


        currentExpansion = -0.5f;
        targetExpansion = 0;

        currentColor = targetColor = BlockColor.random();
    }

    public boolean canBeDestroyed() {
        return currentColor != BlockColor.None && currentColor == targetColor && currentExpansion > -0.005f && Math.abs(currentPosition.x - targetPosition.x) < 0.05f && Math.abs(currentPosition.y - targetPosition.y) < 0.05f;
    }

    public void setTranslation(PointF translation) {
        this.translation = translation;
    }

    public void setColor(BlockColor color) {
        currentColor = color;
        targetColor = color;
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

    public BlockColor getCurrentColor() {
        return currentColor;
    }

    public BlockColor getTargetColor() {
        return targetColor;
    }

    public float getCurrentExpansion() {
        return currentExpansion;
    }

    public static float getSize() {
        return RenderView.ViewWidth * SIZE_MULTIPLIER;
    }
}
