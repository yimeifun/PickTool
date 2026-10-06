package com.pickup.tool;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Material 3 风格的表面卡片。
 *
 * M3 用"色调分层"（surface / surfaceContainer 各级）来表达层次，阴影很轻，
 * 所以这里只用极少层数的柔和投影，主体完全靠填充色与背景拉开。
 *
 * 阴影在 Drawable 边界内自绘，不使用 View 的 elevation：
 * 半透明圆角面板在部分设备的软件渲染路径下，elevation 阴影会出现异常亮块。
 */
final class SurfaceDrawable extends Drawable {

    private static final int SHADOW_LAYERS = 5;
    /** 阴影带一点暖棕，和暖色底面上的卡片更贴合。 */
    private static final int SHADOW_R = 60;
    private static final int SHADOW_G = 40;
    private static final int SHADOW_B = 32;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF body = new RectF();
    private final RectF shadowRect = new RectF();

    private final float density;
    private final float radius;
    private final int fillColor;
    private final float shadowSpread;
    private final float shadowAlpha;
    private final float strokeWidth;
    private final int strokeColor;

    private int drawableAlpha = 255;

    SurfaceDrawable(
            float density,
            float radiusDp,
            int fillColor,
            float shadowDp,
            float shadowAlpha,
            float strokeWidthDp,
            int strokeColor
    ) {
        this.density = density;
        this.radius = Math.max(0f, radiusDp) * density;
        this.fillColor = fillColor;
        this.shadowSpread = Math.max(0f, shadowDp) * density;
        this.shadowAlpha = Math.max(0f, Math.min(1f, shadowAlpha));
        this.strokeWidth = Math.max(0f, strokeWidthDp) * density;
        this.strokeColor = strokeColor;

        fillPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(density * 2.2f);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        body.set(
                bounds.left + shadowSpread,
                bounds.top + shadowSpread,
                bounds.right - shadowSpread,
                bounds.bottom - shadowSpread
        );
    }

    @Override
    public void draw(Canvas canvas) {
        if (body.width() <= 0f || body.height() <= 0f) {
            return;
        }

        if (shadowSpread > 0f && shadowAlpha > 0f) {
            shadowPaint.setShader(null);
            for (int layer = SHADOW_LAYERS; layer >= 1; layer--) {
                float inflate = shadowSpread * (layer / (float) SHADOW_LAYERS);
                float strength = 1f - (layer - 1) / (float) SHADOW_LAYERS;
                int alpha = Math.round(
                        255f * shadowAlpha * 0.34f * strength * strength
                );
                if (alpha <= 0) {
                    continue;
                }
                shadowPaint.setColor(Color.argb(alpha, SHADOW_R, SHADOW_G, SHADOW_B));
                shadowRect.set(
                        body.left - inflate,
                        body.top - inflate,
                        body.right + inflate,
                        body.bottom + inflate
                );
                canvas.drawRoundRect(
                        shadowRect,
                        radius + inflate,
                        radius + inflate,
                        shadowPaint
                );
            }
        }

        fillPaint.setShader(null);
        fillPaint.setColor(applyAlpha(fillColor));
        canvas.drawRoundRect(body, radius, radius, fillPaint);

        if (strokeWidth > 0f) {
            fillPaint.setStyle(Paint.Style.STROKE);
            fillPaint.setStrokeWidth(strokeWidth);
            fillPaint.setColor(applyAlpha(strokeColor));
            float inset = strokeWidth * 0.5f;
            shadowRect.set(
                    body.left + inset,
                    body.top + inset,
                    body.right - inset,
                    body.bottom - inset
            );
            canvas.drawRoundRect(
                    shadowRect,
                    Math.max(0f, radius - inset),
                    Math.max(0f, radius - inset),
                    fillPaint
            );
            fillPaint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    public void getOutline(Outline outline) {
        if (body.isEmpty()) {
            return;
        }
        if (radius > 0f) {
            outline.setRoundRect(
                    Math.round(body.left),
                    Math.round(body.top),
                    Math.round(body.right),
                    Math.round(body.bottom),
                    radius
            );
        } else {
            outline.setRect(
                    Math.round(body.left),
                    Math.round(body.top),
                    Math.round(body.right),
                    Math.round(body.bottom)
            );
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawableAlpha == alpha) {
            return;
        }
        drawableAlpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private int applyAlpha(int color) {
        int scaled = Math.round(Color.alpha(color) * (drawableAlpha / 255f));
        return (color & 0x00FFFFFF) | (scaled << 24);
    }
}
