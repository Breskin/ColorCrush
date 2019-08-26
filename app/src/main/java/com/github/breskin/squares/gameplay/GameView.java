package com.github.breskin.squares.gameplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.View;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

public class GameView implements View {

    private RenderView renderView;

    private Paint paint;
    private Path upArrow;

    private GameLogic gameLogic;
    private ModeSwitcher modeSwitcher;

    private boolean expandButtonHighlight = false, expanding = false;
    private float expandButtonAnimation = 1;

    public GameView(RenderView renderView) {
        this.renderView = renderView;

        paint = new Paint();
        paint.setAntiAlias(true);

        gameLogic = new GameLogic(this);
        modeSwitcher = new ModeSwitcher(this);
    }

    @Override
    public void update() {
        if (upArrow == null) {
            upArrow = new Path();
            upArrow.moveTo(-RenderView.ViewWidth * 0.055f, RenderView.ViewWidth * 0.03f);
            upArrow.lineTo(0, 0);
            upArrow.lineTo(RenderView.ViewWidth * 0.055f, RenderView.ViewWidth * 0.03f);
        }

        modeSwitcher.update(gameLogic);
        gameLogic.update();

        if (gameLogic.isGameStarted() || expanding) {
            expandButtonAnimation += (1 - expandButtonAnimation) * 0.175f;
        } else if (!gameLogic.isGameStarted()) {
            expandButtonAnimation += (0 - expandButtonAnimation) * 0.175f;
        }

        if (expanding && modeSwitcher.isClosed()) {
            renderView.switchView(RenderView.ViewType.Scoreboard);
        }
    }

    @Override
    public void render(Canvas canvas) {
        modeSwitcher.render(canvas);
        gameLogic.render(canvas);

        paint.setColor(Color.WHITE);

        if (expandButtonHighlight) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha((int)(64 * Math.abs(expandButtonAnimation - 1)));
            canvas.drawRect(0, RenderView.ViewHeight - RenderView.ViewWidth * 0.1f + RenderView.ViewWidth * 0.075f * expandButtonAnimation, RenderView.ViewWidth, RenderView.ViewHeight + RenderView.ViewWidth * 0.075f * expandButtonAnimation, paint);
        }

        paint.setAlpha((int)(255 * Math.abs(expandButtonAnimation - 1)));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(RenderView.ViewWidth * 0.007f);
        canvas.save();
        canvas.translate(RenderView.ViewWidth * 0.5f, RenderView.ViewHeight - RenderView.ViewWidth * 0.065f + RenderView.ViewWidth * 0.075f * expandButtonAnimation);
        canvas.drawPath(upArrow, paint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (modeSwitcher.onTouchEvent(event)) return true;
        else if (gameLogic.onTouchEvent(event)) return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!gameLogic.isGameStarted() && y > RenderView.ViewHeight - RenderView.ViewWidth * 0.1f) {
                    expandButtonHighlight = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!(!gameLogic.isGameStarted() && y > RenderView.ViewHeight - RenderView.ViewWidth * 0.1f)) {
                    expandButtonHighlight = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (expandButtonHighlight) {
                    gameLogic.getBoard().clear(gameLogic);
                    modeSwitcher.close();

                    expanding = true;
                }

                expandButtonHighlight = false;
                break;
        }

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
        expanding = false;

        gameLogic.prepare();
        modeSwitcher.open();
    }

    public void load(Context context) {
        modeSwitcher.load(context);
    }

    public RenderView getRenderView() {
        return renderView;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
