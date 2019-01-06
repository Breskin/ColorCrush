package com.github.breskin.squares.game.context;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.github.breskin.squares.game.Board;
import com.github.breskin.squares.game.Overlord;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;

/**
 * Created by Kuba on 12.11.2018.
 */

public class ModePicker {

    private enum AnimationType { None, Initial, PreviousMode, NextMode, Show, ToResults, Hide }

    private ContextualInfo mGameInfo;

    private Bitmap arrowDown, arrowUp;

    private AnimationType currentAnimation = AnimationType.None;

    private PointF arrowSize, touchStart, touchPrevious;
    private Paint mPaint;
    private float Height = -1, nameFontSize = -1, descFontSize = -1, contentMargin = -1, arrowTranslation = 0, contentTranslation = 0, multiplierRadius = 0, multiplierMargin = 0, multiplierHeight = 0, multiplierMarker = 0;
    private int arrowAlpha, contentAlpha;
    private boolean playInitialWhenReady = false, multiplierTouch = false;

    public ModePicker(ContextualInfo gameInfo) {
        mGameInfo = gameInfo;

        arrowSize = new PointF(0, 0);
        touchStart = new PointF(-1, -1);
        touchPrevious = new PointF(0, 0);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void update() {
        arrowSize.x = MainView.ViewWidth * 0.1f;
        arrowSize.y = arrowDown.getHeight() * arrowSize.x / arrowDown.getWidth();
        Height = Board.getTopMargin();

        nameFontSize = fitFontSize(Overlord.CurrentMode.getName(), MainView.ViewWidth / 12, MainView.ViewWidth * 4 / 5);
        descFontSize = fitFontSize(Overlord.CurrentMode.getDescription(), MainView.ViewWidth / 20, MainView.ViewWidth * 4 / 5);
        multiplierRadius = MainView.ViewWidth * 0.01f;

        contentMargin = (Height - nameFontSize - descFontSize - Height / 8 - multiplierRadius * 3) / 2;

        multiplierMargin = contentMargin + nameFontSize + descFontSize + Height / 8 + multiplierRadius * 3 + contentTranslation;
        multiplierHeight = (Height * 5 / 6 - arrowSize.y - (contentMargin + nameFontSize + descFontSize + Height / 18)) / 2;

        if (playInitialWhenReady) {
            currentAnimation = AnimationType.Initial;
            arrowAlpha = contentAlpha = 0;
            arrowTranslation = contentTranslation = -Height;
            playInitialWhenReady = false;
        }

        updateAnimations();
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Color.argb(contentAlpha, 255, 255, 255));

        mPaint.setTextSize(nameFontSize);
        canvas.drawText(Overlord.CurrentMode.getName(), (MainView.ViewWidth - mPaint.measureText(Overlord.CurrentMode.getName())) / 2, contentMargin + nameFontSize + contentTranslation, mPaint);

        mPaint.setTextSize(descFontSize);
        canvas.drawText(Overlord.CurrentMode.getDescription(), (MainView.ViewWidth - mPaint.measureText(Overlord.CurrentMode.getDescription())) / 2, contentMargin + nameFontSize + descFontSize + Height / 18 + contentTranslation, mPaint);

        if (isMultiplierVisible())
            drawMultiplierInfo(canvas);

        mPaint.setColor(Color.argb(arrowAlpha, 255, 255, 255));
        canvas.drawBitmap(arrowUp, new Rect(0, 0, arrowUp.getWidth(), arrowUp.getHeight()), new RectF((MainView.ViewWidth - arrowSize.x) / 2, Height / 12 + arrowTranslation, (MainView.ViewWidth - arrowSize.x) / 2 + arrowSize.x, Height / 12 + arrowSize.y + arrowTranslation), mPaint);
        canvas.drawBitmap(arrowDown, new Rect(0, 0, arrowDown.getWidth(), arrowDown.getHeight()), new RectF((MainView.ViewWidth - arrowSize.x) / 2, Height * 11 / 12 - arrowSize.y + arrowTranslation, (MainView.ViewWidth - arrowSize.x) / 2 + arrowSize.x, Height * 11 / 12 + arrowTranslation), mPaint);
    }

