package com.github.breskin.squares;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by Kuba on 10.11.2018.
 */

public class Logo {

    public enum LogoState { None, InitialAnimation, CenterToLeftAnimation, Left, LeftToCenterAnimation, CenteredWrapped, CenteredWrappingAnimation, CenteredUnwrapped, CenteredUnwrappingAnimation }

    public static Point Size = new Point();

    private MainView mMainView;

    private Bitmap logoBitmap;
    private Point position;
    private Paint mPaint;

    private int frame = 0, frameHeight = 0, progress, maxProgress = 500;
    private float margin = 0, moveSpeed = 0;
    private boolean touchDown = false, linkTouchDown = false;
    private long time = 0;

    private Queue<LogoState> Queue = new ArrayDeque<>();

    private LogoState CurrentState = LogoState.None;

    public Logo(MainView mv) {
        mMainView = mv;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        position = new Point(0, 0);
    }

    public void update() {
        Size.x = MainView.ViewWidth * 5 / 8;
        Size.y = (frameHeight * Size.x / (logoBitmap.getWidth() / 4)) * 2;
        position.x = (int)((MainView.ViewWidth - Size.x) / 2f + margin);
        position.y = MainView.ViewHeight - Size.y * 5 / 8;

        if (touchDown)
            progress += System.currentTimeMillis() - time;
        time = System.currentTimeMillis();

        if (MainView.CurrentView == MainView.ViewType.Info)
            progress = 0;

        if (progress > maxProgress) {
            mMainView.hideGameUI();
            MainView.NextView = MainView.ViewType.Info;

            progress = 0;
            touchDown = false;

            addToQueue(LogoState.LeftToCenterAnimation);
            addToQueue(LogoState.CenteredUnwrappingAnimation);
            addToQueue(LogoState.CenteredUnwrapped);

            if (!isAnimating())
                startQueue();
        }

        updateAnimations();
    }

    public void render(Canvas canvas) {
        int row = (int)(frame / 4f), column = frame - row * 4;

        if (MainView.CurrentView == MainView.ViewType.Info) {
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(new RectF(0, MainView.ViewHeight - Size.y * 0.75f, MainView.ViewWidth, MainView.ViewHeight), mPaint);
        }

        canvas.drawBitmap(logoBitmap, new Rect(column * logoBitmap.getWidth() / 4 + 1, frameHeight * row, (column + 1) * logoBitmap.getWidth() / 4, frameHeight * (row + 1)), new Rect(position.x, position.y, position.x + Size.x, position.y + Size.y / 2), null);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(MainView.ViewWidth / 100);
        canvas.drawArc(-Size.y, MainView.ViewHeight - Size.y, Size.y, MainView.ViewHeight + Size.y, 270, 90f * progress / maxProgress, false, mPaint);
    }

    public void playInitialAnimation() {
        CurrentState = LogoState.InitialAnimation;

        Queue.clear();
        Queue.offer(LogoState.CenterToLeftAnimation);
        Queue.offer(LogoState.Left);

        frame = 0;
        margin = 0;
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (CurrentState == LogoState.Left && x < Size.x / 5 &&  y > MainView.ViewHeight - Size.y) {
                    touchDown = true;
                }

                linkTouchDown = CurrentState == LogoState.CenteredUnwrapped && y > MainView.ViewHeight - Size.y * 0.75f;

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:
                if (linkTouchDown)
                    mMainView.openBrowser();

                touchDown = linkTouchDown = false;
                progress = 0;
                break;
        }
    }

    void updateAnimations() {
        switch (CurrentState) {
            case InitialAnimation:
                frame++;
                if (frame >= 140) {
                    CurrentState = nextState();
                    moveSpeed = MainView.ViewWidth / 90;
                }
                break;

            case CenterToLeftAnimation:
                if (-margin < MainView.ViewWidth / 2.3f) {
                    margin -= (MainView.ViewWidth / 2.3f + margin + MainView.ViewWidth / 75) * 0.07f;

                    if (MainView.ViewWidth / 2.3f + margin < 0.5f)
                        margin = -MainView.ViewWidth / 2.3f;
                } else {
                    CurrentState = nextState();
                }
                break;

            case LeftToCenterAnimation:
                if (margin < 0) {
                    margin += (-margin + MainView.ViewWidth / 75) * 0.07f;

                    if (margin > -0.5f)
                        margin = 0;
                } else {
                    CurrentState = nextState();
                }
                break;

            case CenteredUnwrappingAnimation:
                frame++;
                if  (frame >= 193)
                    CurrentState = nextState();
                break;

            case CenteredWrappingAnimation:
                frame--;
                if  (frame <= 140)
                    CurrentState = nextState();
                break;
        }


        if (!isAnimating()) {
            LogoState next = nextState();
            if (next != LogoState.None)
                CurrentState = next;
        }
    }

    public boolean isAnimating() {
        return !(CurrentState == LogoState.None || CurrentState == LogoState.CenteredUnwrapped || CurrentState == LogoState.CenteredWrapped || CurrentState == LogoState.Left);
    }

    public void clearQueue() {
        Queue.clear();
    }

    public void addToQueue(LogoState s) {
        Queue.offer(s);
    }

    public void startQueue() {
        CurrentState = nextState();
    }

    public Point getPosition() {
        return position;
    }

    LogoState nextState() {
        if (Queue.isEmpty())
            return LogoState.None;

        LogoState next = Queue.poll();

        switch (next) {
            case LeftToCenterAnimation:
                frame = 140;
                margin = -MainView.ViewWidth * 2 / 5;
                moveSpeed = MainView.ViewWidth / 90;
                break;

            case CenterToLeftAnimation:
                frame = 140;
                margin = 0;
                moveSpeed = MainView.ViewWidth / 90;
                break;

            case CenteredUnwrappingAnimation:
                frame = 140;
                margin = 0;
                break;

            case CenteredWrappingAnimation:
                frame = 193;
                margin = 0;
                break;
        }

        return next;
    }

    public void load(Context context) {
        logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.breskin);
        frameHeight = logoBitmap.getHeight() / 51;
    }
}
