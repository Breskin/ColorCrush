package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.github.breskin.squares.RenderView;

public class Board {

    private Block[][] blocks;
    private PointF translation;

    private Block selectedBlock;

    public Board() {
        blocks = new Block[5][5];

        generate();
    }

    public void generate() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                blocks[x][y] = new Block(this, BlockColor.random(), x, y);
            }
        }
    }

    public void update() {
        translation = new PointF(Block.getSize() * Block.SIZE_MULTIPLIER, RenderView.ViewHeight - RenderView.ViewWidth);

        updateSelectedMovement();

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                blocks[x][y].setTranslation(translation);
                blocks[x][y].update();
            }
        }
    }

    private void updateSelectedMovement() {
        if (selectedBlock != null) {
            int newX = Math.round(selectedBlock.getCurrentPosition().x), newY = Math.round(selectedBlock.getCurrentPosition().y);

            int calculationX = Math.round(selectedBlock.getCurrentPosition().x - 0.15f * Math.signum(selectedBlock.getCurrentPosition().x - selectedBlock.getTargetPosition().x));
            int calculationY = Math.round(selectedBlock.getCurrentPosition().y - 0.15f * Math.signum(selectedBlock.getCurrentPosition().y - selectedBlock.getTargetPosition().y));

            if ((selectedBlock.getTargetPosition().x != calculationX || selectedBlock.getTargetPosition().y != calculationY) && newX >= 0 && newY >= 0 && newX < 5 && newY < 5) {
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y] = blocks[newX][newY];
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y].nextColor();
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y].moveTo(selectedBlock.getTargetPosition().x, selectedBlock.getTargetPosition().y);

                blocks[newX][newY] = selectedBlock;
                selectedBlock.moveTo(newX, newY);
            }
        }
    }

    public void render(Canvas canvas) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (blocks[x][y] != selectedBlock && !blocks[x][y].goingHome())
                    blocks[x][y].render(canvas);
            }
        }

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (blocks[x][y].goingHome())
                    blocks[x][y].render(canvas);
            }
        }

        if (selectedBlock != null)
            selectedBlock.render(canvas);
    }

    public void setSelectedBlock(Block block) {
        selectedBlock = block;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (blocks[0][0] == null)
            return false;

        boolean handled = false;
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (blocks[x][y].onTouchEvent(event)) {
                    handled = true;
                    break;
                }
            }

            if (handled)
                break;
        }

        /*float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:

                break;

            case MotionEvent.ACTION_UP:

                break;
        }*/

        return handled;
    }
}
