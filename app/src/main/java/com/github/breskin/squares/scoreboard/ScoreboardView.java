package com.github.breskin.squares.scoreboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.github.breskin.squares.DataManager;
import com.github.breskin.squares.MainActivity;
import com.github.breskin.squares.R;
import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.View;

public class ScoreboardView implements View {

    private RenderView renderView;

    private Paint paint;

    private boolean closing = false, touchDown = false, touchMoved = false;
    private float alpha = 0, animationTranslation = 0, translation = 0, headlineFontSize, categoryFontSize, contentFontSize, touchDifference, contentSize;

    private PointF previousTouch;

    private String infinite, timeLimited, moveLimited, untilPointless, bestScores, today, allTime, gamesPlayed;

    public ScoreboardView(RenderView renderView) {
        this.renderView = renderView;

        paint = new Paint();
        paint.setAntiAlias(true);

        previousTouch = new PointF(0, 0);
    }

    @Override
    public void update() {
        headlineFontSize = MainActivity.fitFontSize(paint, bestScores, RenderView.ViewWidth / 10, RenderView.ViewWidth * 4 / 5);
        categoryFontSize = MainActivity.fitFontSize(paint, allTime, RenderView.ViewWidth / 15, RenderView.ViewWidth * 4 / 5);

        float timeLimitedFont = MainActivity.fitFontSize(paint, timeLimited, RenderView.ViewWidth / 20, (int)(RenderView.ViewWidth * 0.65f));
        float moveLimitedFont = MainActivity.fitFontSize(paint, moveLimited, RenderView.ViewWidth / 20, (int)(RenderView.ViewWidth * 0.65f));

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

        if (contentSize > RenderView.ViewHeight) {
            if (-translation + RenderView.ViewHeight > contentSize)
                translation = -contentSize + RenderView.ViewHeight;
        } else
            translation = 0;

        if (closing) {
            animationTranslation += (RenderView.ViewHeight * 0.5f - animationTranslation) * 0.1f;
            alpha += (0 - alpha) * 0.1f;

            if (alpha < 0.075f)
                renderView.switchView(RenderView.ViewType.Game);
        } else {
            animationTranslation += (0 - animationTranslation) * 0.1f * (RenderView.FrameTime / 16f);
            alpha += (1 - alpha) * 0.1f;
        }
    }

