package com.github.breskin.squares;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.github.breskin.squares.achievements.Achievements;
import com.github.breskin.squares.game.Board;
import com.github.breskin.squares.game.Overlord;
import com.github.breskin.squares.info.Information;
import com.github.breskin.squares.particles.ParticleSystem;

/**
 * Created by Kuba on 10.11.2018.
 */

public class MainView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    public enum ViewType { None, Game, Scoreboard, Info }

    public static int ViewHeight, ViewWidth, FrameTime;
    public static ParticleSystem ParticleSystem;
    public static Achievements Achievements;

    public static ViewType CurrentView = ViewType.Game;
    public static ViewType NextView = ViewType.None;

    private SharedPreferences mPreferences;

    Context mContext;
    SurfaceHolder mSurfaceHolder;

    private Thread mGameThread;
    private boolean mRunning, firstTime = false;

    public Logo Logo;

    Overlord mOverlord;
    Scoreboard Scoreboard;
    Information InformationView;

    public MainView(Context context) {
        super(context);
        mContext = context;
        mPreferences = context.getSharedPreferences("scoreboard", Context.MODE_PRIVATE);
        String mode = mPreferences.getString("last-game-mode", "none");
        firstTime = mode.equals("none");

        switch (mode) {
            case "move-limited":
                Overlord.CurrentMode = Overlord.GameMode.MoveLimited;
                break;
            case "time-limited":
                Overlord.CurrentMode = Overlord.GameMode.TimeLimited;
                break;
            case "until-pointless":
                Overlord.CurrentMode = Overlord.GameMode.UntilPointless;
                break;
            default:
                Overlord.CurrentMode = Overlord.GameMode.Infinite;
                break;
        }

        Overlord.ModeMultiplier = mPreferences.getInt("last-multiplier", 1);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        ParticleSystem = new ParticleSystem();
        Achievements = new Achievements();
        Achievements.load(context);

        Logo = new Logo(this);
        Logo.load(mContext);

        Scoreboard = new Scoreboard(this);
        Scoreboard.load(context);

        mOverlord = new Overlord(this);
        mOverlord.load(context);
        mOverlord.setScoreboard(Scoreboard);

        InformationView = new Information(this);
        InformationView.load(context);

        if (firstTime) {
            Logo.addToQueue(com.github.breskin.squares.Logo.LogoState.LeftToCenterAnimation);
            Logo.addToQueue(com.github.breskin.squares.Logo.LogoState.CenteredUnwrappingAnimation);
            Logo.addToQueue(com.github.breskin.squares.Logo.LogoState.CenteredUnwrapped);

            if (!Logo.isAnimating())
                Logo.startQueue();

            InformationView.show();
            CurrentView = ViewType.Info;
        } else {
            Logo.playInitialAnimation();
            mOverlord.createNewGame();
        }
    }

    @Override
    public void run() {
        Canvas canvas;

        while (mRunning) {
            if (mSurfaceHolder.getSurface().isValid()) {
                long time = System.currentTimeMillis();

                if(android.os.Build.VERSION.SDK_INT >= 26)
                    canvas = mSurfaceHolder.lockHardwareCanvas();
                else
                    canvas = mSurfaceHolder.lockCanvas();

                if (canvas == null)
                    continue;

                Achievements.update();

                canvas.save();
                canvas.drawColor(Color.BLACK);

                switch (CurrentView) {
                    case Game:
                        mOverlord.update();
                        mOverlord.render(canvas);
                        break;

                    case Scoreboard:
                        Scoreboard.update();
                        Scoreboard.render(canvas);
                        break;

                    case Info:
                        InformationView.update();
                        InformationView.render(canvas);
                        break;
                }


                ParticleSystem.render(canvas);

                Logo.update();
                Logo.render(canvas);


                canvas.restore();
                mSurfaceHolder.unlockCanvasAndPost(canvas);

                if (mOverlord.isGameStarted() && !mOverlord.isGameFinished())
                    Overlord.Time += System.currentTimeMillis() - time;

                FrameTime = (int)(System.currentTimeMillis() - time);
            }
        }
    }

    public void createNewGame() {
        mOverlord.createNewGame();
    }

    public void hideGameUI() {
        switch (CurrentView) {
            case Info:

                break;

            case Game:
                mOverlord.hideGameUI();
                break;

            case Scoreboard:
                Scoreboard.hide();
                break;
        }
    }

    public void nextView() {
        CurrentView = NextView;
        NextView = ViewType.None;

        switch (CurrentView) {
            case Info:
                InformationView.show();
                break;

            case Game:
                createNewGame();
                break;

            case Scoreboard:
                Scoreboard.show();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (CurrentView) {
            case Game:
                mOverlord.onTouchEvent(event);
                break;

            case Scoreboard:
                Scoreboard.onTouchEvent(event);
                break;

            case Info:
                InformationView.onTouchEvent(event);
                break;
        }


        if (y > ViewHeight - Logo.Size.y)
            Logo.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }

    public boolean onBackPressed() {
        switch (CurrentView) {
            case Game:
                return mOverlord.onBackPressed();

            case Scoreboard:
                return Scoreboard.onBackPressed();

            case Info:
                return InformationView.onBackPressed();
        }

        return false;
    }

    public void openBrowser() {
        ((MainActivity)mContext).openBrowser();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mRunning = false;
        try {
            mGameThread.join();
        } catch (InterruptedException e) { }
    }

    public void pause() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("last-multiplier", Overlord.ModeMultiplier);
        switch (Overlord.CurrentMode) {
            case MoveLimited:
                editor.putString("last-game-mode", "move-limited");
                break;
            case TimeLimited:
                editor.putString("last-game-mode", "time-limited");
                break;
            case Infinite:
                editor.putString("last-game-mode", "infinite");
                break;
            case UntilPointless:
                editor.putString("last-game-mode", "until-pointless");
                break;
        }
        editor.commit();

        mRunning = false;
        try {
            mGameThread.join();
        } catch (InterruptedException e) { }
    }

    public void resume() {
        if (!mRunning) {
            mRunning = true;
            mGameThread = new Thread(this);
            mGameThread.start();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        ViewWidth = w;
        ViewHeight = h;

        Board.SquareSize = ViewWidth / 6;

        mSurfaceHolder.setFixedSize(ViewWidth, ViewHeight);
    }

    public static float fitFontSize(Paint mPaint, String text, float desiredFontSize, int maxWidth) {
        float save = mPaint.getTextSize();
        mPaint.setTextSize(desiredFontSize);

        while (mPaint.measureText(text) > maxWidth)
            mPaint.setTextSize(mPaint.getTextSize() - 1);

        float result = mPaint.getTextSize();
        mPaint.setTextSize(save);

        return result;
    }
}
