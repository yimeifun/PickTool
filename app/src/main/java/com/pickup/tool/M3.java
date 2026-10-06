package com.pickup.tool;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Material 3 的配色与常用 Drawable 工厂。
 *
 * 基准色为浅肉色暖调（页面 #FBF4EE）；取件码主卡使用略暖的白，避免纯白
 * 压在肉色背景上显得刺眼。平台 tonal container 按各自品牌色微调，
 * 保证浅色容器上的深色文字对比度足够。
 */
final class M3 {
    private M3() {
    }

    // ---- surface 层级：M3 用色调深浅表达层次，而不是靠阴影 ----
    /** 页面底色，暖象牙。 */
    static final int SURFACE = Color.rgb(0xFB, 0xF4, 0xEE);
    /** 取件码主卡：暖白，比页面稍亮但不刺眼。 */
    static final int SURFACE_CONTAINER_LOWEST = Color.rgb(0xFF, 0xFC, 0xFA);
    /** 待取卡：比页面略深一档，作为次级层级。 */
    static final int SURFACE_CONTAINER_LOW = Color.rgb(0xF5, 0xEA, 0xE2);
    /** 底部胶囊按钮。 */
    static final int SURFACE_CONTAINER_HIGH = Color.rgb(0xEF, 0xE1, 0xD7);

    static final int ON_SURFACE = Color.rgb(0x23, 0x1A, 0x15);
    static final int ON_SURFACE_VARIANT = Color.rgb(0x54, 0x43, 0x3A);
    /** 比 onSurfaceVariant 更浅，用于标题后的附注、次要说明。 */
    static final int OUTLINE = Color.rgb(0x82, 0x6F, 0x63);
    static final int OUTLINE_VARIANT = Color.rgb(0xD9, 0xC8, 0xBC);

    /** 按下时的状态层，数值取 onSurface 的 8% 混合。 */
    private static final float STATE_LAYER = 0.08f;

    // ---- 各平台的 tonal container 与前景色 ----
    static final int CAINIAO_CONTAINER = Color.rgb(0xA9, 0xF2, 0xC4);
    static final int CAINIAO_ON = Color.rgb(0x00, 0x21, 0x0F);
    static final int CAINIAO_ACCENT = Color.rgb(0x14, 0x6C, 0x43);

    static final int TAOBAO_CONTAINER = Color.rgb(0xFF, 0xDC, 0xBE);
    static final int TAOBAO_ON = Color.rgb(0x2B, 0x17, 0x00);
    static final int TAOBAO_ACCENT = Color.rgb(0x8A, 0x4A, 0x00);

    static final int PINDUODUO_CONTAINER = Color.rgb(0xFF, 0xDA, 0xD6);
    static final int PINDUODUO_ON = Color.rgb(0x41, 0x00, 0x02);
    static final int PINDUODUO_ACCENT = Color.rgb(0x93, 0x00, 0x0A);

    static final int JD_CONTAINER = Color.rgb(0xFF, 0xE0, 0x8F);
    static final int JD_ON = Color.rgb(0x26, 0x1A, 0x00);
    static final int JD_ACCENT = Color.rgb(0x6D, 0x4C, 0x00);

    static final int XHS_CONTAINER = Color.rgb(0xFF, 0xD9, 0xE2);
    static final int XHS_ON = Color.rgb(0x3E, 0x00, 0x1D);
    static final int XHS_ACCENT = Color.rgb(0x8E, 0x15, 0x50);

    static final int MEITUAN_CONTAINER = Color.rgb(0xFF, 0xD8, 0xB5);
    static final int MEITUAN_ON = Color.rgb(0x31, 0x0A, 0x00);
    static final int MEITUAN_ACCENT = Color.rgb(0xA5, 0x3B, 0x00);

    static final int DOUYIN_CONTAINER = Color.rgb(0xC9, 0xED, 0xE4);
    static final int DOUYIN_ON = Color.rgb(0x00, 0x1E, 0x1A);
    static final int DOUYIN_ACCENT = Color.rgb(0x00, 0x5A, 0x52);

    /**
     * 表面卡片：普通态与按下态各一层，按下时叠加状态层颜色。
     */
    static Drawable surface(
            float density,
            float radiusDp,
            int fillColor,
            float shadowDp,
            float shadowAlpha
    ) {
        StateListDrawable states = new StateListDrawable();
        states.setEnterFadeDuration(0);
        states.setExitFadeDuration(120);
        states.addState(
                new int[] {android.R.attr.state_pressed},
                new SurfaceDrawable(
                        density,
                        radiusDp,
                        layer(fillColor),
                        0f,
                        0f,
                        0f,
                        0
                )
        );
        states.addState(
                new int[0],
                new SurfaceDrawable(
                        density,
                        radiusDp,
                        fillColor,
                        shadowDp,
                        shadowAlpha,
                        0f,
                        0
                )
        );
        return states;
    }

    /**
     * 纯色调容器，用于图标底衬这类不需要交互的元素。
     */
    static Drawable tonal(float density, float radiusDp, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(color);
        shape.setCornerRadius(radiusDp * density);
        return shape;
    }

    /**
     * 有边框的表面，用于需要更明确轮廓的容器。
     */
    static Drawable outlined(
            float density,
            float radiusDp,
            int fillColor,
            int strokeColor,
            float strokeWidthDp
    ) {
        StateListDrawable states = new StateListDrawable();
        states.setEnterFadeDuration(0);
        states.setExitFadeDuration(120);
        states.addState(
                new int[] {android.R.attr.state_pressed},
                new SurfaceDrawable(
                        density,
                        radiusDp,
                        layer(fillColor),
                        0f,
                        0f,
                        0f,
                        0
                )
        );
        states.addState(
                new int[0],
                new SurfaceDrawable(
                        density,
                        radiusDp,
                        fillColor,
                        0f,
                        0f,
                        strokeWidthDp,
                        strokeColor
                )
        );
        return states;
    }

    static Drawable transparent() {
        return new ColorDrawable(Color.TRANSPARENT);
    }

    /**
     * 在底色上叠加一层 onSurface，得到 M3 的按下状态色。
     */
    private static int layer(int base) {
        return blend(base, ON_SURFACE, STATE_LAYER);
    }

    private static int blend(int base, int overlay, float ratio) {
        return Color.rgb(
                Math.round(Color.red(base) * (1f - ratio) + Color.red(overlay) * ratio),
                Math.round(Color.green(base) * (1f - ratio) + Color.green(overlay) * ratio),
                Math.round(Color.blue(base) * (1f - ratio) + Color.blue(overlay) * ratio)
        );
    }
}
