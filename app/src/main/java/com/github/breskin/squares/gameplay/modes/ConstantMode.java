package com.github.breskin.squares.gameplay.modes;

import android.content.Context;
import android.graphics.Canvas;

import com.github.breskin.squares.R;
import com.github.breskin.squares.gameplay.GameLogic;

public class ConstantMode extends GameMode {

    public ConstantMode() {

    }

    @Override
    protected void checkCondition(GameLogic logic) {
        if (locked && logic.getBoard().canFinish())
            logic.finishGame();
    }

    @Override
    public void onMoveMade(GameLogic logic) {
        super.onMoveMade(logic);

        if (logic.getBoard().findPatterns().size() == 0)
            locked = true;
    }

    @Override
    public void load(Context context) {
        super.load(context);

        name = context.getString(R.string.mode_constant);
        description = context.getString(R.string.mode_constant_description);
    }
}
