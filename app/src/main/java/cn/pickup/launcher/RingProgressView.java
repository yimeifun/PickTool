package cn.pickup.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * Circular download progress ring drawn with Canvas.drawArc.
 *
 * - Background track: soft light ring
 * - Foreground arc: gradient (indigo -> lavender) with rounded caps
 * - progress 0..100
 * The center text (percent / check) is rendered by an external TextView
 * layered above in a FrameLayout.
 */
public final class RingProgressView extends View {

    private int progress = 0; // 0..100
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int trackColor = 0xFFE9EDF6;
    private int startColor = 0xFF7C8CF0;
    private int endColor   = 0xFFA9BBEC;

    public RingProgressView(Context context) {
        super(context);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgress(int p) {
        progress = Math.max(0, Math.min(100, p));
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - dp(7);
        if (radius <= 0) return;
        float stroke = dp(10);

        // track
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(trackColor);
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint);

        // foreground arc (gradient)
        LinearGradient shader = new LinearGradient(
                0f, 0f, getWidth(), getHeight(),
                startColor, endColor, Shader.TileMode.CLAMP);
        arcPaint.setShader(shader);
        arcPaint.setStrokeWidth(stroke);
        float sweep = progress * 360f / 100f;
        if (sweep > 0) {
            canvas.drawArc(arcRect, -90f, sweep, false, arcPaint);
        }
    }

    private float dp(float v) {
        return getResources().getDisplayMetrics().density * v;
    }
}
