package com.github.breskin.squares.particles;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kuba on 11.11.2018.
 */

public class ParticleSystem {

    private Random Random;
    private List<Particle> Particles;
    private List<Particle> newParticles;

    public ParticleSystem() {
        Random = new Random();
        Particles = new ArrayList<>();
        newParticles = new ArrayList<>();
    }

    public void render(Canvas canvas) {
        update();

        for (int i = 0; i < Particles.size(); i++) {
            if (Particles.get(i) == null)
                continue;

            Particles.get(i).update();

            if (Particles.get(i).toDelete) {
                Particles.remove(i);
                i--;
            } else {
                Particles.get(i).render(canvas);
            }
        }
    }

    void update() {
        if (!newParticles.isEmpty()) {
            //float divider = (Particles.size() + newParticles.size()) * 0.35f;
            //int increase = (int)((Particles.size() + newParticles.size()) / divider);
            //if (increase < 1 || android.os.Build.VERSION.SDK_INT >= 26)
            //    increase = 1;
            int increase = 1;
            for (int i=0; i < newParticles.size(); i += increase) {
                Particles.add(newParticles.get(i));
            }

            newParticles.clear();
        }
    }

    public void createInPoint(PointF position, float size, int count, int r, int g, int b) {
        for (int i=0; i<count; i++) {
            newParticles.add(new Particle(new PointF(position.x, position.y), size, r, g, b));
        }
    }

    public void createInArea(RectF area, float size, int count, int r, int g, int b) {
        for (int i=0; i<count; i++) {
            newParticles.add(new Particle(new PointF(area.left + area.width() * Random.nextFloat(), area.top + area.height() * Random.nextFloat()), size, r, g, b));
        }
    }
}
