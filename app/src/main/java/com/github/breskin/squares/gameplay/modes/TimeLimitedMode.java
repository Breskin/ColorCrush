package com.github.breskin.squares.gameplay.modes;

import android.content.Context;
import android.graphics.Canvas;

import com.github.breskin.squares.DataManager;
import com.github.breskin.squares.R;
import com.github.breskin.squares.gameplay.GameLogic;

public class TimeLimitedMode extends GameMode {

    public static final int MAX_MULTIPLIER_VALUE = 5;

    private static final int BASE_TIME_LIMIT = 60000;
    private static final String MULTIPLIER_LABEL = "mode-time-limited-multiplier";

    private String remainingTime, description_one;

    private int timeLimit = BASE_TIME_LIMIT;

    public TimeLimitedMode() {
        usesMultiplier = true;
        maxMultiplierValue = MAX_MULTIPLIER_VALUE;
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
    public void setMultiplier(int multiplier) {
        super.setMultiplier(multiplier);

        DataManager.getPreferences().edit().putInt(MULTIPLIER_LABEL, multiplier).apply();
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

        name = context.getString(R.string.mode_time_limited);
        description = context.getString(R.string.mode_time_limited_description);
        description_one = context.getString(R.string.mode_time_limited_description_one);
        remainingTime = context.getString(R.string.info_time_left);

        multiplier = DataManager.getPreferences().getInt(MULTIPLIER_LABEL, 1);
    }
}
