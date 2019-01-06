package com.github.breskin.squares.game;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Looper;
import android.util.Log;

import com.github.breskin.squares.MainActivity;
import com.github.breskin.squares.MainView;
import com.github.breskin.squares.game.toaster.Toast;

import java.util.HashMap;

/**
 * Created by Kuba on 13.11.2018.
 */

public class BoardUpdater {

    Context mContext;
    Board mBoard;
    Overlord mOverlord;

    private HashMap<Square.SquareColor, Integer> points = new HashMap<>();

    public BoardUpdater(Overlord o, Board gb) {
        mOverlord = o;
        mBoard = gb;
    }

    public void update() {

    }

    public boolean findLines(Square Squares[][]) {
        boolean lineFound = false;

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if ((x > 0 && Squares[x - 1][y].getCurrentColor() == Squares[x][y].getCurrentColor()) && (x < 4 && Squares[x][y].getCurrentColor() == Squares[x + 1][y].getCurrentColor())) {
                    Squares[x - 1][y].ToDelete = Squares[x][y].ToDelete = Squares[x + 1][y].ToDelete = lineFound = true;
                }

                if ((y > 0 && Squares[x][y - 1].getCurrentColor() == Squares[x][y].getCurrentColor()) && (y < 4 && Squares[x][y].getCurrentColor() == Squares[x][y + 1].getCurrentColor())) {
                    Squares[x][y - 1].ToDelete = Squares[x][y].ToDelete = Squares[x][y + 1].ToDelete = lineFound = true;
                }
            }
        }

        deleteLines(Squares);

        return lineFound;
    }

    public void deleteLines(Square Squares[][]) {
        boolean moving = isMoving(Squares), newBlocks = false;

        points.clear();

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (!moving && Squares[x][y].ToDelete) {
                    if (!points.containsKey(Squares[x][y].getCurrentColor()))
                        points.put(Squares[x][y].getCurrentColor(), 1);
                    else
                        points.put(Squares[x][y].getCurrentColor(), points.get(Squares[x][y].getCurrentColor()) + 1);
                }
            }
        }

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (!moving && Squares[x][y].ToDelete && points.get(Squares[x][y].getCurrentColor()) != null) {
                    mOverlord.addPoints(points.get(Squares[x][y].getCurrentColor()) - 2);

                    RectF rect = Squares[x][y].getRect();
                    mBoard.addToast("+" + (points.get(Squares[x][y].getCurrentColor()) - 2), new PointF(rect.left + rect.width() / 2, rect.top + rect.height() / 2));

                    int color = Squares[x][y].getCurrentColor().toColor();

                    if(android.os.Build.VERSION.SDK_INT >= 26)
                        MainView.ParticleSystem.createInArea(Squares[x][y].getRect(), MainView.ViewWidth / 17, 15, (color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);
                    else
                        MainView.ParticleSystem.createInArea(Squares[x][y].getRect(), MainView.ViewWidth / 13, 5, (color >> 16) & 0xff, (color >> 8) & 0xff, (color) & 0xff);

                    Squares[x][y] = new Square(Square.SquareColor.random(), x, y);
                    Squares[x][y].setEnlargement(-1);
                    Squares[x][y].setCurrentAnimation(Square.SquareAnimation.Emerge);

                    newBlocks = true;
                }
            }
        }

        if (newBlocks)
            findLines(Squares);
    }

    public void load(Context context) {
        mContext = context;
    }

    boolean isMoving(Square Squares[][]) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (Squares[x][y].getCurrentAnimation() == Square.SquareAnimation.MoveToIndex || Squares[x][y].getCurrentAnimation() == Square.SquareAnimation.Emerge)
                    return true;
            }
        }

        return false;
    }
}
