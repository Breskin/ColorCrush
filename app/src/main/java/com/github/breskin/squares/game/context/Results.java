package com.github.breskin.squares.game.context;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.Scoreboard;
import com.github.breskin.squares.achievements.Achievements;
import com.github.breskin.squares.game.Board;
import com.github.breskin.squares.game.Overlord;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;

/**
 * Created by Kuba on 12.11.2018.
 */

public class Results {

    private enum AnimationType { None, Show, Grow, Hide, HideUI }

    private Context mContext;

    private ContextualInfo mGameInfo;
    private EndGameButton EndButton;

    private TextPaint mPaint;
    private AnimationType currentAnimation = AnimationType.None;
    private String YourScore, Time, Moves, TimeLeft, MovesLeft, NewHighScore, TodayHighScore;

    private float Height, scoreFontSize = -1, commentFontSize = -1, highscoreFontSize = -1, scale = 1, contentMargin = 0, contentTranslation = 0, targetEndTranslation = 0;
    private int Alpha = 0, previousMoves = 0, highscoreAlpha = 0;
    private boolean ReadyForGame = false, TouchDown = false, gameFinished = false;

    public Results(ContextualInfo gameInfo) {
        mGameInfo = gameInfo;
        EndButton = new EndGameButton(this);

        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
    }

    public void update() {
        Height = Board.getTopMargin();
        scoreFontSize = MainView.fitFontSize(mPaint, Math.round(Overlord.Points)+"", MainView.ViewWidth / 8 * scale, MainView.ViewWidth * 4 / 5);
        commentFontSize = MainView.fitFontSize(mPaint, YourScore, MainView.ViewWidth / 20, MainView.ViewWidth * 4 / 5);
        highscoreFontSize = MainView.fitFontSize(mPaint, NewHighScore, MainView.ViewWidth / 15, MainView.ViewWidth * 4 / 5);

        contentMargin = (Height + commentFontSize * 0.5f - scoreFontSize * 1.25f) / 2f;
        targetEndTranslation = MainView.ViewWidth / 3;

        EndButton.update();

        updateAnimations();
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Color.argb(highscoreAlpha, 255, 255, 255));
        mPaint.setTextSize(highscoreFontSize);

        if (mGameInfo.getScoreType() == Scoreboard.ScoreType.AllTimeHigh) {
            canvas.drawText(NewHighScore, (MainView.ViewWidth - mPaint.measureText(NewHighScore)) / 2, contentMargin - commentFontSize * 2 + contentTranslation, mPaint);
        } else if (mGameInfo.getScoreType() == Scoreboard.ScoreType.TodayHigh) {
            canvas.drawText(TodayHighScore, (MainView.ViewWidth - mPaint.measureText(TodayHighScore)) / 2, contentMargin - commentFontSize * 2 + contentTranslation, mPaint);
        }

        mPaint.setColor(Color.argb(Alpha, 255, 255, 255));

        mPaint.setTextSize(commentFontSize);
        canvas.drawText(YourScore, (MainView.ViewWidth - mPaint.measureText(YourScore)) / 2, contentMargin + commentFontSize + contentTranslation, mPaint);

        mPaint.setTextSize(scoreFontSize);
        canvas.drawText(Math.round(Overlord.Points) + "", (MainView.ViewWidth - mPaint.measureText(Math.round(Overlord.Points) + "")) / 2, contentMargin + scoreFontSize + commentFontSize * 1.1f + contentTranslation, mPaint);

        drawAdditionalInfo(canvas);


        if (!gameFinished)
            EndButton.render(canvas);


