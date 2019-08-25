package com.github.breskin.squares.particles;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.github.breskin.squares.RenderView;

import java.util.Random;

public class Particle {

    private static Paint paint = new Paint();
    private static Random random = new Random();

    private PointF position, velocity;
    private float size, rotation, rotationSpeed;
    private int a, r, g, b;

    public boolean toDelete = false;

    public Particle(PointF pos, float size, int cr, int cg, int cb) {
        this.position = pos;
        this.size = size;
        this.rotation = random.nextInt(360);
        this.rotationSpeed = random.nextInt(20) - 10;
        this.a = 130 + random.nextInt(65);
        this.r = cr - random.nextInt(90); if (this.r < 0) this.r = 0; if (this.r > 255) this.r = 255;
        this.g = cg - random.nextInt(90); if (this.g < 0) this.g = 0; if (this.g > 255) this.g = 255;
        this.b = cb - random.nextInt(90); if (this.b < 0) this.b = 0; if (this.b > 255) this.b = 255;

        velocity = new PointF(RenderView.ViewWidth * 0.01f * (random.nextFloat() - 0.5f), RenderView.ViewHeight * 0.01f * (random.nextFloat() - 0.5f));
    }

    public void update() {
        position.x += velocity.x * (RenderView.FrameTime / 16f);
        position.y += velocity.y * (RenderView.FrameTime / 16f);

        rotation += rotationSpeed * (RenderView.FrameTime / 16f);
        if (rotation > 360) rotation -= 360;
        if (rotation < 0) rotation += 360;

        if (a > 92)
            a--;

        if (velocity.y < RenderView.ViewHeight / 100)
            velocity.y += 0.5f * (RenderView.FrameTime / 16f);

        if (size > RenderView.ViewWidth / 200)
            size -= size * 0.025f * (RenderView.FrameTime / 16f);
        else
            toDelete = true;
    }

    public void render(Canvas canvas) {
        paint.setColor(Color.argb(a, r, g, b));
        canvas.save();

        canvas.translate(position.x, position.y);
        canvas.rotate(rotation);
        canvas.drawRoundRect(new RectF(-size / 2, -size / 2, size / 2, size / 2), 5, 5, paint);

        canvas.restore();
    }
}
