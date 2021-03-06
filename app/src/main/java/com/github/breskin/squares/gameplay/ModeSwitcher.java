package com.github.breskin.squares.gameplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.provider.ContactsContract;
import android.view.MotionEvent;

import com.github.breskin.squares.DataManager;
import com.github.breskin.squares.MainActivity;
import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.modes.ConstantMode;
import com.github.breskin.squares.gameplay.modes.EndlessMode;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.gameplay.modes.MoveLimitedMode;
import com.github.breskin.squares.gameplay.modes.ResettingTimeMode;
import com.github.breskin.squares.gameplay.modes.TimeLimitedMode;

import java.util.ArrayList;
import java.util.List;

public class ModeSwitcher {

    private static final String LAST_SELECTED_MODE_LABEL = "last-selected-mode";

    private enum State { None, Opening, Closing, Switching }

    private GameView gameView;
    private Paint paint;
    private Path downArrow;
    private State currentState = State.None;

    private List<GameMode> modeList;

    private EndlessMode endlessMode = new EndlessMode();
    private MoveLimitedMode moveLimitedMode = new MoveLimitedMode();
    private TimeLimitedMode timeLimitedMode = new TimeLimitedMode();
    private ConstantMode constantMode = new ConstantMode();
    private ResettingTimeMode resettingTimeMode = new ResettingTimeMode();

    private Multiplier multiplier;

    private int selectedMode = 0;
    private float alpha = 0, translation = 0, switchAlpha = 0, switchTranslation = 0, switchTargetAlpha = 0, switchTargetTranslation = 0;
    private boolean switchButtonHighlight = false;

    public ModeSwitcher(GameView gameView) {
        this.gameView = gameView;

        paint = new Paint();
        paint.setAntiAlias(true);

        modeList = new ArrayList<>();

        multiplier = new Multiplier();

        modeList.add(endlessMode);
        modeList.add(moveLimitedMode);
        modeList.add(timeLimitedMode);
        modeList.add(constantMode);
        modeList.add(resettingTimeMode);
    }

    public void update(GameLogic logic) {
        if (downArrow == null) {
            downArrow = new Path();
            downArrow.moveTo(-RenderView.ViewWidth * 0.075f, -RenderView.ViewWidth * 0.035f);
            downArrow.lineTo(0, 0);
            downArrow.lineTo(RenderView.ViewWidth * 0.075f, -RenderView.ViewWidth * 0.035f);
        }

        if (logic.isGameStarted() && currentState != State.Closing)
            close();

        updateAnimations(logic);

        multiplier.setAlpha(switchAlpha);
        multiplier.setTranslation(switchTranslation);
        multiplier.setGameMode(modeList.get(selectedMode));
        multiplier.update();
    }

    private void updateAnimations(GameLogic logic) {
        switch (currentState) {
            case Closing:
                translation += (RenderView.ViewWidth * 0.6f - translation) * 0.085f;
                alpha += (0 - alpha) * 0.15f;

                switchTranslation = translation;
                switchAlpha = alpha;
                break;

            case Opening:
                translation += (0 - translation) * 0.085f;
                alpha += (1 - alpha) * 0.05f;

                switchTranslation = translation;
                switchAlpha = alpha;
                break;

            case Switching:
                switchTranslation += (switchTargetTranslation - switchTranslation) * 0.12f;
                switchAlpha += (switchTargetAlpha - switchAlpha) * 0.25f;

                if (switchAlpha < 0.05f) {
                    switchAlpha = 0.05f;
                    switchTargetAlpha = 1;
                    switchTargetTranslation = 0;
                    switchTranslation = -RenderView.ViewWidth * 0.4f;

                    selectedMode++;
                    if (selectedMode >= modeList.size()) selectedMode = 0;
                    DataManager.getPreferences().edit().putInt(LAST_SELECTED_MODE_LABEL, selectedMode).apply();

                    if (!logic.isGameStarted())
                        logic.setCurrentMode(modeList.get(selectedMode));
                }
                break;
        }
    }

    public void render(Canvas canvas) {
        GameMode selected = modeList.get(selectedMode);

        float margin = (gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.35f) * 0.5f;

        paint.setTextSize(RenderView.ViewWidth * 0.07f);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int)(255 * switchAlpha));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawText(selected.getName(), (RenderView.ViewWidth - paint.measureText(selected.getName())) * 0.5f,  switchTranslation + margin + paint.getTextSize() * 1.5f, paint);
        margin += paint.getTextSize() * 1.5f;

        paint.setTextSize(MainActivity.fitFontSize(paint, selected.getDescription(), RenderView.ViewWidth * 0.05f, RenderView.ViewWidth * 0.8f));
        canvas.drawText(selected.getDescription(), (RenderView.ViewWidth - paint.measureText(selected.getDescription())) * 0.5f, switchTranslation + margin + paint.getTextSize() * 1.75f, paint);
        margin += paint.getTextSize() * 3.5f;

        if (selected.usesMultiplier())
            multiplier.render(canvas, margin);

        if (switchButtonHighlight) {
            paint.setAlpha(64);
            canvas.drawRect(0, translation + gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.16f, RenderView.ViewWidth, translation + gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.045f, paint);
        }

        paint.setAlpha((int)(255 * alpha));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(RenderView.ViewWidth * 0.008f);
        canvas.save();
        canvas.translate(RenderView.ViewWidth * 0.5f, translation + gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.085f);
        canvas.drawPath(downArrow, paint);
        canvas.restore();
    }

    public void open() {
        alpha = 0;
        translation = -RenderView.ViewWidth * 0.5f;
        currentState = State.Opening;
    }

    public void close() {
        alpha = 1;
        translation = 0;
        currentState = State.Closing;
    }

    public boolean isClosed() {
        return currentState == State.Closing && alpha < 0.05f;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (gameView.getGameLogic().isGameStarted())
            return false;

        float x = event.getX();
        float y = event.getY();

        if (multiplier.onTouchEvent(event)) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!gameView.getGameLogic().isGameStarted() && y > translation + gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.16f && y < translation + gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.045f ) {
                    switchButtonHighlight = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!(!gameView.getGameLogic().isGameStarted() && y > gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.16f && y < gameView.getGameLogic().getBoard().getTranslation().y - RenderView.ViewWidth * 0.045f )) {
                    switchButtonHighlight = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (switchButtonHighlight) {
                    next();
                }

                switchButtonHighlight = false;
                break;
        }

        return false;
    }

    private void next() {
        if (currentState == State.Opening) {
            switchAlpha = alpha = 1;
            switchTranslation = translation = 0;
        }

        currentState = State.Switching;
        switchTargetAlpha = 0;
        switchTargetTranslation = RenderView.ViewWidth * 0.6f;
    }

    public void load(Context context) {
        endlessMode.load(context);
        moveLimitedMode.load(context);
        timeLimitedMode.load(context);
        constantMode.load(context);
        resettingTimeMode.load(context);

        selectedMode = DataManager.getPreferences().getInt(LAST_SELECTED_MODE_LABEL, 0);
    }

    public GameMode getSelectedMode() {
        return modeList.get(selectedMode);
    }
}
