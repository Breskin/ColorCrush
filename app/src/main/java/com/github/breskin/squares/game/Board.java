package com.github.breskin.squares.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.Logo;
import com.github.breskin.squares.game.toaster.PointToasts;
import com.github.breskin.squares.MainView;

/**
 * Created by Kuba on 11.11.2018.
 */

public class Board {

    public static int SquareSize;

    private Overlord mOverlord;
    private BoardUpdater Updater;

    private PointF previousTouchPosition;
    private Point selectedSquare;

    private PointToasts Toasts;

    Square[][] Squares;

    public Board(Overlord o) {
        mOverlord = o;
        Updater = new BoardUpdater(o, this);

        Toasts = new PointToasts();
    }

    public void startGame() {
        previousTouchPosition = new PointF(0, 0);
        selectedSquare = new Point(-1, -1);
        Squares = new Square[5][5];
        Toasts.clear();

        for (int x=0; x<5; x++) {
            for (int y=0; y<5; y++) {
                Squares[x][y] = new Square(Square.SquareColor.random(), x, y);

                Squares[x][y].setX((x - 2) * 4 + 2);
                Squares[x][y].setY((y - 8) * 2);
                Squares[x][y].setEnlargement(0.75f);

                Squares[x][y].setCurrentAnimation(Square.SquareAnimation.Initial);
            }
        }

        for (int i = 0; i < 5; i++) {
            for (int x = 0; x < 5; x++) {
                for (int y = 0; y < 5; y++) {
                    if ((x > 0 && Squares[x - 1][y].getCurrentColor() == Squares[x][y].getCurrentColor()) && (x < 4 && Squares[x][y].getCurrentColor() == Squares[x + 1][y].getCurrentColor()))
                        Squares[x][y].setCurrentColor(Squares[x][y].getCurrentColor().nextColor());

                    if ((y > 0 && Squares[x][y - 1].getCurrentColor() == Squares[x][y].getCurrentColor()) && (y < 4 && Squares[x][y].getCurrentColor() == Squares[x][y + 1].getCurrentColor()))
                        Squares[x][y].setCurrentColor(Squares[x][y].getCurrentColor().nextColor());
                }
            }
        }
    }

    public void update() {
        try {
            if (Squares[0][0] != null) {
                Toasts.update();
                MainView.Achievements.checkBoardState(Squares);
                updateSquares();

                if (selectedSquare.x != -1 && selectedSquare.y != -1 && (Squares[selectedSquare.x][selectedSquare.y].ToDelete || mOverlord.isGameConditionMet())) {
                    Squares[selectedSquare.x][selectedSquare.y].setCurrentAnimation(Square.SquareAnimation.MoveToIndex);
                    selectedSquare.x = selectedSquare.y = -1;
                }

                Updater.update();
                Updater.deleteLines(Squares);

                if (mOverlord.isGameConditionMet()) {
                    if (!isAnimating() && mOverlord.canEndGame())
                        finishGame();
                }
            }
        }
        catch (Exception e) {}
    }

    public void render(Canvas canvas) {
        if (mOverlord.isGameFinished() || Squares[0][0] == null)
            return;

        for (int x=0; x<5; x++) {
            for (int y=0; y<5; y++) {
                if (!((x == selectedSquare.x && y == selectedSquare.y) || Squares[x][y].getCurrentAnimation() == Square.SquareAnimation.MoveToIndex))
                    Squares[x][y].render(canvas);
            }
        }

        for (int x=0; x<5; x++) {
            for (int y = 0; y < 5; y++) {
                if (Squares[x][y].getCurrentAnimation() == Square.SquareAnimation.MoveToIndex)
                    Squares[x][y].render(canvas);
            }
        }

        if (selectedSquare.x != -1 && selectedSquare.y != -1)
            Squares[selectedSquare.x][selectedSquare.y].render(canvas);

        Toasts.render(canvas);
    }

