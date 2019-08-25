package com.github.breskin.squares.gameplay.modes;

import android.graphics.Canvas;

import com.github.breskin.squares.gameplay.GameLogic;

public class TimeLimitedMode extends GameMode {

    private String remainingTime = "remaining time: ";

    private int timeLimit = 60000;

    public TimeLimitedMode() {
        name = "time limited";
        description = "time limited description";
    }

    @Override
    protected void checkCondition(GameLogic logic) {
        if (!logic.isGameFinished() && logic.gameDuration >= timeLimit) {
            locked = true;

            if (logic.getBoard().canFinish())
                logic.finishGame();
        }
    }

    @Override
    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        if (!logic.isGameFinished())
            drawCustomizedInfo(logic, canvas, moveCountText + logic.moveCount, remainingTime + timeToString((timeLimit - logic.gameDuration) / 1000));
        else
            super.renderSessionInfo(logic, canvas);
    }
}
