package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.gameplay.modes.GameMode;
import com.github.breskin.squares.particles.ParticleSystem;

public class GameLogic {

    private GameView gameView;
    private Board board;
    private GameMode currentMode;

    private boolean gameStarted = false, gameFinished = false;

    public int moveCount = 0, gameDuration = 0, points = 0;
    public float pointsVisible = 0;

    public GameLogic(GameView gameView) {
        this.gameView = gameView;
        board = new Board();
    }

    public void update() {
        board.update(this);

        pointsVisible += (points - pointsVisible) * 0.1f;

        if (gameStarted) {
            if (!gameFinished)
                gameDuration += RenderView.FrameTime;

            if (currentMode != null)
                currentMode.update(this);
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
        boolean boardTouch = board.onTouchEvent(event);

        if (boardTouch && !gameStarted) {
            startGame();
        }

        if (gameFinished) {
            prepare();
        }

        return boardTouch;
    }

    private void startGame() {
        gameStarted = true;

        moveCount = 0;
        gameDuration = 0;
        points = 0;
        pointsVisible = 0;

        currentMode = gameView.getSelectedMode();
    }

    public void finishGame() {
        gameFinished = true;
        pointsVisible = points;

        board.clear(this);
    }

    public boolean onBackPressed() {
        if (gameStarted && !gameFinished) {
            finishGame();

            return true;
        } else if (gameStarted && gameFinished) {
            prepare();

            return true;
        }

        return false;
    }

    public void prepare() {
        gameFinished = false;
        gameStarted = false;
        board.setTranslation(new PointF(Block.getSize() * Block.SIZE_MULTIPLIER, RenderView.ViewHeight - RenderView.ViewWidth));
        board.generate();
    }

    public Board getBoard() {
        return board;
    }

    public ParticleSystem getParticleSystem() {
        return gameView.getRenderView().getParticleSystem();
    }
}
