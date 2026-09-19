package cn.pickup.launcher;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * 静态毛玻璃面板：半透填充 + 1px 高光描边。
 * 不带阴影，阴影由 View.setElevation() 提供（更原生）。
 *
 * "毛玻璃"质感在 Android 真机上：
 *  - API 31+ 配合 RenderEffect.createBlurEffect() 实现真模糊（高版本体验更好）
 *  - API 23~30 用半透叠加做"视觉等效"
 */
final class GlassPanelDrawable extends Drawable {
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private int fill;
    private int stroke;
    private final float cornerRadius;

    GlassPanelDrawable(int fill, int stroke, float cornerRadius) {
        this.fill = fill;
        this.stroke = stroke;
        this.cornerRadius = cornerRadius;
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1f);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) {
            return;
        }
        rect.set(b.left + 0.5f, b.top + 0.5f, b.right - 0.5f, b.bottom - 0.5f);
        fillPaint.setColor(fill);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint);
        strokePaint.setColor(stroke);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint);
    }

    @Override public void setAlpha(int alpha) { fillPaint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter cf) { fillPaint.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