    public void onTouchEvent(MotionEvent event) {
        if (mOverlord.isGameFinished() || mOverlord.isGameConditionMet() || Squares[0][0] == null)
            return;
        float x = event.getX();
        float y = event.getY();

        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float sx = (x - Board.SquareSize / 6) / Board.SquareSize * 6 / 7;
                    float sy = (y - (MainView.ViewHeight - Board.SquareSize * 34 / 6 - Logo.Size.y * 5 / 4)) / Board.SquareSize * 6 / 7;

                    if (sx >= 0 && sx < 5 && sy >= 0 && sy < 5 && sx - (int) sx < 6f / 7f && sy - (int) sy < 6f / 7f && Squares[0][0].getCurrentAnimation() != Square.SquareAnimation.Initial) {
                        selectedSquare.x = (int) sx;
                        selectedSquare.y = (int) sy;

                        Squares[selectedSquare.x][selectedSquare.y].setEnlargement(0.2f);

                        mOverlord.onGameStarted();
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (selectedSquare.x != -1 && selectedSquare.y != -1 && Squares[selectedSquare.x][selectedSquare.y] != null) {
                        Squares[selectedSquare.x][selectedSquare.y].increaseX((x - previousTouchPosition.x) / Board.SquareSize);
                        Squares[selectedSquare.x][selectedSquare.y].increaseY((y - previousTouchPosition.y) / Board.SquareSize);

                        updateSwitching();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (selectedSquare.x != -1 && selectedSquare.y != -1) {
                        Squares[selectedSquare.x][selectedSquare.y].setCurrentAnimation(Square.SquareAnimation.MoveToIndex);

                        selectedSquare.x = selectedSquare.y = -1;
                    }
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        previousTouchPosition.x = x;
        previousTouchPosition.y = y;
    }

    public void onExternalTouch() {
        if (mOverlord.isGameFinished())
            return;

        if (selectedSquare.x != -1 && selectedSquare.y != -1) {
            Squares[selectedSquare.x][selectedSquare.y].setCurrentAnimation(Square.SquareAnimation.MoveToIndex);
            selectedSquare.x = selectedSquare.y = -1;
        }
    }

    public void addToast(String text, PointF pos) {
        Toasts.add(text, pos);
    }

    public static float getTopMargin() {
        return MainView.ViewHeight - SquareSize * 35 / 6 - Logo.Size.y * 5 / 4;
    }

    public static float getHeight() {
        return SquareSize / 6 + SquareSize * 5 * 7 / 6;
    }

    void updateSquares() {
        if (Squares[0][0] == null)
            return;

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                Squares[x][y].update();

                //if (!mOverlord.isGameStarted() && Squares[x][y].getCurrentAnimation() != Square.SquareAnimation.Initial)
                    //mOverlord.setWaitingForPlayerToStartGame(true);
            }
        }
    }

    void updateSwitching() {
        if (selectedSquare.x == -1 || selectedSquare.y == -1)
            return;

        Square s = Squares[selectedSquare.x][selectedSquare.y];

        int nX = Math.round(s.getX()), nY = Math.round(s.getY()), cX = Math.round(s.getX() - 0.1f * Math.signum(s.getX() - s.getIndexX())), cY = Math.round(s.getY() - 0.1f * Math.signum(s.getY() - s.getIndexY()));

        if ((cX != s.getIndexX() || cY != s.getIndexY()) && nY >= 0 && nX >= 0 && nX < 5 && nY < 5){
            Square copy = Squares[nX][nY];

            Squares[s.getIndexX()][s.getIndexY()] = copy;
            copy.setIndexX(s.getIndexX());
            copy.setIndexY(s.getIndexY());
            copy.setCurrentAnimation(Square.SquareAnimation.MoveToIndex);
            copy.setNextColor(copy.getCurrentColor());
            copy.setCurrentColor(copy.getCurrentColor().nextColor());

            Squares[nX][nY] = s;
            s.setIndexX(nX);
            s.setIndexY(nY);

            selectedSquare.x = nX;
            selectedSquare.y = nY;

            if (Overlord.CurrentMode == Overlord.GameMode.UntilPointless) {
                Squares[nX][nY].setCurrentAnimation(Square.SquareAnimation.MoveToIndex);
                selectedSquare.x = selectedSquare.y = -1;
            }

            boolean lineFound = Updater.findLines(Squares);

            if (Overlord.CurrentMode == Overlord.GameMode.UntilPointless && !lineFound)
                mOverlord.setGameConditionMet(true);

            Overlord.Moves++;
        }
    }

    public void finishGame() {
        if (Squares[0][0] == null)
            return;

        for (int x=0; x<5; x++) {
            for (int y=0; y<5; y++) {
                int color = Squares[x][y].getCurrentColor().toColor();

                if(android.os.Build.VERSION.SDK_INT >= 26)
                    MainView.ParticleSystem.createInArea(Squares[x][y].getRect(), MainView.ViewWidth / 20, 8, (color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);
                else
                    MainView.ParticleSystem.createInArea(Squares[x][y].getRect(), MainView.ViewWidth / 12, 3, (color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);

                Squares[x][y] = null;
            }
        }

        mOverlord.onGameFinished();
    }

    public void load(Context context) {
        Updater.load(context);
    }

    boolean isAnimating() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (Squares[x][y].getCurrentAnimation() != Square.SquareAnimation.None)
                    return true;
            }
        }

        return false;
    }
}
