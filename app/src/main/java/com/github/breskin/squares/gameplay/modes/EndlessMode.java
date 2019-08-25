package com.github.breskin.squares.gameplay.modes;

import android.content.Context;

import com.github.breskin.squares.R;

public class EndlessMode extends GameMode {

    public EndlessMode() {

    }

    @Override
    public void load(Context context) {
        super.load(context);

        name = context.getString(R.string.mode_endless);
        description = context.getString(R.string.mode_endless_description);
    }
}
