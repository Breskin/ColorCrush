package com.github.breskin.squares.game.toaster;

import android.graphics.Canvas;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kuba on 12.11.2018.
 */

public class PointToasts {

    private List<Toast> activeToasts;

    public PointToasts() {
        activeToasts = new ArrayList<>();
    }

    public void update() {
        for (int i=0; i<activeToasts.size(); i++) {
            activeToasts.get(i).update();

            if (!activeToasts.get(i).isVisible()) {
                activeToasts.remove(i);
                i--;
            }
        }
    }

    public void render(Canvas canvas) {
        for (int i=0; i<activeToasts.size(); i++) {
            activeToasts.get(i).render(canvas);
        }
    }

    public void add(String text, PointF position) {
        activeToasts.add(new Toast(text, position));
    }

    public void clear() {
        activeToasts.clear();
    }
}
