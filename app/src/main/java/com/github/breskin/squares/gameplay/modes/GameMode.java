package com.github.breskin.squares.gameplay.modes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.Block;
import com.github.breskin.squares.gameplay.Board;
import com.github.breskin.squares.gameplay.GameLogic;

import java.util.Stack;

public class GameMode {

    protected static Paint paint = new Paint();

    protected String yourScoreText, moveCountText, timeText;

    protected int pointsTable[];

    public GameMode() {
        paint.setAntiAlias(true);

        pointsTable = new int[7];

        yourScoreText = "your score: ";
        moveCountText = "moves: ";
        timeText = "time: ";
    }

    public void update(GameLogic logic) {
        checkCondition(logic);
        checkPatterns(logic);
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

    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        paint.setTextSize(RenderView.ViewWidth * 0.06f);
        paint.setColor(Color.WHITE);

        canvas.drawText(moveCountText + logic.moveCount, RenderView.ViewWidth * 0.01f, paint.getTextSize() * 1.5f, paint);
        canvas.drawText(timeText + timeToString(logic.gameDuration / 1000), RenderView.ViewWidth - paint.measureText(timeText + timeToString(logic.gameDuration / 1000)) - 10, paint.getTextSize() * 1.5f, paint);

        canvas.drawText(yourScoreText, (RenderView.ViewWidth - paint.measureText(yourScoreText)) / 2, paint.getTextSize() * 3f, paint);
        float margin = paint.getTextSize() * 3f;

        paint.setTextSize(RenderView.ViewWidth * 0.125f);
        canvas.drawText(Math.round(logic.pointsVisible)+"", (RenderView.ViewWidth - paint.measureText(Math.round(logic.pointsVisible)+"")) / 2, paint.getTextSize() + margin, paint);
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
