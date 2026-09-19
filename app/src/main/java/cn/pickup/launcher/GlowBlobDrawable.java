package cn.pickup.launcher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * 静态柔光斑：圆心实色 → 边缘完全透明，铺在背景层。
 * 一次性绘制后不再变化，零运行时开销。
 */
final class GlowBlobDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color;
    private final float radiusFactor;

    GlowBlobDrawable(int color, float radiusFactor) {
        this.color = color;
        this.radiusFactor = radiusFactor;
        paint.setShader(null);  // 在 draw() 里根据 bounds 重设
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) {
            return;
        }
        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        float r = Math.min(b.width(), b.height()) * radiusFactor;
        paint.setShader(new RadialGradient(
                cx, cy, r,
                new int[] { color, Color.TRANSPARENT },
                new float[] { 0f, 1f },
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(cx, cy, r, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter cf) { paint.setColorFilter(cf); }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
}
