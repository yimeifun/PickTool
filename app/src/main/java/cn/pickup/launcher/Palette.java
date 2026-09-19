package cn.pickup.launcher;

import android.graphics.Color;

/**
 * 主题调色板：W 周历版专用。
 * 亮 / 暗各一套，覆盖页面底、4 个柔光斑、玻璃面板、徽标渐变、周历高亮、文字色等。
 *
 * 静态配色：所有颜色在创建 Palette 时一次性算出，运行时不再变化（满足"静态优先、减小内存"）。
 */
final class Palette {
    final boolean dark;

    // 底层
    final int pageBackground;
    final int navigationBarColor;
    final int statusBarMode;        // 0=浅色图标（用于深底），1=深色图标（用于浅底）

    // 4 个柔光斑（Miuix 调色板：樱粉/薄荷/桃粉/淡紫）
    final int glowA, glowB, glowC, glowD;

    // 玻璃
    final int glassFill;
    final int glassStroke;
    final int glassChipFill;        // 内部小卡片（紧凑行）

    // 文字
    final int ink1, ink2, ink3;

    // 强调
    final int accentGradientStart;
    final int accentGradientEnd;

    // 周历高亮（今日格底色）
    final int calTodayFill;
    final int calTodayInk;
    final int calDot;

    // 服务强调色（5 个）
    final int cCainiao, cTaobao, cPinduoduo, cJd, cXhs;

    // 服务徽标渐变 2 端
    final int[] cCainiaoGrad = new int[2];
    final int[] cTaobaoGrad  = new int[2];
    final int[] cPinduoduoGrad = new int[2];
    final int[] cJdGrad      = new int[2];
    final int[] cXhsGrad     = new int[2];

    private Palette(boolean dark,
                    int pageBackground,
                    int navigationBarColor,
                    int statusBarMode,
                    int glowA, int glowB, int glowC, int glowD,
                    int glassFill, int glassStroke, int glassChipFill,
                    int ink1, int ink2, int ink3,
                    int accentGradientStart, int accentGradientEnd,
                    int calTodayFill, int calTodayInk, int calDot,
                    int cCainiao, int cTaobao, int cPinduoduo, int cJd, int cXhs,
                    int[] c1, int[] c2, int[] c3, int[] c4, int[] c5) {
        this.dark = dark;
        this.pageBackground = pageBackground;
        this.navigationBarColor = navigationBarColor;
        this.statusBarMode = statusBarMode;
        this.glowA = glowA; this.glowB = glowB; this.glowC = glowC; this.glowD = glowD;
        this.glassFill = glassFill; this.glassStroke = glassStroke; this.glassChipFill = glassChipFill;
        this.ink1 = ink1; this.ink2 = ink2; this.ink3 = ink3;
        this.accentGradientStart = accentGradientStart; this.accentGradientEnd = accentGradientEnd;
        this.calTodayFill = calTodayFill; this.calTodayInk = calTodayInk; this.calDot = calDot;
        this.cCainiao = cCainiao; this.cTaobao = cTaobao;
        this.cPinduoduo = cPinduoduo; this.cJd = cJd; this.cXhs = cXhs;
        System.arraycopy(c1, 0, this.cCainiaoGrad, 0, 2);
        System.arraycopy(c2, 0, this.cTaobaoGrad, 0, 2);
        System.arraycopy(c3, 0, this.cPinduoduoGrad, 0, 2);
        System.arraycopy(c4, 0, this.cJdGrad, 0, 2);
        System.arraycopy(c5, 0, this.cXhsGrad, 0, 2);
    }

    static Palette light() {
        return new Palette(
                false,
                rgb(245, 242, 236),        // pageBackground 暖米白
                rgb(245, 242, 236),
                0,                          // 浅色图标（深底时）
                // 4 个柔光斑（Miuix）
                argb(102, 255, 107, 91),    // 樱粉
                argb(82, 168, 230, 207),    // 薄荷
                argb(102, 255, 183, 197),   // 桃粉
                argb(102, 197, 176, 232),   // 淡紫
                // 玻璃
                argb(107, 255, 255, 255),   // glassFill 半透白
                argb(140, 255, 255, 255),   // glassStroke 高光描边
                argb(128, 255, 255, 255),   // glassChipFill
                // 文字
                rgb(42, 37, 32),            // ink1 主文字
                rgb(107, 94, 82),           // ink2 副
                rgb(168, 155, 142),         // ink3 三级
                // 大字渐变（17 号字）
                rgb(255, 107, 91),          // accentGradientStart MIUI 橙
                rgb(232, 74, 107),          // accentGradientEnd 樱粉
                // 周历
                rgb(255, 107, 91),          // calTodayFill 今日底（MIUI 橙）
                Color.WHITE,                // calTodayInk 今日文字
                rgb(255, 107, 91),          // calDot 圆点
                // 5 个服务色
                rgb(61, 153, 112),          // cCainiao 鲜绿
                rgb(255, 107, 91),          // cTaobao MIUI 橙
                rgb(232, 74, 107),          // cPinduoduo 樱红
                rgb(199, 122, 62),          // cJd 暖琥珀
                rgb(212, 58, 122),         // cXhs 玫红
                // 徽标渐变 (浅 → 深)
                new int[] { rgb(93, 188, 146),  rgb(61, 153, 112) },
                new int[] { rgb(255, 147, 133), rgb(255, 107, 91) },
                new int[] { rgb(244, 123, 149), rgb(232, 74, 107) },
                new int[] { rgb(226, 159, 112), rgb(199, 122, 62) },
                new int[] { rgb(236, 117, 164), rgb(212, 58, 122) }
        );
    }

    static Palette dark() {
        return new Palette(
                true,
                rgb(26, 22, 32),           // pageBackground 深紫灰
                rgb(26, 22, 32),
                1,                          // 深色图标（浅底时）
                // 暗色下光晕更鲜亮
                argb(120, 255, 107, 91),
                argb(95, 168, 230, 207),
                argb(115, 255, 183, 197),
                argb(115, 197, 176, 232),
                // 玻璃
                argb(56, 60, 50, 65),       // glassFill 半透深灰
                argb(46, 255, 255, 255),    // glassStroke
                argb(64, 60, 50, 65),       // glassChipFill
                // 文字
                rgb(245, 238, 234),
                rgb(191, 180, 172),
                rgb(122, 113, 104),
                // 大字渐变
                rgb(255, 139, 125),
                rgb(244, 123, 149),
                // 周历今日
                rgb(255, 139, 125),
                rgb(26, 22, 32),
                rgb(255, 139, 125),
                // 5 个服务色（提亮）
                rgb(93, 188, 146),
                rgb(255, 139, 125),
                rgb(244, 123, 149),
                rgb(226, 159, 112),
                rgb(236, 117, 164),
                // 渐变
                new int[] { rgb(143, 224, 181), rgb(93, 188, 146) },
                new int[] { rgb(255, 181, 172), rgb(255, 139, 125) },
                new int[] { rgb(248, 160, 181), rgb(244, 123, 149) },
                new int[] { rgb(236, 192, 147), rgb(226, 159, 112) },
                new int[] { rgb(244, 158, 189), rgb(236, 117, 164) }
        );
    }

    static Palette forMode(int mode) {
        return mode == ThemeManager.MODE_DARK ? dark() : light();
    }

    private static int rgb(int r, int g, int b) {
        return Color.rgb(r, g, b);
    }

    private static int argb(int a, int r, int g, int b) {
        return Color.argb(a, r, g, b);
    }
}
