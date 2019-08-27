package com.github.breskin.squares.gameplay;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Board {

    private int width = 5, height = 5;

    private Block[][] blocks;
    private PointF translation;

    private Block selectedBlock;

    private Stack<Block> blocksInPatterns;
    private List<FloatingPoint> floatingPoints;

    public Board() {
        blocks = new Block[width][height];
        blocksInPatterns = new Stack<>();
        floatingPoints = new ArrayList<>();

        generate();
    }

    public void generate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks[x][y] = new Block(this, BlockColor.random(), x, y);

                blocks[x][y].getCurrentPosition().x = (x - 2) * 5;
                blocks[x][y].getCurrentPosition().y = (y - 9) * 5;
            }
        }

        findPatterns();

        while (blocksInPatterns.size() > 0) {
            for (Block block : blocksInPatterns)
                block.setColor(BlockColor.random());

            findPatterns();
        }
    }

    public void update(GameLogic logic) {
        updateSelectedMovement(logic);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks[x][y].setTranslation(translation);
                blocks[x][y].update();
            }
        }

        for (int i = 0; i < floatingPoints.size(); i++) {
            floatingPoints.get(i).update();

            if (!floatingPoints.get(i).alive()) {
                floatingPoints.remove(i);
                i--;
            }
        }
    }

    public void clear(GameLogic logic) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks[x][y].destroy(logic);
                blocks[x][y].setColor(BlockColor.None);
            }
        }
    }

    private void updateSelectedMovement(GameLogic logic) {
        if (selectedBlock != null) {
            int newX = Math.round(selectedBlock.getCurrentPosition().x), newY = Math.round(selectedBlock.getCurrentPosition().y);

            int calculationX = Math.round(selectedBlock.getCurrentPosition().x - 0.15f * Math.signum(selectedBlock.getCurrentPosition().x - selectedBlock.getTargetPosition().x));
            int calculationY = Math.round(selectedBlock.getCurrentPosition().y - 0.15f * Math.signum(selectedBlock.getCurrentPosition().y - selectedBlock.getTargetPosition().y));

            if ((selectedBlock.getTargetPosition().x != calculationX || selectedBlock.getTargetPosition().y != calculationY) && newX >= 0 && newY >= 0 && newX < width && newY < height) {
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y] = blocks[newX][newY];
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y].nextColor();
                blocks[selectedBlock.getTargetPosition().x][selectedBlock.getTargetPosition().y].moveTo(selectedBlock.getTargetPosition().x, selectedBlock.getTargetPosition().y);

                blocks[newX][newY] = selectedBlock;
                selectedBlock.moveTo(newX, newY);

                logic.onMoveMade();
            }
        }
    }

    public void render(Canvas canvas) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (blocks[x][y] != selectedBlock && !blocks[x][y].isGoingHome())
                    blocks[x][y].render(canvas);
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (blocks[x][y].isGoingHome())
                    blocks[x][y].render(canvas);
            }
        }

        if (selectedBlock != null)
            selectedBlock.render(canvas);

        for (FloatingPoint point : floatingPoints)
            point.render(canvas);
    }

    public void setSelectedBlock(Block block) {
        selectedBlock = block;
    }

    public Block getBlock(int x, int y) {
        return blocks[x][y];
    }

    public Stack<Block> findPatterns() {
        blocksInPatterns.clear();

        for (int x = 0; x < width; x++) {
            int length = 1;
            blocksInPatterns.push(blocks[x][0]);

            for (int y = 1; y < height; y++) {
                if (blocks[x][y - 1].getTargetColor() == blocks[x][y].getTargetColor()) {
                    length++;
                    blocksInPatterns.push(blocks[x][y]);
                } else {
                    if (length < 3) {
                        while (length > 0) {
                            blocksInPatterns.pop();
                            length--;
                        }
                    }

                    length = 1;
                    blocksInPatterns.push(blocks[x][y]);
                }
            }

            if (length < 3) {
                while (length > 0) {
                    blocksInPatterns.pop();
                    length--;
                }
            }
        }

        for (int y = 0; y < height; y++) {
            int length = 1;
            blocksInPatterns.push(blocks[0][y]);

            for (int x = 1; x < width; x++) {
                if (blocks[x - 1][y].getTargetColor() == blocks[x][y].getTargetColor()) {
                    length++;
                    blocksInPatterns.push(blocks[x][y]);
                } else {
                    if (length < 3) {
                        while (length > 0) {
                            blocksInPatterns.pop();
                            length--;
                        }
                    }

                    length = 1;
                    blocksInPatterns.push(blocks[x][y]);
                }
            }

            if (length < 3) {
                while (length > 0) {
                    blocksInPatterns.pop();
                    length--;
                }
            }
        }
        
        return blocksInPatterns;
    }

    public boolean canFinish() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Math.abs(blocks[x][y].getCurrentExpansion() - 0) > 0.0025)
                    return false;
            }
        }

        return findPatterns().size() == 0;
    }

    public void addFloatingPoint(Block block, int points) {
        PointF position = block.getCalculatedPosition();

        floatingPoints.add(new FloatingPoint(position.x + Block.getSize() * 0.5f, position.y, points));
    }

    public boolean onTouchEvent(GameLogic logic, MotionEvent event) {
        if (blocks[0][0] == null)
            return false;

        boolean handled = false;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (blocks[x][y].onTouchEvent(logic, event)) {
                    handled = true;
                    break;
                }
            }

            if (handled)
                break;
        }

        return handled;
    }

    public void setTranslation(PointF translation) {
        this.translation = translation;
    }

    public PointF getTranslation() {
        return translation;
    }
}
