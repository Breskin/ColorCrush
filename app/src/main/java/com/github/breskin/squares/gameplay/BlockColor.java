package com.github.breskin.squares.gameplay;

import android.graphics.Color;

import java.util.Random;

public enum BlockColor {
    Purple, Blue, Cyan, Green, Yellow, Red, None;

    private static Random random = new Random();

    public int getColor() {
        switch (this) {
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

    public static int calculateColor(BlockColor current, BlockColor target, float progress) {
        int currentColor = current.getColor(), nextColor = target.getColor();

        int cr = (currentColor >> 16) & 0xff, cg = (currentColor >> 8) & 0xff, cb = (currentColor) & 0xff;
        int nr = (nextColor >> 16) & 0xff, ng = (nextColor >> 8) & 0xff, nb = (nextColor) & 0xff;

        int ar = (int)(cr + (nr - cr) * progress), ag = (int)(cg + (ng - cg) * progress), ab = (int)(cb + (nb - cb) * progress);

        return Color.rgb(ar, ag, ab);
    }

    public BlockColor nextColor() {
        switch (this) {
            case Red:
                return BlockColor.Purple;
            case Green:
                return BlockColor.Yellow;
            case Blue:
                return BlockColor.Cyan;
            case Purple:
                return BlockColor.Blue;
            case Yellow:
                return BlockColor.Red;
            case Cyan:
                return BlockColor.Green;
        }

        return BlockColor.Red;
    }

    public static BlockColor random() {
        int next = random.nextInt(6);

        switch (next) {
            case 0:
                return BlockColor.Purple;
            case 1:
                return BlockColor.Blue;
            case 2:
                return BlockColor.Cyan;
            case 3:
                return BlockColor.Green;
            case 4:
                return BlockColor.Yellow;
            case 5:
                return BlockColor.Red;
        }

        return BlockColor.Red;
    }
}
