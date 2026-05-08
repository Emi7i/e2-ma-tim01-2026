package com.example.slagalica.presentation.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.slagalica.R;

public class PieChartDrawable extends Drawable {
    private Paint paintBackground;
    private Paint paintForeground;
    private Paint paintBorder;
    private RectF rectF;
    private float progress = 0.55f; // Default 55%

    public PieChartDrawable(Context context) {
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(ContextCompat.getColor(context, R.color.red));
        paintBackground.setStyle(Paint.Style.FILL);

        paintForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintForeground.setColor(ContextCompat.getColor(context, R.color.green));
        paintForeground.setStyle(Paint.Style.FILL);

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(ContextCompat.getColor(context, R.color.red));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(8f);

        rectF = new RectF();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - 4f;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw background circle (red for losses)
        canvas.drawCircle(centerX, centerY, radius, paintBackground);

        // Draw foreground arc (green for wins)
        float sweepAngle = progress * 360f;
        canvas.drawArc(rectF, -90f, sweepAngle, true, paintForeground);

        // Draw border
        canvas.drawCircle(centerX, centerY, radius, paintBorder);
    }

    @Override
    public void setAlpha(int alpha) {
        paintBackground.setAlpha(alpha);
        paintForeground.setAlpha(alpha);
        paintBorder.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paintBackground.setColorFilter(colorFilter);
        paintForeground.setColorFilter(colorFilter);
        paintBorder.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
