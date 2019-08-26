package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.particles.ParticleSystem;

public class GameLogic {

    private GameView gameView;
    private Board board;
    private GameMode currentMode;

    private boolean gameStarted = false, gameFinished = false, touchDown = false;

    public int moveCount = 0, gameDuration = 0, points = 0;
    public float pointsVisible = 0;

    public GameLogic(GameView gameView) {
        this.gameView = gameView;
        board = new Board();
    }

    public void update() {
        if (currentMode == null)
            currentMode = gameView.getSelectedMode();

        board.update(this);

        pointsVisible += (points - pointsVisible) * 0.1f;

        if (gameStarted) {
            if (!gameFinished && !currentMode.isLocked())
                gameDuration += RenderView.FrameTime;

            if (currentMode != null)
                currentMode.update(this);
        }

        if (gameFinished && currentMode.isClosed()) {
            gameView.open();
            currentMode.reset();
        }
    }

    public void render(Canvas canvas) {
        if (gameStarted) {
            if (currentMode != null)
                currentMode.renderSessionInfo(this, canvas);
        }

        board.render(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean boardTouch = board.onTouchEvent(this, event);

        if (boardTouch && !gameStarted) {
            startGame();
        }

        if (gameFinished) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                touchDown = true;
            else if (touchDown && event.getAction() == MotionEvent.ACTION_UP) {
                currentMode.close();
                touchDown = false;
            }
        }

        return boardTouch;
    }

    private void startGame() {
        gameStarted = true;

        moveCount = 0;
        gameDuration = 0;
        points = 0;
        pointsVisible = 0;

        currentMode.reset();
    }

    public void finishGame() {
        gameFinished = true;
        pointsVisible = points;
        currentMode.checkScore(this);

        board.clear(this);
    }

    public boolean onBackPressed() {
        if (gameStarted && !gameFinished) {
            finishGame();

            return true;
        } else if (gameStarted && gameFinished) {
            currentMode.close();

            return true;
        }

        return false;
    }

    public void prepare() {
        gameFinished = false;
        gameStarted = false;
        board.setTranslation(new PointF(Block.getSize() * Block.SIZE_MULTIPLIER, RenderView.ViewHeight - RenderView.ViewWidth - ((float)RenderView.ViewHeight / RenderView.ViewWidth - 1.5f) * Block.getSize() * 2));
        board.generate();
    }

    public void onMoveMade() {
        moveCount++;
        currentMode.onMoveMade(this);
    }

    public Board getBoard() {
        return board;
    }

    public void setCurrentMode(GameMode currentMode) {
        this.currentMode = currentMode;
    }

    public ParticleSystem getParticleSystem() {
        return gameView.getRenderView().getParticleSystem();
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }
}
