package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.View;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

public class GameView implements View {

    private RenderView renderView;

    private GameLogic gameLogic;
    private ModeSwitcher modeSwitcher;

    public GameView(RenderView renderView) {
        this.renderView = renderView;

        gameLogic = new GameLogic(this);
        modeSwitcher = new ModeSwitcher(this);
    }

    @Override
    public void update() {
        modeSwitcher.update(gameLogic);
        gameLogic.update();
    }

    @Override
    public void render(Canvas canvas) {
        modeSwitcher.render(canvas);
        gameLogic.render(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (modeSwitcher.onTouchEvent(event)) return true;
        else if (gameLogic.onTouchEvent(event)) return true;

        return false;
    }

    public GameMode getSelectedMode() {
        return modeSwitcher.getSelectedMode();
    }

    @Override
    public boolean onBackPressed() {
        return gameLogic.onBackPressed();
    }

    @Override
    public void open() {
        gameLogic.prepare();
        modeSwitcher.open();
    }

    public RenderView getRenderView() {
        return renderView;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
