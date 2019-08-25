package com.github.breskin.squares.gameplay.modes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.github.breskin.squares.R;
import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.Block;
import com.github.breskin.squares.gameplay.Board;
import com.github.breskin.squares.gameplay.GameLogic;

import java.util.Stack;

public class GameMode {

    protected String name, description;

    protected static Paint paint = new Paint();

    protected String yourScoreText, moveCountText, timeText;

    protected int pointsTable[];
    protected int multiplier = 1, maxMultiplierValue = 5;

    protected boolean closing = false, locked = false, usesMultiplier = false, usesProgressBar = false;
    protected float animationProgress = 0, topMargin = 0, alpha = 0, progress = 0, targetProgress = 0;

    public GameMode() {
        paint.setAntiAlias(true);

        pointsTable = new int[7];
    }

    public void reset() {
        animationProgress = 0;
        topMargin = -RenderView.ViewHeight * 0.4f;
        alpha = 0;

        progress = targetProgress = 1;

        locked = false;
        closing = false;
    }

    public void close() {
        closing = true;
    }

    public void update(GameLogic logic) {
        checkCondition(logic);
        checkPatterns(logic);

        updateAnimations(logic);

        progress += (targetProgress - progress) * 0.15f;
    }

    private void updateAnimations(GameLogic logic) {
        if (logic.isGameStarted() && !logic.isGameFinished()) {
            topMargin += (0 - topMargin) * 0.15f * RenderView.FrameTime / 16f;
            alpha += (1 - alpha) * 0.1f * RenderView.FrameTime / 16f;
        } else if (logic.isGameFinished()) {
            animationProgress += (1 - animationProgress) * 0.1f * RenderView.FrameTime / 16f;

            if (closing && animationProgress > 0.9) {
                topMargin += (RenderView.ViewHeight * 0.8f - topMargin) * 0.07f * RenderView.FrameTime / 16f;
                alpha += (0 - alpha) * 0.15f * RenderView.FrameTime / 16f;
            }
        }
    }

    protected void checkCondition(GameLogic logic) {

    }

    protected void checkPatterns(GameLogic logic) {
        Stack<Block> patterns = logic.getBoard().findPatterns();

        boolean proceed = true;

        for (int i = 0; i < 7; i++)
            pointsTable[i] = -2;

        for (Block block : patterns) {
            if (!block.canBeDestroyed()) {
                proceed = false;
            }

            if (block.isSelected())
                block.deselect();

            pointsTable[block.getCurrentColor().getId()]++;
        }

        if (proceed) {
            for (Block block : patterns) {
                logic.points += pointsTable[block.getCurrentColor().getId()];
                logic.getBoard().addFloatingPoint(block, pointsTable[block.getCurrentColor().getId()]);

                block.destroy(logic);
            }
        }
    }

    public void onMoveMade(GameLogic logic) {

    }

    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        drawCustomizedInfo(logic, canvas, String.format(moveCountText, logic.moveCount), String.format(timeText, timeToString(logic.gameDuration / 1000)));
    }

    protected void drawCustomizedInfo(GameLogic logic, Canvas canvas, String moves, String time) {
        float margin = (logic.getBoard().getTranslation().y - RenderView.ViewWidth * 0.16f) / 2 + topMargin + animationProgress * Block.getSize() * 2;

        paint.setTextSize(RenderView.ViewWidth * 0.05f);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int)(alpha * 255));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawText(moves, RenderView.ViewWidth * 0.01f + (RenderView.ViewWidth * 0.98f - paint.measureText(moves)) * animationProgress * 0.5f,
                paint.getTextSize() * 2f + (margin + RenderView.ViewWidth * 0.3f) * animationProgress, paint);
        canvas.drawText(time, RenderView.ViewWidth * 0.99f - paint.measureText(time) - (RenderView.ViewWidth * 0.98f - paint.measureText(time)) * animationProgress * 0.5f,
                paint.getTextSize() * 2f + (margin + RenderView.ViewWidth * 0.3f + paint.getTextSize() * 1.75f) * animationProgress, paint);

        paint.setTextSize(RenderView.ViewWidth * (0.04f + animationProgress * 0.01f));
        canvas.drawText(yourScoreText, (RenderView.ViewWidth - paint.measureText(yourScoreText)) / 2, margin + paint.getTextSize(), paint);
        margin += paint.getTextSize();

        paint.setTextSize(RenderView.ViewWidth * (0.125f + animationProgress * 0.1f));
        canvas.drawText(Math.round(logic.pointsVisible)+"", (RenderView.ViewWidth - paint.measureText(Math.round(logic.pointsVisible)+"")) / 2, paint.getTextSize() + margin, paint);

        if (usesProgressBar) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(RenderView.ViewWidth * 0.01f);
            paint.setAlpha((int)(alpha * 255 * Math.pow(1 - animationProgress, 3)));

            float radius = RenderView.ViewWidth * 0.18f;

            canvas.drawArc(new RectF(RenderView.ViewWidth * 0.5f - radius, paint.getTextSize() * 0.4f + margin - radius, RenderView.ViewWidth * 0.5f + radius, paint.getTextSize() * 0.4f + margin + radius), -90, 360 * progress, false, paint);
        }
    }

    public boolean isClosed() {
        return closing && alpha < 0.07f;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean usesMultiplier() {
        return usesMultiplier;
    }

    public int getMaxMultiplierValue() {
        return maxMultiplierValue;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void load(Context context) {
        yourScoreText = context.getString(R.string.info_your_score);
        timeText = context.getString(R.string.info_time);
        moveCountText = context.getString(R.string.info_moves);
    }

    String timeToString(int t) {
        StringBuilder builder = new StringBuilder();

        int hours = t / 3600;
        t -= hours * 3600;
        if (hours > 0)
            builder.append(hours + "h ");

        int minutes = t / 60;
        t -= minutes * 60;
        if (minutes > 0)
            builder.append(minutes + "m ");

        builder.append(t + "s");

        return builder.toString();
    }
}
