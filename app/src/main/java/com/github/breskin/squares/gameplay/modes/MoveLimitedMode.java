package com.github.breskin.squares.gameplay.modes;

import android.graphics.Canvas;

import com.github.breskin.squares.gameplay.GameLogic;

public class MoveLimitedMode extends GameMode {

    private String remainingMoves = "remaining moves: ";

    private int moveLimit = 50;

    public MoveLimitedMode() {
        name = "move limited";
        description = "move limited description";
    }

    @Override
    protected void checkCondition(GameLogic logic) {
        if (!logic.isGameFinished() && logic.moveCount >= moveLimit) {
            locked = true;

            if (logic.getBoard().canFinish())
                logic.finishGame();
        }
    }

    @Override
    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        if (!logic.isGameFinished())
            drawCustomizedInfo(logic, canvas, remainingMoves + (moveLimit - logic.moveCount), timeText + timeToString(logic.gameDuration / 1000));
        else
            super.renderSessionInfo(logic, canvas);
    }
}
