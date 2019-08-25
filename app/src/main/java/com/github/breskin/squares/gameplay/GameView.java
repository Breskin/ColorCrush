package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.View;
import com.github.breskin.squares.gameplay.modes.EndlessMode;
import com.github.breskin.squares.gameplay.modes.GameMode;

public class GameView implements View {

    private RenderView renderView;

    private GameLogic gameLogic;

    public GameView(RenderView renderView) {
        this.renderView = renderView;

        gameLogic = new GameLogic(this);
    }

    @Override
    public void update() {
        gameLogic.update();
    }

    @Override
    public void render(Canvas canvas) {
        gameLogic.render(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gameLogic.onTouchEvent(event);

        return false;
    }

    public GameMode getSelectedMode() {
        return new EndlessMode();
    }

    @Override
    public boolean onBackPressed() {
        return gameLogic.onBackPressed();
    }

    @Override
    public void open() {
        gameLogic.prepare();
    }

    public RenderView getRenderView() {
        return renderView;
    }
}