    @Override
    public void render(Canvas canvas) {
        paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));

        float margin = RenderView.ViewHeight * 0.1f;

        paint.setTextSize(headlineFontSize);
        canvas.drawText(bestScores, (RenderView.ViewWidth - paint.measureText(bestScores)) / 2, margin + translation + animationTranslation + headlineFontSize, paint);

        margin += headlineFontSize + RenderView.ViewHeight * 0.05f;

        String gamesPlayedCount = String.format(gamesPlayed, DataManager.getGamesPlayed());
        paint.setTextSize(categoryFontSize * 0.75f);
        canvas.drawText(gamesPlayedCount, (RenderView.ViewWidth - paint.measureText(gamesPlayedCount)) / 2, margin + translation + animationTranslation + contentFontSize, paint);
        margin += categoryFontSize * 0.75f + RenderView.ViewHeight * 0.07f;

        paint.setTextSize(categoryFontSize);
        canvas.drawText(today, (RenderView.ViewWidth - paint.measureText(today)) / 2, margin + translation + animationTranslation + contentFontSize, paint);
        margin += categoryFontSize * 1.75f;
        margin = drawRecords(canvas, margin, DataManager.getTodayEndlessMaxScore(), DataManager.getTodayConstantMaxScore(), DataManager.getTodayTimeLimitedMaxScore(), DataManager.getTodayMoveLimitedMaxScore()) + categoryFontSize;

        paint.setTextSize(categoryFontSize);
        canvas.drawText(allTime, (RenderView.ViewWidth - paint.measureText(today)) / 2, margin + translation + animationTranslation + contentFontSize, paint);
        margin += categoryFontSize * 1.75f;
        margin = drawRecords(canvas, margin, DataManager.getEndlessMaxScore(), DataManager.getConstantMaxScore(), DataManager.getTimeLimitedMaxScore(), DataManager.getMoveLimitedMaxScore()) + RenderView.ViewHeight * 0.1f;

        contentSize = margin;
    }

    float drawRecords(Canvas canvas, float margin, int infiniteMaxScore, int untilPointlessMaxScore, int timeLimitedMaxScore[], int moveLimitedMaxScore[]) {
        paint.setTextSize(contentFontSize);
        canvas.drawText(infinite + ":", RenderView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, paint);
        canvas.drawText(infiniteMaxScore+"", RenderView.ViewWidth * 0.95f - paint.measureText(infiniteMaxScore + ""), margin + translation + animationTranslation + contentFontSize, paint);

        margin += contentFontSize * 2.5f;

        paint.setTextSize(contentFontSize);
        canvas.drawText(untilPointless + ":", RenderView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, paint);
        canvas.drawText(untilPointlessMaxScore+"", RenderView.ViewWidth * 0.95f - paint.measureText(untilPointlessMaxScore + ""), margin + translation + animationTranslation + contentFontSize, paint);

        margin += contentFontSize * 2.5f;

        canvas.drawText(timeLimited + ":", RenderView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, paint);
        margin += contentFontSize * 1.75f;

        for (int i=0; i<5; i++) {
            drawRecordMultiplier(canvas, margin, i);
            paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
            canvas.drawText(timeLimitedMaxScore[i]+"", RenderView.ViewWidth * 0.95f - paint.measureText(timeLimitedMaxScore[i] + ""), margin + translation + animationTranslation + contentFontSize, paint);

            margin += contentFontSize * 1.75f;
        }

        margin += contentFontSize * 1.25f;


        canvas.drawText(moveLimited + ":", RenderView.ViewWidth * 0.05f, margin + translation + animationTranslation + contentFontSize, paint);
        margin += contentFontSize * 1.75f;

        for (int i=0; i<5; i++) {
            drawRecordMultiplier(canvas, margin, i);
            paint.setColor(Color.argb((int)(255 * alpha), 255, 255, 255));
            canvas.drawText(moveLimitedMaxScore[i]+"", RenderView.ViewWidth * 0.95f - paint.measureText(moveLimitedMaxScore[i] + ""), margin + translation + animationTranslation + contentFontSize, paint);

            margin += contentFontSize * 1.75f;
        }

        margin += contentFontSize * 1.25f;

        return margin;
    }

    void drawRecordMultiplier(Canvas canvas, float margin, int multiplier) {
        float width = RenderView.ViewWidth * 0.12f, top = (int)(margin + translation + animationTranslation + contentFontSize * 0.7f);

        canvas.drawRect(new RectF(RenderView.ViewWidth * 0.1f, top - RenderView.ViewWidth * 0.002f, RenderView.ViewWidth * 0.1f + width * multiplier, top + RenderView.ViewWidth * 0.002f), paint);

        for (int i = 0; i < 5; i++) {
            canvas.drawCircle(RenderView.ViewWidth * 0.1f + width * i, top, RenderView.ViewWidth * 0.01f, paint);

            if (i >= multiplier)
                paint.setColor(Color.argb((int)(255 * alpha), 64, 64, 64));
        }

        if (multiplier < 4)
            canvas.drawRect(new RectF(RenderView.ViewWidth * 0.05f, (int)(margin + translation + animationTranslation + contentFontSize * 1.5f), RenderView.ViewWidth * 0.95f, (int)(margin + translation + animationTranslation + contentFontSize * 1.5f + RenderView.ViewWidth * 0.004f)), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
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
        
        return false;
    }

    @Override
    public boolean onBackPressed() {
        closing = true;

        return true;
    }

    @Override
    public void open() {
        closing = false;

        translation = touchDifference = alpha = 0;
        animationTranslation = RenderView.ViewHeight / 3;
        touchDown = false;
    }

    public void load(Context context) {
        bestScores = context.getString(R.string.scoreboard_best_scores);
        infinite = context.getString(R.string.mode_endless);
        timeLimited = context.getString(R.string.mode_timelimited);
        moveLimited = context.getString(R.string.mode_movelimited);
        untilPointless = context.getString(R.string.mode_constant);
        today = context.getString(R.string.scoreboard_today);
        allTime = context.getString(R.string.scoreboard_alltime);
        gamesPlayed = context.getString(R.string.games_played);
    }
}
