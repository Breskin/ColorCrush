package com.github.breskin.squares.gameplay.modes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

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

    protected boolean closing = false, locked = false;
    protected float animationProgress = 0, topMargin = 0, alpha = 0;

    public GameMode() {
        paint.setAntiAlias(true);

        pointsTable = new int[7];

        yourScoreText = "your score: ";
        moveCountText = "moves: ";
        timeText = "time: ";
    }

    public void reset() {
        animationProgress = 0;
        topMargin = -RenderView.ViewHeight * 0.4f;
        alpha = 0;

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

    public void onMoveMade() {

    }

    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        drawCustomizedInfo(logic, canvas, moveCountText + logic.moveCount, timeText + timeToString(logic.gameDuration / 1000));
    }

    protected void drawCustomizedInfo(GameLogic logic, Canvas canvas, String moves, String time) {
        float margin = (logic.getBoard().getTranslation().y - RenderView.ViewWidth * 0.335f) / 2 + topMargin + animationProgress * Block.getSize() * 2;

        paint.setTextSize(RenderView.ViewWidth * 0.055f);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int)(alpha * 255));

        canvas.drawText(moves, RenderView.ViewWidth * 0.01f + (RenderView.ViewWidth * 0.98f - paint.measureText(moves)) * animationProgress * 0.5f,
                margin + paint.getTextSize() * 1.5f + RenderView.ViewWidth * 0.425f * animationProgress, paint);
        canvas.drawText(time, RenderView.ViewWidth * 0.99f - paint.measureText(time) - (RenderView.ViewWidth * 0.98f - paint.measureText(time)) * animationProgress * 0.5f,
                margin + paint.getTextSize() * 1.5f + (RenderView.ViewWidth * 0.425f + paint.getTextSize() * 1.75f) * animationProgress, paint);

        canvas.drawText(yourScoreText, (RenderView.ViewWidth - paint.measureText(yourScoreText)) / 2, margin + paint.getTextSize() * 3.5f, paint);
        margin += paint.getTextSize() * 3.5f;

        paint.setTextSize(RenderView.ViewWidth * (0.125f + animationProgress * 0.1f));
        canvas.drawText(Math.round(logic.pointsVisible)+"", (RenderView.ViewWidth - paint.measureText(Math.round(logic.pointsVisible)+"")) / 2, paint.getTextSize() + margin, paint);
    }

    public boolean isClosed() {
        return closing && alpha < 0.07f;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
