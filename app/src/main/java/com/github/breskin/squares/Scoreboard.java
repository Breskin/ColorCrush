package com.github.breskin.squares;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.breskin.squares.game.Overlord;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Kuba on 14.11.2018.
 */

public class Scoreboard {

    private enum AnimationType { None, Show, Hide }

    public enum ScoreType { Default, AllTimeHigh, TodayHigh }

    int InfiniteMaxScore, UntilPointlessMaxScore, MoveLimitedMaxScore[], TimeLimitedMaxScore[];
    int todayInfiniteMaxScore, todayUntilPointlessMaxScore, todayMoveLimitedMaxScore[], todayTimeLimitedMaxScore[];

    MainView mMainView;
    SharedPreferences preferences;
    Paint mPaint;
    BackButton backButton;

    private GregorianCalendar mCalendar;
    private AnimationType CurrentAnimation = AnimationType.None;
    private boolean back = false, touchDown = false, touchMoved = false;
    private float animationTranslation = 0, translation = 0, headlineFontSize, categoryFontSize, contentFontSize, touchDifference, contentSize;
    private int Alpha = 0;

    private PointF previousTouch;

    private String infinite, timeLimited, moveLimited, untilPointless, bestScores, today, allTime, gamesPlayed;

    public Scoreboard(MainView mv) {
        mMainView = mv;

        backButton = new BackButton(this);

        mCalendar = new GregorianCalendar();
        mCalendar.setTime(new Date());

        MoveLimitedMaxScore = new int[5];
        TimeLimitedMaxScore = new int[5];
        todayMoveLimitedMaxScore = new int[5];
        todayTimeLimitedMaxScore = new int[5];

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        previousTouch = new PointF(0, 0);
    }

    public void update() {
        backButton.update();

        headlineFontSize = MainView.fitFontSize(mPaint, bestScores, MainView.ViewWidth / 10, MainView.ViewWidth * 4 / 5);
        categoryFontSize = MainView.fitFontSize(mPaint, allTime, MainView.ViewWidth / 15, MainView.ViewWidth * 4 / 5);

        float timeLimitedFont = MainView.fitFontSize(mPaint, timeLimited, MainView.ViewWidth / 20, (int)(MainView.ViewWidth * 0.65f));
        float moveLimitedFont = MainView.fitFontSize(mPaint, moveLimited, MainView.ViewWidth / 20, (int)(MainView.ViewWidth * 0.65f));

        contentFontSize = (timeLimitedFont > moveLimitedFont) ? moveLimitedFont : timeLimitedFont;

        if (!touchDown && touchDifference != 0) {
            translation += touchDifference;
            touchDifference -= (touchDifference + 3 * Math.signum(touchDifference)) * 0.06f;

            if (Math.abs(touchDifference) < 0.5) {
                touchDifference = 0;
            }
        }

        if (touchMoved && touchDown) {
            translation += touchDifference;
            touchMoved = false;
        }

        if (translation > 0)
            translation = 0;

        if (contentSize > MainView.ViewHeight) {
            if (-translation + MainView.ViewHeight > contentSize)
                translation = -contentSize + MainView.ViewHeight;
        } else
            translation = 0;

        updateAnimations();
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Color.argb(Alpha, 255, 255, 255));

        float margin = MainView.ViewHeight * 0.1f;

        mPaint.setTextSize(headlineFontSize);
        canvas.drawText(bestScores, (MainView.ViewWidth - mPaint.measureText(bestScores)) / 2, margin + translation + animationTranslation + headlineFontSize, mPaint);

        margin += headlineFontSize + MainView.ViewHeight * 0.05f;

        String gamesplayedcount = String.format(gamesPlayed, MainView.Achievements.GamesPlayed);
        mPaint.setTextSize(categoryFontSize * 0.75f);
        canvas.drawText(gamesplayedcount, (MainView.ViewWidth - mPaint.measureText(gamesplayedcount)) / 2, margin + translation + animationTranslation + contentFontSize, mPaint);
        margin += categoryFontSize * 0.75f + MainView.ViewHeight * 0.07f;

        mPaint.setTextSize(categoryFontSize);
        canvas.drawText(today, (MainView.ViewWidth - mPaint.measureText(today)) / 2, margin + translation + animationTranslation + contentFontSize, mPaint);
        margin += categoryFontSize * 1.75f;
        margin = drawRecords(canvas, margin, todayInfiniteMaxScore, todayUntilPointlessMaxScore, todayTimeLimitedMaxScore, todayMoveLimitedMaxScore) + categoryFontSize;

        mPaint.setTextSize(categoryFontSize);
        canvas.drawText(allTime, (MainView.ViewWidth - mPaint.measureText(today)) / 2, margin + translation + animationTranslation + contentFontSize, mPaint);
        margin += categoryFontSize * 1.75f;
        margin = drawRecords(canvas, margin, InfiniteMaxScore, UntilPointlessMaxScore, TimeLimitedMaxScore, MoveLimitedMaxScore) + MainView.ViewHeight * 0.1f;

