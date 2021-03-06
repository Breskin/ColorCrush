package com.github.breskin.squares;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.github.breskin.squares.gameplay.GameView;
import com.github.breskin.squares.particles.ParticleSystem;
import com.github.breskin.squares.scoreboard.ScoreboardView;
import com.github.breskin.squares.tutorial.HowToView;

public class RenderView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    public enum ViewType { None, Game, Scoreboard, HowTo }

    public static int ViewHeight = 0, ViewWidth = 0, FrameTime = 0;

    private static Context context;
    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private boolean threadRunning;

    private ViewType currentView = ViewType.None;

    private ParticleSystem particleSystem;

    private GameView gameView;
    private ScoreboardView scoreboardView;
    private HowToView howToView;

    public RenderView(Context context) {
        super(context);
        this.context = context;

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        DataManager.init(context);

        particleSystem = new ParticleSystem();

        gameView = new GameView(this);
        gameView.load(context);

        scoreboardView = new ScoreboardView(this);
        scoreboardView.load(context);

        howToView = new HowToView(this);
        howToView.load(context);
    }

    private void render(Canvas canvas) {
        switch (currentView) {
            case Game: gameView.update(); gameView.render(canvas); break;
            case Scoreboard: scoreboardView.update(); scoreboardView.render(canvas); break;
            case HowTo: howToView.update(); howToView.render(canvas); break;
        }

        particleSystem.update();
        particleSystem.render(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)  {
        float x = event.getX();
        float y = event.getY();

        switch (currentView) {
            case Game: gameView.onTouchEvent(event); break;
            case Scoreboard: scoreboardView.onTouchEvent(event); break;
            case HowTo: howToView.onTouchEvent(event); break;
        }

        return true;
    }

    public void switchView(ViewType next) {
        currentView = next;

        switch (currentView) {
            case Game: gameView.open(); break;
            case Scoreboard: scoreboardView.open(); break;
            case HowTo: howToView.open(); break;
        }
    }

    public boolean onBackPressed() {
        switch (currentView) {
            case Game: return gameView.onBackPressed();
            case Scoreboard: return scoreboardView.onBackPressed();
            case HowTo: return howToView.onBackPressed();
        }

        return false;
    }

    @Override
    public void run() {
        Canvas canvas;

        while (threadRunning) {
            if (surfaceHolder.getSurface().isValid()) {
                if (currentView == ViewType.None)
                    if (DataManager.isFirstLaunch())
                        switchView(ViewType.HowTo);
                    else
                        switchView(ViewType.Game);

                long time = System.nanoTime() / 1000000;

                if(Build.VERSION.SDK_INT >= 26)
                    canvas = surfaceHolder.lockHardwareCanvas();
                else
                    canvas = surfaceHolder.lockCanvas();

                if (canvas == null)
                    continue;

                canvas.save();
                canvas.drawColor(Color.BLACK);

                render(canvas);

                canvas.restore();
                surfaceHolder.unlockCanvasAndPost(canvas);

                if (System.nanoTime() / 1000000 - time < 16)
                    try { Thread.sleep(16 - System.nanoTime() / 1000000 + time); } catch (Exception e) {}

                FrameTime = (int)(System.nanoTime() / 1000000 - time);
            }
        }
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        threadRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) { }
    }

    public void pause() {
        threadRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) { }
    }

    public void resume() {
        threadRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        ViewWidth = w;
        ViewHeight = h;

        surfaceHolder.setFixedSize(ViewWidth, ViewHeight);
    }

    public static void vibrate(int time) {
        /*if  (!Config.VIBRATIONS_ENABLED)
            return;*/

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(time);
        }
    }
}
