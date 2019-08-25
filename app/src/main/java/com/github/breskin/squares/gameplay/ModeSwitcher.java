package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.modes.EndlessMode;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.MoveLimitedMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

import java.util.ArrayList;
import java.util.List;

public class ModeSwitcher {

    private GameView gameView;
    private Paint paint;

    private List<GameMode> modeList;

    private EndlessMode endlessMode = new EndlessMode();
    private MoveLimitedMode moveLimitedMode = new MoveLimitedMode();
    private TimeLimitedMode timeLimitedMode = new TimeLimitedMode();

    private int selectedMode = 0;

    public ModeSwitcher(GameView gameView) {
        this.gameView = gameView;

        paint = new Paint();
        paint.setAntiAlias(true);

        modeList = new ArrayList<>();

        modeList.add(endlessMode);
        modeList.add(moveLimitedMode);
        modeList.add(timeLimitedMode);
    }

    public void update(GameLogic logic) {
        if (!logic.isGameStarted())
            logic.setCurrentMode(modeList.get(selectedMode));
    }

    public void render(Canvas canvas) {
        if (!gameView.getGameLogic().isGameStarted()) {
            GameMode selected = modeList.get(selectedMode);

            paint.setTextSize(RenderView.ViewWidth * 0.07f);
            paint.setColor(Color.WHITE);

            canvas.drawText(selected.getName(), 20, paint.getTextSize() * 1.5f, paint);
            canvas.drawText(selected.getDescription(), 20, paint.getTextSize() * 3f, paint);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
                if (!gameView.getGameLogic().isGameStarted() && y < gameView.getGameLogic().getBoard().getTranslation().y * 0.8f) {
                    selectedMode++;

                    if (selectedMode >= modeList.size())
                        selectedMode = 0;
                }
                break;
        }

        return false;
    }

    public GameMode getSelectedMode() {
        return modeList.get(selectedMode);
    }
}
