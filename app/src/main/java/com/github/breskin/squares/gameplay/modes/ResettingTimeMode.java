package com.github.breskin.squares.gameplay.modes;

import android.content.Context;

import com.github.breskin.squares.R;
import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.Block;
import com.github.breskin.squares.gameplay.GameLogic;

public class ResettingTimeMode extends GameMode {

    private static final int MOVE_TIME = 2000;

    private float remainingTime = MOVE_TIME;

    @Override
    public void update(GameLogic logic) {
        super.update(logic);

        remainingTime -= RenderView.FrameTime;

        targetProgress = remainingTime / MOVE_TIME;
    }

    @Override
    protected void checkCondition(GameLogic logic) {
        if (!logic.isGameFinished() && progress <= 0) {
            locked = true;
            logic.finishGame();
        }
    }

    @Override
    public void onMoveMade(GameLogic logic) {
        super.onMoveMade(logic);

        remainingTime += 200;

        if (remainingTime > MOVE_TIME)
            remainingTime = MOVE_TIME;
    }

    @Override
    protected void onBlockDestroyed(GameLogic logic, Block block) {
        super.onBlockDestroyed(logic, block);

        remainingTime = MOVE_TIME;
    }

    @Override
    public void reset() {
        super.reset();

        remainingTime = MOVE_TIME;
    }

    @Override
    public void load(Context context) {
        super.load(context);

        name = context.getString(R.string.mode_resetting_time);
        description = context.getString(R.string.mode_resetting_time_description);
    }
}
