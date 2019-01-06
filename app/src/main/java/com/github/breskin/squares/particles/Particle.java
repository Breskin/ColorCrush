package com.github.breskin.squares.particles;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.github.breskin.squares.MainView;

import java.util.Random;

/**
 * Created by Kuba on 11.11.2018.
 */

public class Particle {

    private static Paint Paint = new Paint();
    private static Random Random = new Random();

    private PointF position, velocity;
    private float size, rotation, rotationSpeed;
    private int a, r, g, b;

    public boolean toDelete = false;

    public Particle(PointF pos, float size, int cr, int cg, int cb) {

        this.position = pos;
        this.size = size;
        this.rotation = Random.nextInt(360);
        this.rotationSpeed = Random.nextInt(20) - 10;
        this.a = 190 + Random.nextInt(65);
        this.r = cr - Random.nextInt(90); if (this.r < 0) this.r = 0; if (this.r > 255) this.r = 255;
        this.g = cg - Random.nextInt(90); if (this.g < 0) this.g = 0; if (this.g > 255) this.g = 255;
        this.b = cb - Random.nextInt(90); if (this.b < 0) this.b = 0; if (this.b > 255) this.b = 255;

        velocity = new PointF(MainView.ViewWidth / 90 * (Random.nextFloat() - 0.5f), MainView.ViewHeight / 90 * (Random.nextFloat() - 0.5f));
    }

    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;

        rotation += rotationSpeed * (MainView.FrameTime / 16f);
        if (rotation > 360) rotation -= 360;
        if (rotation < 0) rotation += 360;

        if (a > 92)
            a--;

        if (velocity.y < MainView.ViewHeight / 100)
            velocity.y += 0.5f * (MainView.FrameTime / 16f);

        if (size > MainView.ViewWidth / 200)
            size -= size * 0.025f * (MainView.FrameTime / 16f);
        else
            toDelete = true;
    }

    public void render(Canvas canvas) {
        Paint.setColor(Color.argb(a, r, g, b));
        canvas.save();

        canvas.translate(position.x, position.y);
        canvas.rotate(rotation);
        canvas.drawRoundRect(new RectF(-size / 2, -size / 2, size / 2, size / 2), 5, 5, Paint);

        canvas.restore();
    }
}
