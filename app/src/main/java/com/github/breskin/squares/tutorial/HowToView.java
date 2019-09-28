package com.github.breskin.squares.tutorial;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;

import com.github.breskin.squares.R;
import com.github.breskin.squares.RenderView;
import com.github.breskin.squares.View;

public class HowToView implements View {

    private RenderView renderView;

    private Paint paint;
    private TextPaint textPaint;

    private boolean opening = false, closing = false;

    private float alpha = 0, animationTranslation = 0;

    private String cycleInfo, tapAnywhere;
    private StaticLayout cycleInfoLayout;
    private Bitmap cycle;

    public HowToView(RenderView renderView) {
        this.renderView = renderView;

        this.paint = new Paint();
        this.textPaint = new TextPaint();

        textPaint.setAntiAlias(true);
    }

    @Override
    public void update() {
        if (opening) {
            alpha += (1 - alpha) * 0.1f;
            animationTranslation += (0 - animationTranslation) * 0.1f;
        } else if (closing) {
            alpha += (0 - alpha) * 0.1f;
            animationTranslation += (RenderView.ViewHeight * 0.95f - animationTranslation) * 0.1f;

            if (alpha < 0.05f) {
                renderView.switchView(RenderView.ViewType.Game);
            }
        }
    }

    @Override
    public void render(Canvas canvas) {
        float margin = animationTranslation + RenderView.ViewHeight * 0.1f;

        if (cycleInfoLayout == null) {
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(RenderView.ViewWidth * 0.06f);
            if (Build.VERSION.SDK_INT >= 23) {
                cycleInfoLayout = StaticLayout.Builder.obtain(cycleInfo, 0, cycleInfo.length(), textPaint, RenderView.ViewWidth * 9 / 10).setAlignment(Layout.Alignment.ALIGN_CENTER).setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY).build();
            } else {
                cycleInfoLayout = new StaticLayout(cycleInfo, 0, cycleInfo.length(), textPaint, RenderView.ViewWidth * 9 / 10, Layout.Alignment.ALIGN_CENTER, 1, 0, true);
            }
        }

        canvas.save();
        canvas.translate(RenderView.ViewWidth * 0.05f, margin);
        cycleInfoLayout.draw(canvas);
        canvas.restore();

        margin += cycleInfoLayout.getHeight() + RenderView.ViewWidth * 0.1f;

        paint.setColor(Color.argb((int)(alpha * 255), 255, 255, 255));
        float y = cycle.getHeight() * RenderView.ViewWidth * 0.7f / cycle.getWidth();
        canvas.drawBitmap(cycle, new Rect(0, 0, cycle.getWidth(), cycle.getHeight()), new RectF(RenderView.ViewWidth * 0.15f, margin, RenderView.ViewWidth * 0.85f, margin + y), paint);


        paint.setTextSize(RenderView.ViewWidth * 0.0375f);
        canvas.drawText(tapAnywhere, (RenderView.ViewWidth - paint.measureText(tapAnywhere)) * 0.5f, animationTranslation + RenderView.ViewHeight * 0.975f - paint.getTextSize(), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            closing = true;
            opening = false;

            return true;
        }

        return false;
    }

    @Override
    public boolean onBackPressed() {
        closing = true;
        opening = false;

        return true;
    }

    @Override
    public void open() {
        opening = true;
        closing = false;

        alpha = 0;
        animationTranslation = RenderView.ViewHeight * 0.75f;
    }

    public void load(Context context) {
        cycleInfo = context.getString(R.string.tutorial_cycle_info);
        tapAnywhere = context.getString(R.string.result_tap_to_continue);

        cycle = BitmapFactory.decodeResource(context.getResources(), R.drawable.cycle);
    }
}