        contentSize = margin;

        backButton.render(canvas);
    }

    float drawRecords(Canvas canvas, float margin, int infiniteMaxScore, int untilPointlessMaxScore, int timeLimitedMaxScore[], int moveLimitedMaxScore[]) {
        mPaint.setTextSize(contentFontSize);
        canvas.drawText(infinite + ":", MainView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, mPaint);
        canvas.drawText(infiniteMaxScore+"", MainView.ViewWidth * 0.95f - mPaint.measureText(infiniteMaxScore + ""), margin + translation + animationTranslation + contentFontSize, mPaint);

        margin += contentFontSize * 2.5f;

        mPaint.setTextSize(contentFontSize);
        canvas.drawText(untilPointless + ":", MainView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, mPaint);
        canvas.drawText(untilPointlessMaxScore+"", MainView.ViewWidth * 0.95f - mPaint.measureText(untilPointlessMaxScore + ""), margin + translation + animationTranslation + contentFontSize, mPaint);

        margin += contentFontSize * 2.5f;

        canvas.drawText(timeLimited + ":", MainView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, mPaint);
        margin += contentFontSize * 1.75f;

        for (int i=0; i<5; i++) {
            drawRecordMultiplier(canvas, margin, i);
            mPaint.setColor(Color.argb(Alpha, 255, 255, 255));
            canvas.drawText(timeLimitedMaxScore[i]+"", MainView.ViewWidth * 0.95f - mPaint.measureText(timeLimitedMaxScore[i] + ""), margin + translation + animationTranslation + contentFontSize, mPaint);

            margin += contentFontSize * 1.75f;
        }

        margin += contentFontSize * 1.25f;


        canvas.drawText(moveLimited + ":", MainView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, mPaint);
        margin += contentFontSize * 1.75f;

        for (int i=0; i<5; i++) {
            drawRecordMultiplier(canvas, margin, i);
            mPaint.setColor(Color.argb(Alpha, 255, 255, 255));
            canvas.drawText(moveLimitedMaxScore[i]+"", MainView.ViewWidth * 0.95f - mPaint.measureText(moveLimitedMaxScore[i] + ""), margin + translation + animationTranslation + contentFontSize, mPaint);

            margin += contentFontSize * 1.75f;
        }

        margin += contentFontSize * 1.25f;

        return margin;
    }

    void drawRecordMultiplier(Canvas canvas, float margin, int multiplier) {
        float width = MainView.ViewWidth * 0.12f, top = (int)(margin + translation + animationTranslation + contentFontSize * 0.7f);

        canvas.drawRect(new RectF(MainView.ViewWidth * 0.1f, top - MainView.ViewWidth * 0.002f, MainView.ViewWidth * 0.1f + width * multiplier, top + MainView.ViewWidth * 0.002f), mPaint);

        for (int i = 0; i < 5; i++) {
            canvas.drawCircle(MainView.ViewWidth * 0.1f + width * i, top, MainView.ViewWidth * 0.01f, mPaint);

            if (i >= multiplier)
                mPaint.setColor(Color.argb(Alpha, 64, 64, 64));
        }

        if (multiplier < 4)
            canvas.drawRect(new RectF(MainView.ViewWidth * 0.05f, (int)(margin + translation + animationTranslation + contentFontSize * 1.5f), MainView.ViewWidth * 0.95f, (int)(margin + translation + animationTranslation + contentFontSize * 1.5f + MainView.ViewWidth * 0.004f)), mPaint);
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        backButton.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDifference = 0;
                touchDown = true;
                break;

            case MotionEvent.ACTION_MOVE:
                touchDifference = (touchDifference + (y - previousTouch.y)) / 2;
                touchMoved = true;
                break;

            case MotionEvent.ACTION_UP:
                translation -= touchDifference;
                touchDown = false;
                break;
        }

        previousTouch.x = x;
        previousTouch.y = y;
    }

    void updateAnimations() {
        switch (CurrentAnimation) {
            case Show:
                animationTranslation -= (animationTranslation + 10) * 0.1f * (MainView.FrameTime / 16f);
                Alpha += 15;

                if (Alpha > 255)
                    Alpha = 255;

                if (animationTranslation < 1) {
                    animationTranslation = 0;
                    Alpha = 255;
                    CurrentAnimation = AnimationType.None;
                }
                break;

            case Hide:
                animationTranslation += (MainView.ViewHeight / 2 - animationTranslation + 2) * 0.08f;
                Alpha -= 17;

                if (Alpha < 0) {
                    Alpha = 0;
                    CurrentAnimation = AnimationType.None;

                    mMainView.nextView();
                }
                break;
        }
    }

    public void show() {
        CurrentAnimation = AnimationType.Show;
        translation = touchDifference = Alpha = 0;
        animationTranslation = MainView.ViewHeight / 3;
        touchDown = false;

        backButton.show();
    }

    public void hide() {
        CurrentAnimation = AnimationType.Hide;
        MainView.NextView = MainView.ViewType.Game;

        backButton.hide();
    }

    public boolean onBackPressed() {
        hide();

        return true;
    }

    public ScoreType checkScore(int score) {
        int index = Overlord.ModeMultiplier - 1;
        if (index < 0 || index > 4)
            return ScoreType.Default;

        ScoreType result = ScoreType.Default;

        switch (Overlord.CurrentMode) {
            case Infinite:
                if (score > todayInfiniteMaxScore) {
                    todayInfiniteMaxScore = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("today-high-score-infinite", todayInfiniteMaxScore);
                    editor.commit();

                    result = ScoreType.TodayHigh;
                }

                if (score > InfiniteMaxScore) {
                    InfiniteMaxScore = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("high-score-infinite", InfiniteMaxScore);
                    editor.commit();

                    result = ScoreType.AllTimeHigh;
                }
                break;

            case TimeLimited:
                if (score > todayTimeLimitedMaxScore[index]) {
                    todayTimeLimitedMaxScore[index] = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("today-high-score-time-limited-" + index, todayTimeLimitedMaxScore[index]);
                    editor.commit();

                    result = ScoreType.TodayHigh;
                }

                if (score > TimeLimitedMaxScore[index]) {
                    TimeLimitedMaxScore[index] = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("high-score-time-limited-" + index, TimeLimitedMaxScore[index]);
                    editor.commit();

                    result = ScoreType.AllTimeHigh;
                }
                break;

            case MoveLimited:
                if (score > todayMoveLimitedMaxScore[index]) {
                    todayMoveLimitedMaxScore[index] = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("today-high-score-move-limited-" + index, todayMoveLimitedMaxScore[index]);
                    editor.commit();

                    result = ScoreType.TodayHigh;
                }

                if (score > MoveLimitedMaxScore[index]) {
                    MoveLimitedMaxScore[index] = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("high-score-move-limited-" + index, MoveLimitedMaxScore[index]);
                    editor.commit();

                    result = ScoreType.AllTimeHigh;
                }
                break;

            case UntilPointless:
                if (score > todayUntilPointlessMaxScore) {
                    todayUntilPointlessMaxScore = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("today-high-score-until-pointless", todayUntilPointlessMaxScore);
                    editor.commit();

                    result = ScoreType.TodayHigh;
                }

                if (score > UntilPointlessMaxScore) {
                    UntilPointlessMaxScore = score;

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("high-score-until-pointless", UntilPointlessMaxScore);
                    editor.commit();

                    result = ScoreType.AllTimeHigh;
                }
                break;
        }

        return result;
    }

    public void load(Context context) {
        backButton.load(context);

        preferences = context.getSharedPreferences("scoreboard", Context.MODE_PRIVATE);

        InfiniteMaxScore = preferences.getInt("high-score-infinite", 0);
        UntilPointlessMaxScore = preferences.getInt("high-score-until-pointless", 0);

        for (int i = 0; i < 5; i++)
            TimeLimitedMaxScore[i] = preferences.getInt("high-score-time-limited-" + i, 0);

        for (int i = 0; i < 5; i++)
            MoveLimitedMaxScore[i] = preferences.getInt("high-score-move-limited-" + i, 0);


        if (preferences.getString("last-day-played", "none").equals(mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH))) {
            todayInfiniteMaxScore = preferences.getInt("today-high-score-infinite", 0);
            todayUntilPointlessMaxScore = preferences.getInt("today-high-score-until-pointless", 0);
            for (int i = 0; i < 5; i++)
                todayTimeLimitedMaxScore[i] = preferences.getInt("today-high-score-time-limited-" + i, 0);

            for (int i = 0; i < 5; i++)
                todayMoveLimitedMaxScore[i] = preferences.getInt("today-high-score-move-limited-" + i, 0);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("last-day-played", mCalendar.get(Calendar.DAY_OF_MONTH) + "/" + mCalendar.get(Calendar.MONTH));
            editor.putInt("today-high-score-infinite", 0);
            for (int i = 0; i < 5; i++)
                editor.putInt("today-high-score-time-limited-" + i, 0);
            for (int i = 0; i < 5; i++)
                editor.putInt("today-high-score-move-limited-" + i, 0);
            editor.commit();
        }

        bestScores = context.getString(R.string.scoreboard_bestscores);
        infinite = context.getString(R.string.gamemode_infinite);
        timeLimited = context.getString(R.string.gamemode_timelimited);
        moveLimited = context.getString(R.string.gamemode_movelimited);
        untilPointless = context.getString(R.string.gamemode_untilpointless);
        today = context.getString(R.string.scoreboard_today);
        allTime = context.getString(R.string.scoreboard_alltime);
        gamesPlayed = context.getString(R.string.games_played);
    }
}