    void drawMultiplierInfo(Canvas canvas) {
        float x = MainView.ViewWidth / 10;

        for (int i = 0; i < 5; i++) {
            float distance = (i - multiplierMarker + 1);

            if (distance <= 0)
                mPaint.setColor(Color.argb(contentAlpha, 255, 255, 255));
            else if (distance < 1){
                int color = (int)((1 - distance) *207) + 48;
                mPaint.setColor(Color.argb(contentAlpha, color, color, color));
            } else {
                mPaint.setColor(Color.argb(contentAlpha, 48, 48, 48));
            }

            canvas.drawCircle(x + MainView.ViewWidth * i / 5, multiplierMargin, multiplierRadius, mPaint);
        }

        mPaint.setColor(Color.argb(contentAlpha, 255, 255, 255));
        canvas.drawRect(new RectF(x, multiplierMargin - multiplierRadius / 4, x + MainView.ViewWidth / 5 * (multiplierMarker - 1), multiplierMargin + multiplierRadius / 4), mPaint);
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart.x = x;
                touchStart.y = y;

                if (isMultiplierVisible() && y > multiplierMargin - multiplierHeight / 2 && y < multiplierMargin + multiplierHeight / 2)
                    multiplierTouch = true;

                break;

            case MotionEvent.ACTION_MOVE:
                float diff = touchStart.y - y;

                if (Math.abs(diff) > MainView.ViewHeight * 0.07f && currentAnimation == AnimationType.None && !multiplierTouch) {
                    if (diff < 0)
                        currentAnimation = AnimationType.NextMode;
                    else
                        currentAnimation = AnimationType.PreviousMode;

                    touchStart.x = x;
                    touchStart.y = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (x == touchStart.x && y == touchStart.y && currentAnimation == AnimationType.None) {
                    if (y > 0 && y < Height / 6 + arrowSize.y)
                        currentAnimation = AnimationType.PreviousMode;
                    else if (y > Height * 5 / 6 - arrowSize.y && y < Height)
                        currentAnimation = AnimationType.NextMode;
                }

                multiplierTouch = false;
                touchStart.x = touchStart.y = -1;
                break;
        }

        if (multiplierTouch) {
            Overlord.ModeMultiplier = Math.round((x + MainView.ViewWidth / 10) / (MainView.ViewWidth / 5));
            if (Overlord.ModeMultiplier > 5)
                Overlord.ModeMultiplier = 5;
            if (Overlord.ModeMultiplier < 0)
                Overlord.ModeMultiplier = 0;
        }

        touchPrevious.x = x;
        touchPrevious.y = y;
    }

    public void onGameStarted() {
        currentAnimation = AnimationType.ToResults;
    }

    public void playInitialAnimation() {
        playInitialWhenReady = true;
        multiplierMarker = Overlord.ModeMultiplier;
    }

    public void hide() {
        currentAnimation = AnimationType.Hide;
    }

    void updateAnimations() {
        switch (currentAnimation) {
            case Initial:
                contentAlpha += 10;
                if (contentAlpha > 255)
                    contentAlpha = 255;

                arrowAlpha = contentAlpha;

                contentTranslation *= 0.9f;
                arrowTranslation = contentTranslation;

                if (contentTranslation > -4) {
                    contentTranslation = arrowTranslation = 0;
                    contentAlpha = arrowAlpha = 255;

                    currentAnimation = AnimationType.None;
                }
                break;

            case PreviousMode:
                contentTranslation -= (Height / 2 + contentTranslation) * 0.12f;
                contentAlpha -= 22;

                if (contentAlpha < 0) {
                    contentAlpha = 0;
                    contentTranslation *= -1;
                    currentAnimation = AnimationType.Show;

                    Overlord.CurrentMode = Overlord.CurrentMode.previous();
                }
                break;

            case NextMode:
                contentTranslation += (Height / 2 - contentTranslation) * 0.12f;
                contentAlpha -= 22;

                if (contentAlpha < 0) {
                    contentAlpha = 0;
                    contentTranslation *= -1;
                    currentAnimation = AnimationType.Show;

                    Overlord.CurrentMode = Overlord.CurrentMode.next();
                }
                break;

            case Show:
                contentTranslation -= contentTranslation * 0.2f;
                contentAlpha += 20;

                if (contentAlpha > 255) contentAlpha = 255;

                if (Math.abs(contentTranslation) < 4) {
                    contentTranslation = 0;
                    contentAlpha = 255;

                    currentAnimation = AnimationType.None;
                }
                break;

            case ToResults: case Hide:
                contentAlpha -= 13;
                arrowAlpha = contentAlpha;

                contentTranslation -= (Height / 1.5f + contentTranslation) * 0.1f;
                arrowTranslation = contentTranslation;

                if (contentAlpha < 0) {
                    if (currentAnimation == AnimationType.ToResults)
                        mGameInfo.showResults();
                    else
                        mGameInfo.onUIHidden();

                    contentAlpha = arrowAlpha = 0;
                    currentAnimation = AnimationType.None;
                }
                break;
        }

        if (multiplierMarker != Overlord.ModeMultiplier) {
            multiplierMarker += (Overlord.ModeMultiplier - multiplierMarker + 0.02f) * 0.12f;

            if (Math.abs(multiplierMarker - Overlord.ModeMultiplier) < 0.02f)
                multiplierMarker = Overlord.ModeMultiplier;
        }
    }

    boolean isMultiplierVisible() {
        return Overlord.CurrentMode == Overlord.GameMode.MoveLimited || Overlord.CurrentMode == Overlord.GameMode.TimeLimited;
    }

    public void load(Context context) {
        arrowDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.down);
        arrowUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.up);
    }

    public boolean isAnimating() {
        return currentAnimation != AnimationType.None;
    }

    float fitFontSize(String text, float desiredFontSize, int maxWidth) {
        float save = mPaint.getTextSize();
        mPaint.setTextSize(desiredFontSize);

        while (mPaint.measureText(text) > maxWidth)
            mPaint.setTextSize(mPaint.getTextSize() - 1);

        float result = mPaint.getTextSize();
        mPaint.setTextSize(save);

        return  result;
    }
}
