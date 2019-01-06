package com.github.breskin.squares.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.Logo;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.R;

/**
 * Created by Kuba on 16.11.2018.
 */

public class Information {

    private enum AnimationType { None, Show, Hide }

    private MainView mMainView;
    private TextPaint mPaint;
    private AnimationType currentAnimation = AnimationType.None;

    private Bitmap cycle, play;

    private int Alpha = 0;
    private boolean touchDown = false, touchMoved = false, touchPlay = false;
    private float animationTranslation = 0, translation, touchDifference, contentSize, playMargin;

    private PointF previousTouch;
    private String cycleInfo, tips;
    private StaticLayout cycleInfoLayout, tipsLayout;

    public Information(MainView mv) {
        mMainView = mv;

        mPaint = new TextPaint();
        //mPaint.setAntiAlias(true);

        previousTouch = new PointF(0, 0);
    }

    public void update() {
        if (currentAnimation == AnimationType.Show && animationTranslation == -1)
            animationTranslation = MainView.ViewHeight / 2;

        updateAnimations();

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

        if (contentSize > MainView.ViewHeight - Logo.Size.y) {
            if (-translation + MainView.ViewHeight  - Logo.Size.y > contentSize)
                translation = -contentSize + MainView.ViewHeight  - Logo.Size.y;
        } else
            translation = 0;
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Color.argb(Alpha, 255, 255, 255));
        mPaint.setTextSize(MainView.ViewWidth / 20);

        float margin = MainView.ViewHeight * 0.07f + translation;

        if (cycleInfoLayout == null || currentAnimation != AnimationType.None) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                cycleInfoLayout = StaticLayout.Builder
                        .obtain(cycleInfo, 0, cycleInfo.length(), mPaint, canvas.getWidth() * 9 / 10)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER).setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
                        .build();

                tipsLayout = StaticLayout.Builder
                        .obtain(tips, 0, tips.length(), mPaint, canvas.getWidth() * 9 / 10)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER).setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
                        .build();
            } else {
                cycleInfoLayout = new StaticLayout(cycleInfo, mPaint, canvas.getWidth() * 9 / 10, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
                tipsLayout = new StaticLayout(tips, mPaint, canvas.getWidth() * 9 / 10, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
            }
        }

        canvas.save();
        canvas.translate(MainView.ViewWidth / 20, margin + animationTranslation);
        cycleInfoLayout.draw(canvas);
        canvas.restore();

        margin += cycleInfoLayout.getHeight() + mPaint.getTextSize();

        int x = MainView.ViewWidth * 7 / 10;
        int y = cycle.getHeight() * x / cycle.getWidth();

        canvas.drawBitmap(cycle, new Rect(0, 0, cycle.getWidth(), cycle.getHeight()), new RectF(MainView.ViewWidth * 3 / 20, margin + animationTranslation, MainView.ViewWidth * 3 / 20 + x, y + margin + animationTranslation), mPaint);

        margin += y + mPaint.getTextSize();

        canvas.save();
        canvas.translate(MainView.ViewWidth / 20, margin + animationTranslation);
        tipsLayout.draw(canvas);
        canvas.restore();

        margin += tipsLayout.getHeight() * 1.5f;

        y = MainView.ViewHeight / 10;
        x = play.getWidth() * y / play.getHeight();
        playMargin = margin + animationTranslation;
        canvas.drawBitmap(play, new Rect(0, 0, play.getWidth(), play.getHeight()), new RectF((MainView.ViewWidth - x * 0.5f) / 2, playMargin, (MainView.ViewWidth - x * 0.5f) / 2 + x, playMargin + y), mPaint);

        contentSize = margin + y - translation;
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDifference = 0;
                touchDown = true;

                touchPlay = (y > playMargin && y < playMargin + MainView.ViewHeight / 10 && x > (MainView.ViewWidth - MainView.ViewHeight / 5) / 2 && x < (MainView.ViewWidth - MainView.ViewHeight / 5) / 2 + MainView.ViewHeight / 5);
                break;

            case MotionEvent.ACTION_MOVE:
                touchDifference = (touchDifference + (y - previousTouch.y)) / 2;
                touchMoved = true;
                break;

            case MotionEvent.ACTION_UP:
                if (touchPlay)
                    onBackPressed();

                translation -= touchDifference;
                touchDown = touchPlay = false;
                break;
        }

        previousTouch.x = x;
        previousTouch.y = y;
    }

    public boolean onBackPressed() {
        MainView.NextView = MainView.ViewType.Game;
        hide();

        mMainView.Logo.addToQueue(Logo.LogoState.CenteredWrappingAnimation);
        mMainView.Logo.addToQueue(Logo.LogoState.CenterToLeftAnimation);
        mMainView.Logo.addToQueue(Logo.LogoState.Left);

        if (!mMainView.Logo.isAnimating())
            mMainView.Logo.startQueue();

        return true;
    }

    public void hide() {
        currentAnimation = AnimationType.Hide;
    }

    public void show() {
        currentAnimation = AnimationType.Show;
        Alpha = 0;
        animationTranslation = MainView.ViewHeight / 2 - 1;
        translation = touchDifference = 0;
    }

    void updateAnimations() {
        switch (currentAnimation) {
            case Show:
                animationTranslation -= (animationTranslation + MainView.ViewHeight / 200) * 0.12f;
                Alpha += 20;

                if (Alpha > 255)
                    Alpha = 255;

                if (animationTranslation < 0.5f) {
                    animationTranslation = 0;
                    Alpha = 255;
                    currentAnimation = AnimationType.None;
                }
                break;

            case Hide:
                animationTranslation += (MainView.ViewHeight / 2 - animationTranslation + MainView.ViewHeight / 200) * 0.1f;
                Alpha -= 15;

                if (Alpha < 0) {
                    Alpha = 0;
                    currentAnimation = AnimationType.None;

                    mMainView.nextView();
                }
                break;
        }
    }

    public void load(Context context) {
        cycle = BitmapFactory.decodeResource(context.getResources(), R.drawable.cycle);
        play = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);

        cycleInfo = context.getString(R.string.info_cycle).replace("{n}", System.getProperty("line.separator"));
        tips = context.getString(R.string.info_tips).replace("{n}", System.getProperty("line.separator"));
    }
}
