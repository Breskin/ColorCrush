package com.github.breskin.squares.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.breskin.squares.Logo;
import com.github.breskin.squares.MainView;

import java.util.Random;

/**
 * Created by Kuba on 11.11.2018.
 */

public class Square {
    public enum SquareColor {
        None, Purple, Blue, Cyan, Green, Yellow, Red;

        private static Random Random = new Random();

        public int toColor() {
            switch (this)
            {
                case Red:
                    return Color.rgb(255, 0, 0);
                case Green:
                    return Color.rgb(0, 255, 0);
                case Blue:
                    return Color.rgb(0, 32, 255);
                case Purple:
                    return Color.rgb(192, 0, 192);
                case Yellow:
                    return Color.rgb(255, 255, 0);
                case Cyan:
                    return Color.rgb(0, 255, 255);
            }

            return Color.BLACK;
        }

        public SquareColor nextColor() {
            switch (this)
            {
                case Red:
                    return SquareColor.Purple;
                case Green:
                    return SquareColor.Yellow;
                case Blue:
                    return SquareColor.Cyan;
                case Purple:
                    return SquareColor.Blue;
                case Yellow:
                    return SquareColor.Red;
                case Cyan:
                    return SquareColor.Green;
            }

            return SquareColor.None;
        }

        public static SquareColor random() {
            int next = Random.nextInt(6);

            switch (next)
            {
                case 0:
                    return SquareColor.Purple;
                case 1:
                    return SquareColor.Blue;
                case 2:
                    return SquareColor.Cyan;
                case 3:
                    return SquareColor.Green;
                case 4:
                    return SquareColor.Yellow;
                case 5:
                    return SquareColor.Red;
            }

            return SquareColor.None;
        }
    }
    public enum SquareAnimation { None, Initial, MoveToIndex, Emerge }

    public boolean ToDelete = false;

    private SquareColor CurrentColor, NextColor;
    private SquareAnimation CurrentAnimation;

    private float X, Y, Enlargement = 0;
    private int IndexX, IndexY;

    private static Paint mPaint = new Paint();
    private int Fill;
    private float StartX = Float.NaN, StartY = Float.NaN, StartEnlargement = Float.NaN;

    public Square(SquareColor color, int x, int y) {
        CurrentColor = color;
        NextColor = SquareColor.None;
        CurrentAnimation = SquareAnimation.None;
        X = IndexX = x;
        Y = IndexY = y;

        Fill = CurrentColor.toColor();

        mPaint.setAntiAlias(true);
    }

    public void update() {
        switch (CurrentAnimation) {
            case Initial:
            case MoveToIndex:
                float diffX = X - IndexX, diffY = Y - IndexY;

                double progress = calculateProgress();

                X -= diffX * ((CurrentAnimation == SquareAnimation.Initial) ? 0.12f : 0.25f) * (MainView.FrameTime / 16f);
                Y -= diffY * ((CurrentAnimation == SquareAnimation.Initial) ? 0.12f : 0.25f) * (MainView.FrameTime / 16f);

                Enlargement = StartEnlargement * (float)(1 - progress);

                if (NextColor != SquareColor.None)
                    Fill = calculateColor(progress);

                if (Math.abs(diffX) < ((CurrentAnimation == SquareAnimation.Initial) ? 0.01f : 0.02f) && Math.abs(diffY) < ((CurrentAnimation == SquareAnimation.Initial) ? 0.01f : 0.02f)) {
                    X = IndexX;
                    Y = IndexY;
                    Enlargement = 0;

                    NextColor = SquareColor.None;
                    StartX = StartY = StartEnlargement = Float.NaN;

                    CurrentAnimation = SquareAnimation.None;
                }
                break;

            case Emerge:
                Enlargement -= Enlargement * 0.2f * (MainView.FrameTime / 16f);

                if (Enlargement > -0.01) {
                    Enlargement = 0;
                    CurrentAnimation = SquareAnimation.None;
                }
                break;

            case None:
                Fill = CurrentColor.toColor();
                Enlargement = 0;
                break;
        }
    }

    public void render(Canvas canvas) {
        mPaint.setColor(Fill);
        canvas.drawRoundRect(getRect(), MainView.ViewWidth / 80f, MainView.ViewWidth / 80f, mPaint);
    }

    public RectF getRect() {
        float px = Board.SquareSize / 6 + Board.SquareSize * X * 7 / 6 - Board.SquareSize * Enlargement / 2,
              py = Board.SquareSize / 6 + Board.SquareSize * Y * 7 / 6 + MainView.ViewHeight - Board.SquareSize * 35 / 6 - Logo.Size.y * 5 / 4 - Board.SquareSize * Enlargement / 2;

        return new RectF(px, py, px + Board.SquareSize + Board.SquareSize * Enlargement, py + Board.SquareSize + Board.SquareSize * Enlargement);
    }

    int calculateColor(double progress) {
        int next = CurrentColor.toColor(), current = NextColor.toColor();

        int cr = (current >> 16) & 0xff, cg = (current >> 8) & 0xff, cb = (current) & 0xff;
        int nr = (next >> 16) & 0xff, ng = (next >> 8) & 0xff, nb = (next) & 0xff;

        int ar = (int)(cr + (nr - cr) * progress), ag = (int)(cg + (ng - cg) * progress), ab = (int)(cb + (nb - cb) * progress);

        return Color.rgb(ar, ag, ab);
    }

    double calculateProgress() {
        double max = Math.sqrt((StartX - IndexX) * (StartX - IndexX) + (StartY - IndexY) * (StartY - IndexY));
        double current = Math.sqrt((X - IndexX) * (X - IndexX) + (Y - IndexY) * (Y - IndexY));
        return 1 - current / max;
    }

    public void setCurrentAnimation(SquareAnimation a) {
        CurrentAnimation = a;
        StartX = X;
        StartY = Y;
        StartEnlargement = Enlargement;
    }
    public SquareAnimation getCurrentAnimation() {
        return CurrentAnimation;
    }

    public void setCurrentColor(SquareColor color) {
        CurrentColor = color;
        Fill = color.toColor();
    }
    public SquareColor getCurrentColor() {
        return CurrentColor;
    }

    public void setNextColor(SquareColor color) {
        NextColor = color;
    }
    public SquareColor getNextColor() {
        return NextColor;
    }

    public int getIndexX() {
        return IndexX;
    }
    public void setIndexX(int x) {
        IndexX = x;
    }

    public int getIndexY() {
        return IndexY;
    }
    public void setIndexY(int y) {
        IndexY = y;
    }

    public float getX() {
        return X;
    }
    public void setX(float x) {
        X = x;
    }
    public void increaseX(float x) {
        X += x;
    }

    public float getY() {
        return Y;
    }
    public void setY(float y) {
        Y = y;
    }
    public void increaseY(float y) {
        Y += y;
    }

    public float getEnlargement() {
        return Enlargement;
    }
    public void setEnlargement(float enlargement) {
        Enlargement = enlargement;
    }
}
