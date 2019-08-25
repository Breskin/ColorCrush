package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.view.MotionEvent;

public class GameLogic {

    private Board board;

    public GameLogic() {
        board = new Board();
    }

    public void update() {
        board.update();
    }

    public void render(Canvas canvas) {
        board.render(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return board.onTouchEvent(event);
    }

    public Board getBoard() {
        return board;
    }
}
