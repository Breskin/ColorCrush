package com.github.breskin.squares.gameplay.modes;

import android.content.Context;
import android.graphics.Canvas;

import com.github.breskin.squares.R;
import com.github.breskin.squares.gameplay.GameLogic;

public class TimeLimitedMode extends GameMode {

    private static final int BASE_TIME_LIMIT = 60000;

    private String remainingTime = "remaining time: ", description_one;

    private int timeLimit = BASE_TIME_LIMIT;

    public TimeLimitedMode() {
        usesMultiplier = true;
        maxMultiplierValue = 5;
        usesProgressBar = true;
    }

    @Override
    public void update(GameLogic logic) {
        super.update(logic);

        targetProgress = (float)(timeLimit - logic.gameDuration) / timeLimit;
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
            drawCustomizedInfo(logic, canvas, String.format(moveCountText, logic.moveCount), String.format(remainingTime, timeToString((timeLimit - logic.gameDuration) / 1000)));
        else
            super.renderSessionInfo(logic, canvas);
    }

    @Override
    public void reset() {
        super.reset();

        timeLimit = BASE_TIME_LIMIT * multiplier;
    }

    @Override
    public String getDescription() {
        if (multiplier == 1)
            return String.format(description_one, multiplier);
        else
            return String.format(description, multiplier);
    }

    @Override
    public void load(Context context) {
        super.load(context);

        name = context.getString(R.string.mode_timelimited);
        description = context.getString(R.string.mode_timelimited_description);
        description_one = context.getString(R.string.mode_timelimited_description_one);
        remainingTime = context.getString(R.string.info_time_left);
    }
}
