package com.github.breskin.squares.particles;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;

import com.github.breskin.squares.RenderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
    private Random random;
    private List<Particle> Particles;
    private List<Particle> newParticles;

    private int maximumAllowed = 0;

    public ParticleSystem() {
        random = new Random();
        Particles = new ArrayList<>();
        newParticles = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 26)
            maximumAllowed = 150;
        else
            maximumAllowed = 30;
    }

    public void render(Canvas canvas) {
        for (int i = 0; i < Particles.size(); i++) {
            if (Particles.get(i) == null)
                continue;

            Particles.get(i).render(canvas);
        }
    }

    public void update() {
        if (!newParticles.isEmpty()) {
            int increase = 1;
            for (int i=0; i < newParticles.size(); i += increase) {
                Particles.add(newParticles.get(i));
            }

            newParticles.clear();
        }

        boolean purge = Particles.size() > maximumAllowed && RenderView.FrameTime > 25;
        int purgeCounter = 0;

        for (int i = 0; i < Particles.size(); i++) {
            purgeCounter++;

            if (Particles.get(i) == null ||(purge && purgeCounter % 6 == 0)) {
                Particles.remove(i);
                i--;

                continue;
            }

            Particles.get(i).update();

            if (Particles.get(i).toDelete) {
                Particles.remove(i);
                i--;
            }
        }
    }

    public void createInPoint(PointF position, float size, int count, int r, int g, int b) {
        for (int i=0; i<count; i++) {
            newParticles.add(new Particle(new PointF(position.x, position.y), size, r, g, b));
        }
    }

    public void createInArea(RectF area, float size, int count, int r, int g, int b) {
        for (int i=0; i<count; i++) {
            newParticles.add(new Particle(new PointF(area.left + area.width() * random.nextFloat(), area.top + area.height() * random.nextFloat()), size, r, g, b));
        }
    }
}