        switch (Overlord.CurrentMode) {
            case TimeLimited:
                renderTimeLimited(canvas);
                break;

            case MoveLimited:
                renderMoveLimited(canvas);
                break;

            case Infinite:
                renderInfinite(canvas);
                break;
        }
    }

    public void renderTimeLimited(Canvas canvas) {
        float progress = (Overlord.DURATION * Overlord.ModeMultiplier - Overlord.Time) / (float)(Overlord.DURATION * Overlord.ModeMultiplier);

        if (!gameFinished)
            drawProgressBar(canvas, progress);
    }

    public void renderMoveLimited(Canvas canvas) {
        float progress = (Overlord.MAX_MOVES * Overlord.ModeMultiplier - Overlord.Moves) / (float)(Overlord.MAX_MOVES * Overlord.ModeMultiplier);

        if (!gameFinished)
            drawProgressBar(canvas, progress);
    }

    public void renderInfinite(Canvas canvas) {

    }

    void drawAdditionalInfo(Canvas canvas) {
        mPaint.setTextSize(commentFontSize);
        String moves = String.format(Moves, Overlord.Moves), time = String.format(Time, timeToString((int)(Overlord.Time / 1000)));

        if (!gameFinished) {
            if (Overlord.CurrentMode == Overlord.GameMode.MoveLimited) {
                moves = String.format(MovesLeft, Overlord.MAX_MOVES * Overlord.ModeMultiplier - Overlord.Moves);
            } else if (Overlord.CurrentMode == Overlord.GameMode.TimeLimited) {
                time = String.format(TimeLeft, timeToString((int)(Overlord.DURATION * Overlord.ModeMultiplier - Overlord.Time) / 1000));
            }
        }

        float progress = contentTranslation / targetEndTranslation,
                movesX = MainView.ViewWidth * 0.05f,
                movesY = (contentMargin - commentFontSize) / 2 + commentFontSize * 1.25f + contentTranslation,
                timeX = MainView.ViewWidth * 0.95f - mPaint.measureText(time),
                timeY = (contentMargin - commentFontSize) / 2 + commentFontSize * 1.25f + contentTranslation;

        if (gameFinished) {
            if (progress > 1 || currentAnimation == AnimationType.HideUI)
                progress = 1;

            movesX += ((MainView.ViewWidth - mPaint.measureText(moves)) / 2 - movesX) * progress;
            timeX += ((MainView.ViewWidth - mPaint.measureText(time)) / 2 - timeX) * progress;

            movesY += (contentMargin + scoreFontSize + commentFontSize * 3.5f + contentTranslation - movesY) * progress;
            timeY += (contentMargin + scoreFontSize + commentFontSize * 5.5f + contentTranslation - timeY) * progress;
        }

        canvas.drawText(moves, movesX, movesY, mPaint);
        canvas.drawText(time, timeX, timeY, mPaint);
    }

    void drawProgressBar(Canvas canvas, float progress) {
        int y = (int)(contentMargin + scoreFontSize * 1.75f + commentFontSize + contentTranslation);

        int height = MainView.ViewHeight / 400; if (height < 3) height = 3;
        int particleSize = MainView.ViewHeight / 250; if (particleSize < 4) particleSize = 4;

        canvas.drawRect(new RectF(0, y, progress * MainView.ViewWidth, y + height), mPaint);

        if (Overlord.CurrentMode == Overlord.GameMode.TimeLimited && progress > 0) {
            MainView.ParticleSystem.createInPoint(new PointF(progress * MainView.ViewWidth, y), particleSize, 1, 300, 300, 300);
        } else if (Overlord.CurrentMode == Overlord.GameMode.MoveLimited && previousMoves != Overlord.Moves) {
            float previousProgress = (Overlord.MAX_MOVES * Overlord.ModeMultiplier - previousMoves) / (float)(Overlord.MAX_MOVES * Overlord.ModeMultiplier);
            MainView.ParticleSystem.createInArea(new RectF(progress * MainView.ViewWidth, y, previousProgress * MainView.ViewWidth, y + height), particleSize, 20, 300, 300, 300);

            previousMoves = Overlord.Moves;
        }
    }

    String timeToString(int t) {
        StringBuilder builder = new StringBuilder();

        int hours = (int)(t / 3600);
        t -= hours * 3600;
        if (hours > 0)
            builder.append(hours + "h ");

        int minutes = (int)(t / 60);
        t -= minutes * 60;
        if (minutes > 0)
            builder.append(minutes + "m ");

        builder.append(t + "s");

        return builder.toString();
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (!gameFinished)
            EndButton.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TouchDown = true;
                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
                if (ReadyForGame && TouchDown && currentAnimation != AnimationType.HideUI) {
                    currentAnimation = AnimationType.Hide;
                }
                break;
        }
    }

    public void show() {
        ReadyForGame = TouchDown = gameFinished = false;
        currentAnimation = AnimationType.Show;
        contentTranslation = Height / 2;
        scale = 1 + ((Overlord.CurrentMode == Overlord.GameMode.Infinite || Overlord.CurrentMode == Overlord.GameMode.UntilPointless) ? 0.5f : 0);
        Alpha = previousMoves = 0;

        EndButton.reset();
    }

    public void hide() {
        currentAnimation = AnimationType.HideUI;
    }

    public void back() {
        currentAnimation = AnimationType.Hide;
    }

    public void onGameFinished() {
        gameFinished = true;
        currentAnimation = AnimationType.Grow;

        MainView.Achievements.onGameFinished();
    }

    public void finishGame() {
        mGameInfo.finishGame();
    }

    public boolean isAnimating() {
        return currentAnimation != AnimationType.None;
    }

    void updateAnimations() {
        switch (currentAnimation) {
            case Show:
                contentTranslation *= 0.9f;
                Alpha += 15;

                if (Alpha > 255) Alpha = 255;

                if (contentTranslation < 4) {
                    contentTranslation = 0;
                    Alpha = 255;

                    currentAnimation = AnimationType.None;
                }
                break;

            case Grow:
                contentTranslation += (targetEndTranslation - contentTranslation + 3) * 0.1f  * (MainView.FrameTime / 16f);
                scale += (2 - scale) * 0.07f * (MainView.FrameTime / 16f);

                if (mGameInfo.getScoreType() != Scoreboard.ScoreType.Default) {
                    highscoreAlpha += 10;
                    if (highscoreAlpha > 255)
                        highscoreAlpha = 255;
                }

                if (contentTranslation > targetEndTranslation * 0.9)
                    ReadyForGame = true;

                if (Alpha + 20 <= 255)
                    Alpha += 20;

                if (contentTranslation > targetEndTranslation - 0.5) {
                    Alpha = 255;
                    contentTranslation = targetEndTranslation;
                    scale = 2;

                    currentAnimation = AnimationType.None;
                }
                break;

            case Hide:
                contentTranslation += (Height * 2 - contentTranslation) * 0.07f;
                Alpha -= 15;

                if (mGameInfo.getScoreType() != Scoreboard.ScoreType.Default)
                    highscoreAlpha = Alpha;

                if (Alpha < 0) {
                    Alpha = highscoreAlpha = 0;
                    currentAnimation = AnimationType.None;

                    MainView.NextView = MainView.ViewType.Game;
                    mGameInfo.onUIHidden();
                }
                break;

            case HideUI:
                contentTranslation -= (Height + contentTranslation) * 0.07f;
                Alpha -= 18;

                if (Alpha < 0) {
                    Alpha = 0;
                    currentAnimation = AnimationType.None;

                    mGameInfo.onUIHidden();
                }
                break;
        }
    }

    public void load(Context context) {
        mContext = context;

        EndButton.load(context);

        YourScore = context.getString(R.string.results_yourscore);
        Time = context.getString(R.string.results_time);
        Moves = context.getString(R.string.results_moves);
        TimeLeft = context.getString(R.string.results_timeleft);
        MovesLeft = context.getString(R.string.results_movesleft);
        NewHighScore = context.getString(R.string.results_newhigh);
        TodayHighScore = context.getString(R.string.results_today_high);
    }
}
