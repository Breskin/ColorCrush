package com.github.breskin.squares.gameplay.modes;

import android.content.Context;
import android.graphics.Canvas;

import com.github.breskin.squares.R;
import com.github.breskin.squares.gameplay.GameLogic;

public class MoveLimitedMode extends GameMode {

    private static final int BASE_MOVE_LIMIT = 50;

    private String remainingMoves = "remaining moves: ";

    private int moveLimit = BASE_MOVE_LIMIT;

    public MoveLimitedMode() {
        usesMultiplier = true;
        maxMultiplierValue = 5;
        usesProgressBar = true;
    }

    @Override
    public void update(GameLogic logic) {
        super.update(logic);

        targetProgress = (float)(moveLimit - logic.moveCount) / moveLimit;
    }

    @Override
    protected void checkCondition(GameLogic logic) {
        if (!logic.isGameFinished() && logic.moveCount >= moveLimit) {
            locked = true;
            logic.moveCount = moveLimit;

            if (logic.getBoard().canFinish())
                logic.finishGame();
        }
    }

    @Override
    public void renderSessionInfo(GameLogic logic, Canvas canvas) {
        if (!logic.isGameFinished())
            drawCustomizedInfo(logic, canvas, String.format(remainingMoves, moveLimit - logic.moveCount), String.format(timeText, timeToString(logic.gameDuration / 1000)));
        else
            super.renderSessionInfo(logic, canvas);
    }

    @Override
    public void reset() {
        super.reset();

        moveLimit = BASE_MOVE_LIMIT * multiplier;
    }

    @Override
    public String getDescription() {
        return String.format(description, BASE_MOVE_LIMIT * multiplier);
    }

    @Override
    public void load(Context context) {
        super.load(context);

        name = context.getString(R.string.mode_movelimited);
        description = context.getString(R.string.mode_movelimited_description);
        remainingMoves = context.getString(R.string.info_moves_left);
    }
}
