package com.github.breskin.squares.game.context;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.github.breskin.squares.Scoreboard;
import com.github.breskin.squares.game.Overlord;

/**
 * Created by Kuba on 11.11.2018.
 */

public class ContextualInfo {

    enum InfoSection { Picker, Results }

    private Overlord mOverlord;

    private ModePicker ModePicker;
    private Results Results;

    private InfoSection CurrentSection;

    public ContextualInfo(Overlord o) {
        mOverlord = o;

        ModePicker = new ModePicker(this);
        Results = new Results(this);
    }

    public void update() {
        ModePicker.update();
        Results.update();
    }

    public void render(Canvas canvas) {
        switch (CurrentSection) {
            case Picker:
                ModePicker.render(canvas);
                break;

            case Results:
                Results.render(canvas);
                break;
        }
    }

    public void onGameStarted() {
        ModePicker.onGameStarted();
    }

    public void onGameFinished() {
        if (CurrentSection == InfoSection.Results)
            Results.onGameFinished();
    }

    public void onUIHidden() {
        mOverlord.onUIHidden();
    }

    public void resetGame() {
        mOverlord.createNewGame();
    }

    public void finishGame() {
        mOverlord.setGameConditionMet(true);
    }

    public void showResults() {
        if (CurrentSection != InfoSection.Results) {
            CurrentSection = InfoSection.Results;
            Results.show();
        }
    }

    public Scoreboard.ScoreType getScoreType() {
        return mOverlord.getScoreType();
    }

    public void onTouchEvent(MotionEvent event) {
        switch (CurrentSection) {
            case Picker:
                ModePicker.onTouchEvent(event);
                break;

            case Results:
                Results.onTouchEvent(event);
                break;
        }
    }

    public void playInitialAnimation() {
        CurrentSection = InfoSection.Picker;
        ModePicker.playInitialAnimation();
    }

    public void hide() {
        switch (CurrentSection) {
            case Picker:
                ModePicker.hide();
                break;

            case Results:
                Results.hide();
                break;
        }
    }

    public void back() {
        Results.back();
    }

    public boolean isAnimating() {
        if (CurrentSection == InfoSection.Picker)
            return ModePicker.isAnimating();

        if (CurrentSection == InfoSection.Results)
            return Results.isAnimating();

        return false;
    }

    public void load(Context context) {
        ModePicker.load(context);
        Results.load(context);
    }
}
