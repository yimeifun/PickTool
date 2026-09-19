package cn.pickup.launcher;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Calendar;

public final class MainActivity extends Activity {
    static final String ACTION_OPEN = "cn.pickup.launcher.OPEN";
    static final String EXTRA_DESTINATION = "destination";

    // package-private so anonymous inner classes (e.g. buildGlowLayer's FrameLayout) can access it
    Palette palette;
    // 每日一言 TextView（buildDateBlock 创建后赋值，onCreate 末尾异步 setText）
    TextView dailyQuoteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        palette = Palette.forMode(ThemeManager.currentMode(this));

        Destination destination = destinationFromIntent();
        if (destination != null) {
            DeepLinkLauncher.open(this, destination);
            finish();
            return;
        }

        applySystemBars();
        setContentView(buildContent());
        // 拉取每日一言（hitokoto.cn），网络失败用本地默认句
        DailyQuote.fetch(this, new DailyQuote.Callback() {
            @Override public void onQuote(String quote) {
                if (dailyQuoteView != null) dailyQuoteView.setText(weekdayZh() + " \u00b7 " + quote);
            }
        });
    
    // Auto-check for updates in background, silently.
    // Only a dialog appears when a newer version exists.
    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
        @Override public void run() {
            checkForUpdate(true);
        }
    }, 1500);
}

    private void applySystemBars() {
        // 让 App 内容延展到状态栏 / 导航栏背后（透明沉浸）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            // 鐘舵€佹爮鍥炬爣棰滆壊锛堜寒/鏆楋級
            if (palette.statusBarMode == 0) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            // 璁?decor view 鐨勫唴瀹瑰欢浼稿埌 status bar / nav bar 鑳屽悗
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                   | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                   | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (palette.statusBarMode == 0) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private Destination destinationFromIntent() {
        String action = getIntent().getAction();
        if (ACTION_OPEN.equals(action)) {
            return Destination.fromKey(getIntent().getStringExtra(EXTRA_DESTINATION));
        }
        if ("cn.pickup.launcher.OPEN_CAINIAO".equals(action)) return Destination.CAINIAO;
        if ("cn.pickup.launcher.OPEN_TAOBAO".equals(action)) return Destination.TAOBAO;
        if ("cn.pickup.launcher.OPEN_PINDUODUO".equals(action)) return Destination.PINDUODUO;
        if ("cn.pickup.launcher.OPEN_TAOBAO_PENDING".equals(action)) return Destination.TAOBAO_PENDING;
        if ("cn.pickup.launcher.OPEN_PINDUODUO_PENDING".equals(action)) return Destination.PINDUODUO_PENDING;
        if ("cn.pickup.launcher.OPEN_JD".equals(action)) return Destination.JD;
        if ("cn.pickup.launcher.OPEN_XHS".equals(action)) return Destination.XHS;
        return null;
    }

    // ============================================================
    //  Content
    // ============================================================

    private View buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(palette.pageBackground);

        // 1) 鑳屾櫙鏌斿厜鏂戝眰锛堜竴娆℃€х粯鍒讹紝闆跺姩鐢伙級
        root.addView(buildGlowLayer(), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 2) 婊氬姩鍖?鈥?鐢?status bar 楂樺害鐨?paddingTop 闃叉鍐呭渚靛叆鐘舵€佹爮
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        // paddingTop = 状态栏高度（避免 TODAY/PICKUP 被压到状态栏后面）
        // paddingBottom = 导航栏高度 + 留白（避免 footer 碰到 home indicator）
        column.setPadding(dp(16),
                getStatusBarHeight() + dp(8),
                dp(16),
                getNavigationBarHeight() + dp(16));
        scroll.addView(column, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        // 3) 澶ф棩鏈熷潡
        column.addView(buildDateBlock());

        // 4) 周历条
        column.addView(buildWeekStrip());

        // 5) 今日分组（取件码）— 3 个平台（菜鸟 / 淘宝 / 拼多多）
        column.addView(buildDayGroup("打开身份码", "3 平台", palette.cTaobao,
                new EntryDef(Destination.CAINIAO,  "菜鸟驿站取件",   "优先打开身份码",     palette.cCainiaoGrad,    "››"),
                new EntryDef(Destination.TAOBAO,   "淘宝取件",       "打开取件码页面",     palette.cTaobaoGrad,     "››"),
                new EntryDef(Destination.PINDUODUO,"拼多多取件",     "打开多多买菜身份码", palette.cPinduoduoGrad,  "››")
        ));

        // 6) 待取分组
        column.addView(buildDayGroup("查看待取", "4 平台", palette.cPinduoduo,
                new EntryDef(Destination.TAOBAO_PENDING,    "淘宝待取快递",   "末端驿站待取列表",   palette.cTaobaoGrad,    "››"),
                new EntryDef(Destination.PINDUODUO_PENDING, "拼多多待取快递", "包裹待取列表",       palette.cPinduoduoGrad, "››"),
                new EntryDef(Destination.JD,               "京东待取快递",   "京东订单列表",       palette.cJdGrad,        "››"),
                new EntryDef(Destination.XHS,              "小红书待取快递", "小红书订单列表",     palette.cXhsGrad,       "››")
        ));

        // 7) 添加到桌面
        column.addView(buildShortcutBoard());

        // 7.5) 检查更新
        column.addView(buildUpdateCard());

        // 7.55) 桌面小组件配置
        column.addView(buildWidgetConfigCard());

        // 7.6) 关于软件
        column.addView(buildAboutCard());

        // 8) Footer
        column.addView(buildFooter());

        return root;
    }

    // ============================================================
    //  Date Block锛堥《閮ㄥぇ鏃ユ湡锛?    // ============================================================

    private View buildDateBlock() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(6), dp(14), dp(6), dp(18));

        // 宸︿晶鏂囧瓧
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);

        TextView lbl = text("TODAY \u00b7 " + monthAbbr() + " " + dayOfMonth(),
                10, palette.ink3, Typeface.NORMAL);
        lbl.setLetterSpacing(0.25f);
        left.addView(lbl);

        // 澶ф暟瀛楋紙鐢?LinearGradient 鐫€鑹诧級
        TextView big = new TextView(this) {
            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                if (w > 0 && h > 0) {
                    Paint p = getPaint();
                    p.setShader(new LinearGradient(
                            0, 0, w, h,
                            new int[] { palette.accentGradientStart, palette.accentGradientEnd },
                            null,
                            Shader.TileMode.CLAMP));
                    invalidate();
                }
            }
        };
        big.setText(String.valueOf(dayOfMonth()));
        big.setTextSize(64);
        big.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        big.setIncludeFontPadding(false);
        big.setLetterSpacing(-0.05f);
        big.setLineSpacing(0, 1f);
        LinearLayout.LayoutParams bigParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bigParams.topMargin = dp(4);
        left.addView(big, bigParams);

        TextView sub = text(weekdayZh(),
                12, palette.ink2, Typeface.NORMAL);
        dailyQuoteView = sub;
        // 每日一言：onCreate 末尾异步拉取后 setText
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        left.addView(sub, subParams);

        row.addView(left, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // 右侧：主题切换按钮 + 标题
        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);

        TextView themeBtn = text(palette.dark ? "\u263d" : "\u263e", 16, palette.ink1, Typeface.NORMAL);
        themeBtn.setGravity(Gravity.CENTER);
        themeBtn.setBackground(glassPanel(palette, dp(12)));
        themeBtn.setClickable(true);
        themeBtn.setFocusable(true);
        themeBtn.setContentDescription("切换深色 / 浅色主题");
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { ThemeManager.toggle(MainActivity.this); }
        });
        LinearLayout.LayoutParams tbParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        tbParams.gravity = Gravity.END;
        right.addView(themeBtn, tbParams);

        LinearLayout.LayoutParams verBlockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        verBlockParams.topMargin = dp(8);
        verBlockParams.gravity = Gravity.END;

        LinearLayout verBlock = new LinearLayout(this);
        verBlock.setOrientation(LinearLayout.VERTICAL);
        verBlock.setGravity(Gravity.END);
        TextView v1 = text("PICKUP", 10, palette.ink3, Typeface.NORMAL);
        v1.setLetterSpacing(0.25f);
        verBlock.addView(v1);
        TextView v2 = text("取件助手", 20, palette.ink1, Typeface.BOLD);
        v2.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        verBlock.addView(v2);
        right.addView(verBlock, verBlockParams);

        row.addView(right);

        return row;
    }

    // ============================================================
    //  Week Strip锛堟í鍚?7 鏍煎懆鍘嗭級
    // ============================================================

    private View buildWeekStrip() {
        // 鐜荤拑闈㈡澘
        LinearLayout week = new LinearLayout(this);
        week.setOrientation(LinearLayout.HORIZONTAL);
        week.setWeightSum(7f);
        week.setPadding(dp(8), dp(12), dp(8), dp(12));
        week.setBackground(glassPanel(palette, dp(22)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            week.setElevation(dp(2));
        }

        Calendar today = Calendar.getInstance();
        int todayDow = today.get(Calendar.DAY_OF_WEEK);  // 1=Sun ... 7=Sat
        // 鎴戜滑瑕佽 Mon..Sun 鎺掑垪锛屽懆涓€涓哄垪 0
        int mondayOffset = (todayDow == Calendar.SUNDAY) ? 6 : todayDow - Calendar.MONDAY;
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.DAY_OF_MONTH, -mondayOffset);

        String[] dows = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < 7; i++) {
            boolean isToday = i == mondayOffset;
            int num = day.get(Calendar.DAY_OF_MONTH);
            // 周三、周五显示取件小圆点（仅作视觉演示——今天 / 周三 / 周五有包裹）
            boolean hasPickup = isToday || i == 4;
            week.addView(buildDayCell(dows[i], num, isToday, hasPickup), new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.bottomMargin = dp(18);
        week.setLayoutParams(outer);
        return week;
    }

    private View buildDayCell(String dow, int num, boolean isToday, boolean hasPickup) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(0, dp(6), 0, dp(6));
        if (isToday) {
            // 浠婃棩鐢ㄥ己璋冭壊瀹炲績鍦嗚鑳屾櫙
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(palette.calTodayFill);
            bg.setCornerRadius(dp(12));
            cell.setBackground(bg);
        } else {
            cell.setBackground(null);
        }

        TextView dowView = text(dow, 9, isToday ? palette.calTodayInk : palette.ink3, Typeface.NORMAL);
        dowView.setLetterSpacing(0.1f);
        dowView.setGravity(Gravity.CENTER);
        cell.addView(dowView);

        TextView numView = text(String.valueOf(num), 14,
                isToday ? palette.calTodayInk : palette.ink1, Typeface.BOLD);
        numView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        np.topMargin = dp(2);
        cell.addView(numView, np);

        View dot = new View(this);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setSize(dp(4), dp(4));
        dotBg.setColor(isToday ? palette.calTodayInk : (hasPickup ? palette.calDot : Color.TRANSPARENT));
        dot.setBackground(dotBg);
        LinearLayout.LayoutParams dp_ = new LinearLayout.LayoutParams(dp(4), dp(4));
        dp_.topMargin = dp(4);
        cell.addView(dot, dp_);

        return cell;
    }

    // ============================================================
    //  Day Group锛堟寜鏃ュ垎缁勭殑灏忔爣棰?+ 绱у噾琛屽垪琛級
    // ============================================================

    private static class EntryDef {
        final Destination dest;
        final String title;
        final String sub;
        final int[] grad;
        final String timeTag;
        EntryDef(Destination d, String t, String s, int[] g, String tag) {
            this.dest = d; this.title = t; this.sub = s; this.grad = g; this.timeTag = tag;
        }
    }

    private View buildDayGroup(String label, String countText, int pinColor, EntryDef... entries) {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);

        // 澶撮儴锛氬僵鑹?pin + 鏍囬 + 鏁伴噺
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(4), dp(0), dp(4), dp(8));
        View pin = new View(this);
        GradientDrawable pinBg = new GradientDrawable();
        pinBg.setColor(pinColor);
        pinBg.setCornerRadius(dp(2));
        pin.setBackground(pinBg);
        head.addView(pin, new LinearLayout.LayoutParams(dp(4), dp(14)));
        TextView lbl = text(label, 12, palette.ink1, Typeface.BOLD);
        lbl.setLetterSpacing(0.05f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(8);
        head.addView(lbl, lp);
        TextView cnt = text(countText, 10, palette.ink3, Typeface.NORMAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.leftMargin = dp(8);
        head.addView(cnt, cp);
        group.addView(head);

        // 卡片列表
        for (EntryDef e : entries) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(12), dp(12), dp(12));
            row.setBackground(glassPanel(palette, dp(18)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                row.setElevation(dp(2));
            }
            row.setClickable(true);
            row.setFocusable(true);
            row.setContentDescription("打开" + e.dest.title);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { DeepLinkLauncher.open(MainActivity.this, e.dest); }
            });

            // 徽标（带汉字）
            View badge = makeBadge(e.grad, e.dest.mark);
            row.addView(badge, new LinearLayout.LayoutParams(dp(40), dp(40)));

            // 文字
            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            bodyParams.leftMargin = dp(12);
            TextView t1 = text(e.title, 13, palette.ink1, Typeface.BOLD);
            body.addView(t1);
            TextView t2 = text(e.sub, 10, palette.ink2, Typeface.NORMAL);
            LinearLayout.LayoutParams t2p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            t2p.topMargin = dp(2);
            body.addView(t2, t2p);
            row.addView(body, bodyParams);

            // 右侧双层角引号箭头（"››" 替代原本的 NOW/TODAY 标签）
            TextView arrow = text(e.timeTag, 24, palette.ink3, Typeface.NORMAL);
            arrow.setIncludeFontPadding(false);
            arrow.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(
                    dp(28), ViewGroup.LayoutParams.MATCH_PARENT);
            row.addView(arrow, arrowParams);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = dp(8);
            group.addView(row, rowParams);
        }
        return group;
    }

    // ============================================================
    //  Shortcut Board锛堟坊鍔犲埌妗岄潰 鈥?3+4 缃戞牸锛?    // ============================================================

    private View buildShortcutBoard() {
        LinearLayout board = new LinearLayout(this);
        board.setOrientation(LinearLayout.VERTICAL);
        board.setPadding(dp(14), dp(14), dp(14), dp(14));
        board.setBackground(glassPanel(palette, dp(22)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            board.setElevation(dp(2));
        }
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.topMargin = dp(6);
        board.setLayoutParams(outer);

        // 澶撮儴
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        TextView lbl = text("添加到桌面", 12, palette.ink1, Typeface.BOLD);
        lbl.setLetterSpacing(0.05f);
        head.addView(lbl);
        TextView hint = text("一键固定（需开启添加快捷方式权限）", 10, palette.ink3, Typeface.NORMAL);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.leftMargin = dp(8);
        head.addView(hint, hp);
        LinearLayout.LayoutParams headParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headParams.bottomMargin = dp(16);
        board.addView(head, headParams);

        // 第一行：3 个取件码
                addShortcutRow(board, 3, new EntryDef[] {
                        new EntryDef(Destination.CAINIAO,   "菜鸟", null, palette.cCainiaoGrad,   null),
                        new EntryDef(Destination.TAOBAO,    "淘宝", null, palette.cTaobaoGrad,    null),
                        new EntryDef(Destination.PINDUODUO, "拼多", null, palette.cPinduoduoGrad, null)
                });

                // 第二行：4 个待取
                addShortcutRow(board, 4, new EntryDef[] {
                        new EntryDef(Destination.TAOBAO_PENDING,    "淘待", null, palette.cTaobaoGrad,    null),
                        new EntryDef(Destination.PINDUODUO_PENDING, "拼待", null, palette.cPinduoduoGrad, null),
                        new EntryDef(Destination.JD,                "京东", null, palette.cJdGrad,         null),
                        new EntryDef(Destination.XHS,               "红书", null, palette.cXhsGrad,        null)
                });

        return board;
    }

    private void addShortcutRow(LinearLayout parent, int count, EntryDef[] defs) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(count);
        for (int i = 0; i < defs.length; i++) {
            EntryDef e = defs[i];
            LinearLayout chip = new LinearLayout(this);
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(8), dp(14), dp(8), dp(14));
            chip.setBackground(chipBackground(palette));
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setContentDescription("\u5c06" + e.dest.title + "\u6dfb\u52a0\u5230\u684c\u9762");
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { ShortcutPinning.request(MainActivity.this, e.dest); }
            });

            View dot = new View(this);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(e.grad[1]);
            dot.setBackground(dotBg);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
            chip.addView(dot, dotLp);

            // chip 鏂囨湰锛氱敤 0dp + weight 璁╁畠鍦?chip 鍐呴儴鎸夋瘮渚嬫媺浼革紝鑰屼笉鏄?wrap_content 鎾戠牬 chip
            TextView tv = text(e.title, 12, palette.ink1, Typeface.BOLD);
            tv.setSingleLine(true);
            tv.setGravity(Gravity.CENTER);
            tv.setMaxLines(1);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvParams.leftMargin = dp(6);
            chip.addView(tv, tvParams);

            // 澶栧眰 chip锛氱敤 0dp + weight 璁╂墍鏈?chip 绛夊垎 row 瀹藉害
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) {
                params.leftMargin = dp(8);
            }
            row.addView(chip, params);
        }
        if (parent.getChildCount() > 0) {
            // 涓よ涔嬮棿鐣欓棿璺濓紱棣栬涓嶅姞 topMargin
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (parent.getChildCount() > 1) {
                rowParams.topMargin = dp(10);
            }
            parent.addView(row, rowParams);
        }
    }

        /** Get current app versionName from PackageInfo (avoids needing
     *  generated BuildConfig, which manual pipeline doesn't compile). */
    private String currentVersionName() {
        try {
            android.content.pm.PackageInfo pi =
                    getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName == null ? "0.0.0" : pi.versionName;
        } catch (Exception e) {
            return "0.0.0";
        }
    }

    private View buildUpdateCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(glassPanel(palette, dp(22)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(2));
        }
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.topMargin = dp(6);
        card.setLayoutParams(outer);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView lbl = text("检查更新", 12, palette.ink1, Typeface.BOLD);
        lbl.setLetterSpacing(0.05f);
        head.addView(lbl);

        TextView hint = text("当前版本 v" + currentVersionName(),
                10, palette.ink3, Typeface.NORMAL);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.leftMargin = dp(8);
        head.addView(hint, hp);

        TextView arrow = text("›", 18, palette.ink3, Typeface.NORMAL);
        arrow.setGravity(Gravity.CENTER);
        arrow.setIncludeFontPadding(false);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                dp(22), LinearLayout.LayoutParams.WRAP_CONTENT);
        ap.leftMargin = dp(8);
        head.addView(arrow, ap);

        LinearLayout.LayoutParams headParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headParams.bottomMargin = dp(6);
        card.addView(head, headParams);

        TextView sub = text("点击检查最新版",
                10, palette.ink3, Typeface.NORMAL);
        card.addView(sub);

        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription("检查更新");
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                checkForUpdate(false);
            }
        });
        return card;
    }

    // ============================================================
    //  Update via Gitee Releases API
    // ============================================================

    private static final String GITEE_REPO_OWNER = "yimei-fun";
    private static final String GITEE_REPO_NAME  = "picktool";
    private static final String GITEE_RELEASES_URL =
            "https://gitee.com/" + GITEE_REPO_OWNER + "/" + GITEE_REPO_NAME + "/releases";

    private void checkForUpdate(boolean silent) {
        if (!silent) {
            android.widget.Toast.makeText(this,
                    "正在检查更新...",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
        new UpdateTask(silent).execute(GITEE_RELEASES_URL);
    }


    /** Background fetch + JSON parse; updates UI on main thread. */
    private class UpdateTask extends android.os.AsyncTask<String, Void, String> {
        private final boolean silent;
        UpdateTask(boolean silent) { this.silent = silent; }

        @Override protected String doInBackground(String... urls) {
            String url = urls[0];
            try {
                java.net.URL u = new java.net.URL(url);
                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) u.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Linux; Android 13) PickTool/1.0.1");
                conn.setRequestProperty("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                int code = conn.getResponseCode();
                if (code != 200) return "HTTP_" + code;
                java.io.InputStream is = conn.getInputStream();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) > 0) baos.write(buf, 0, n);
                is.close();
                conn.disconnect();
                return new String(baos.toByteArray(), "UTF-8");
            } catch (Exception e) {
                return "ERR_" + e.getClass().getSimpleName();
            }
        }

        @Override protected void onPostExecute(String body) {
            if (body == null || body.startsWith("ERR_") || body.startsWith("HTTP_")) {
                if (!silent) {
                    android.widget.Toast.makeText(MainActivity.this,
                            "检查失败，请稍后重试",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
                return;
            }
            try {
                // Parse the Gitee Releases HTML page (API v5 returns 403
                // for anonymous requests; HTML list is always public).
                String latest = "";
                String name = "";
                String apkUrl = null;
                String changelog = "";
                java.util.regex.Matcher tagMatcher =
                        java.util.regex.Pattern.compile(
                                "data-tag='([0-9][0-9.]*)'").matcher(body);
                if (tagMatcher.find()) {
                    latest = tagMatcher.group(1);
                }
                java.util.regex.Matcher nameMatcher =
                        java.util.regex.Pattern.compile(
                                "class=\"title\"[^>]*>([^<]{1,60})</a>").matcher(body);
                if (nameMatcher.find()) {
                    name = nameMatcher.group(1).trim();
                }
                java.util.regex.Matcher apkMatcher =
                        java.util.regex.Pattern.compile(
                                "href=\"([^\"]*releases/download/[^\"]*\\.apk)\"").matcher(body);
                if (apkMatcher.find()) {
                    apkUrl = "https://gitee.com" + apkMatcher.group(1);
                }
                String htmlUrl = GITEE_RELEASES_URL;
                if (latest.isEmpty()) {
                    if (!silent) {
                        android.widget.Toast.makeText(MainActivity.this,
                                "检查失败，请稍后重试",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                String current = currentVersionName();
                int cmp = compareVersion(latest, current);
                if (cmp <= 0) {
                    if (!silent) {
                        android.widget.Toast.makeText(MainActivity.this,
                                "已是最新版本 v" + current,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showUpdateDialog(latest, name, changelog, htmlUrl, apkUrl);
                }
            } catch (Exception e) {
                if (!silent) {
                    android.widget.Toast.makeText(MainActivity.this,
                            "检查失败，请稍后重试",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /** Return positive if a > b, 0 if equal, negative if a < b. */
    private static int compareVersion(String a, String b) {
        String[] pa = a.replaceFirst("^v", "").split("\\.");
        String[] pb = b.replaceFirst("^v", "").split("\\.");
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int na = i < pa.length ? parseIntSafe(pa[i]) : 0;
            int nb = i < pb.length ? parseIntSafe(pb[i]) : 0;
            if (na != nb) return Integer.compare(na, nb);
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void showUpdateDialog(String latestTag, String name,
                              String changelog, String htmlUrl, String apkUrl) {
        final android.app.AlertDialog dialog =
            new android.app.AlertDialog.Builder(this).create();
    dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

final String finalHtmlUrl = htmlUrl;
    final String finalApkUrl  = apkUrl;
    final boolean hasApk = apkUrl != null;

    // Outer vertical: icon header + version text + changelog card + actions
    android.widget.LinearLayout root =
            new android.widget.LinearLayout(this);
    root.setOrientation(android.widget.LinearLayout.VERTICAL);
    // rounded card container
    root.setBackground(buildCardBg(0xFFFFFFFF, dp(24)));
    root.setClipToOutline(true);

    // ---- 1. HERO HEADER (gradient bg + big icon + version) ----
    android.widget.LinearLayout hero = new android.widget.LinearLayout(this);
    hero.setOrientation(android.widget.LinearLayout.VERTICAL);
    hero.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
    int padH = dp(20);
    hero.setPadding(padH, dp(28), padH, dp(22));
    android.graphics.drawable.GradientDrawable heroBg = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            new int[] { 0xFFEEF2FF, 0xFFE8ECFA });
    heroBg.setCornerRadii(new float[]{0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0});
    hero.setBackground(heroBg);
    android.widget.LinearLayout.LayoutParams heroLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    root.addView(hero, heroLp);

    // app icon (ImageView with ic_launcher drawable)
    android.widget.ImageView icon = new android.widget.ImageView(this);
    icon.setImageResource(R.drawable.ic_launcher);
    android.widget.LinearLayout.LayoutParams iconLp =
            new android.widget.LinearLayout.LayoutParams(dp(76), dp(76));
    iconLp.bottomMargin = dp(14);
    hero.addView(icon, iconLp);

    // big version number (smaller, dark ink color, centered)
    android.widget.TextView vbig = new android.widget.TextView(this);
    vbig.setText("v" + latestTag);
    vbig.setTextSize(22);
    vbig.setTypeface(android.graphics.Typeface.create("sans-serif-medium",
            android.graphics.Typeface.BOLD));
    vbig.setTextColor(palette.ink1);
    vbig.setGravity(android.view.Gravity.CENTER);
    android.widget.LinearLayout.LayoutParams vbigLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    hero.addView(vbig, vbigLp);

    // subtitle
    android.widget.TextView sub = new android.widget.TextView(this);
    sub.setText(hasApk ? (name.isEmpty() ? "PickTool \u53ef\u7528\u66f4\u65b0" : name + " \u53ef\u7528\u66f4\u65b0")
                          : "PickTool \u53d1\u5e03\u4e86\u65b0\u7248\u672c");
    sub.setTextSize(13);
    sub.setTextColor(0xFF6B7280);
    sub.setGravity(android.view.Gravity.CENTER);
    android.widget.LinearLayout.LayoutParams subLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    subLp.topMargin = dp(4);
    hero.addView(sub, subLp);

    // current version
    android.widget.TextView cur = new android.widget.TextView(this);
    cur.setText("\u5f53\u524d\u7248\u672c v" + currentVersionName() + " \u00b7 110 KB");
    cur.setTextSize(11);
    cur.setTextColor(0xFF8A9099);
    cur.setGravity(android.view.Gravity.CENTER);
    android.widget.LinearLayout.LayoutParams curLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    curLp.topMargin = dp(8);
    hero.addView(cur, curLp);

    // ---- 2. CHANGELOG CARD ----
    if (!changelog.isEmpty()) {
        android.widget.LinearLayout wrap = new android.widget.LinearLayout(this);
        wrap.setOrientation(android.widget.LinearLayout.VERTICAL);
        int wrapPad = dp(20);
        wrap.setPadding(wrapPad, dp(16), wrapPad, dp(4));
        android.widget.LinearLayout.LayoutParams wrapLp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        root.addView(wrap, wrapLp);

        android.widget.TextView changelogTitle = new android.widget.TextView(this);
        changelogTitle.setText("\ud83d\udccb  \u66f4\u65b0\u65e5\u5fd7");
        changelogTitle.setTextSize(12);
        changelogTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        changelogTitle.setTextColor(palette.ink1);
        android.widget.LinearLayout.LayoutParams ctlp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        ctlp.bottomMargin = dp(8);
        wrap.addView(changelogTitle, ctlp);

        android.widget.TextView changelogBody = new android.widget.TextView(this);
        changelogBody.setText(changelog);
        changelogBody.setTextSize(12);
        changelogBody.setTextColor(0xFF5A6472);
        changelogBody.setLineSpacing(0, 1.4f);
        changelogBody.setBackground(buildCardBg(0xFFF6F8FC, dp(14)));
        int cp = dp(14);
        changelogBody.setPadding(cp, cp, cp, cp);
        android.widget.LinearLayout.LayoutParams cblp =
                new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        cblp.bottomMargin = dp(8);
        wrap.addView(changelogBody, cblp);
    }

    // ---- 3. ACTIONS ----
    android.widget.LinearLayout actions = new android.widget.LinearLayout(this);
    actions.setOrientation(android.widget.LinearLayout.VERTICAL);
    int ap = dp(20);
    actions.setPadding(ap, dp(8), ap, dp(20));
    android.widget.LinearLayout.LayoutParams apLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    root.addView(actions, apLp);

    // primary big button
    android.widget.Button pri = new android.widget.Button(this);
    pri.setText(hasApk ? "\u7acb\u5373\u4e0b\u8f7d" : "\u67e5\u770b\u8be6\u60c5");
    pri.setTextSize(15);
    pri.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
    pri.setTextColor(0xFFFFFFFF);
    pri.setAllCaps(false);
    android.graphics.drawable.GradientDrawable priBg = new android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] { 0xFF7C8CF0, 0xFFA9BBEC });
    priBg.setCornerRadius(dp(14));
    priBg.setColors(new int[] { 0xFF7C8CF0, 0xFFA9BBEC });
    pri.setBackground(priBg);
    android.widget.LinearLayout.LayoutParams priLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
    priLp.bottomMargin = dp(10);
    pri.setOnClickListener(new android.view.View.OnClickListener() {
        @Override public void onClick(android.view.View v) {
            dialog.dismiss();
            if (hasApk) {
                showDownloadDialog(finalApkUrl, latestTag);
            } else {
                openInBrowser(finalHtmlUrl);
            }
        }
    });
    actions.addView(pri, priLp);



    
    dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
    dialog.setView(root);
    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0x00000000));
    dialog.getWindow().setLayout(dp(300), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    dialog.show();
}


private void openInBrowser(String url) {
    try {
        android.content.Intent i = new android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url));
        startActivity(i);
    } catch (Exception e) {
        android.widget.Toast.makeText(this,
                "无浏览器可用",
                android.widget.Toast.LENGTH_SHORT).show();
    }
}

// ============================================================
//  Download progress dialog (instantiated by showDownloadDialog)
// ============================================================

private android.app.AlertDialog downloadDialog;
private android.widget.ProgressBar downloadProgress;
private android.widget.TextView downloadPercentText;
private android.widget.TextView downloadSizeText;
private android.widget.Button downloadPositiveBtn;
private android.widget.Button downloadNegativeBtn;
private String downloadedApkPath;
private DownloadApkTask currentDownloadTask;
private cn.pickup.launcher.RingProgressView downloadRingFg;
private android.widget.TextView downloadSpeedText;

private void showDownloadDialog(String url, String latestTag) {
    downloadedApkPath = null;

    // ---- Outer container ----
    android.widget.LinearLayout root = new android.widget.LinearLayout(this);
    root.setOrientation(android.widget.LinearLayout.VERTICAL);
    root.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
    int pad = dp(24);
    root.setPadding(pad, dp(32), pad, dp(20));
    root.setBackground(buildCardBg(0xFFFFFFFF, dp(22)));

    // ---- 1. CIRCULAR PROGRESS RING (custom Canvas arc) ----
    android.widget.FrameLayout ringHolder = new android.widget.FrameLayout(this);
    android.widget.LinearLayout.LayoutParams rhLp =
            new android.widget.LinearLayout.LayoutParams(dp(140), dp(140));
    root.addView(ringHolder, rhLp);

    downloadRingFg = new cn.pickup.launcher.RingProgressView(this);
    downloadRingFg.setProgress(0);
    android.widget.FrameLayout.LayoutParams ringParams =
            new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
    ringHolder.addView(downloadRingFg, ringParams);

    // Center percentage text
    downloadPercentText = new android.widget.TextView(this);
    downloadPercentText.setText("0%");
    downloadPercentText.setTextSize(34);
    downloadPercentText.setTypeface(android.graphics.Typeface.create(
            "sans-serif-black", android.graphics.Typeface.BOLD));
    downloadPercentText.setTextColor(palette.ink1);
    downloadPercentText.setGravity(android.view.Gravity.CENTER);
    android.widget.FrameLayout.LayoutParams pctParams =
            new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
    ringHolder.addView(downloadPercentText, pctParams);

    // ---- 2. SIZE / SPEED INFO BELOW RING ----
    downloadSizeText = new android.widget.TextView(this);
    downloadSizeText.setText("\u8fde\u63a5\u4e2d...");
    downloadSizeText.setTextSize(13);
    downloadSizeText.setTextColor(palette.ink2);
    downloadSizeText.setGravity(android.view.Gravity.CENTER);
    android.widget.LinearLayout.LayoutParams stLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    stLp.topMargin = dp(20);
    root.addView(downloadSizeText, stLp);

    android.widget.TextView speedText = new android.widget.TextView(this);
    speedText.setText("\u26a1  \u51c6\u5907\u4e2d...");
    speedText.setTextSize(11);
    speedText.setTextColor(0xFF8A9099);
    speedText.setGravity(android.view.Gravity.CENTER);
    android.widget.LinearLayout.LayoutParams spLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    spLp.topMargin = dp(2);
    root.addView(speedText, spLp);
    downloadSpeedText = speedText;

    // ---- 3. ACTION BUTTONS (inside white card) ----
    android.widget.LinearLayout row = new android.widget.LinearLayout(this);
    row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
    android.widget.LinearLayout.LayoutParams rowLp =
            new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
    rowLp.topMargin = dp(24);
    root.addView(row, rowLp);

    // primary action button: 取消 (during) / 立即安装 (done) / 重试 (failed)
    downloadPositiveBtn = new android.widget.Button(this);
    downloadPositiveBtn.setText("\u53d6\u6d88");
    downloadPositiveBtn.setTextSize(14);
    downloadPositiveBtn.setTypeface(android.graphics.Typeface.create(
            "sans-serif-medium", android.graphics.Typeface.BOLD));
    downloadPositiveBtn.setTextColor(0xFFFFFFFF);
    downloadPositiveBtn.setAllCaps(false);
    android.graphics.drawable.GradientDrawable posBg =
            new android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[] { 0xFF7C8CF0, 0xFFA9BBEC });
    posBg.setCornerRadius(dp(14));
    downloadPositiveBtn.setBackground(posBg);
    android.widget.LinearLayout.LayoutParams posLp =
            new android.widget.LinearLayout.LayoutParams(0, dp(46), 1.0f);
    posLp.rightMargin = dp(5);
    downloadPositiveBtn.setOnClickListener(new android.view.View.OnClickListener() {
        @Override public void onClick(android.view.View v) {
            if (downloadedApkPath != null) {
                launchInstaller(downloadedApkPath);
                downloadDialog.dismiss();
            } else if (currentDownloadTask != null) {
                currentDownloadTask.cancel(true);
                downloadDialog.dismiss();
            }
        }
    });
    row.addView(downloadPositiveBtn, posLp);

    // secondary button: 后台 (during) / 关闭 (done|failed)
    downloadNegativeBtn = new android.widget.Button(this);
    downloadNegativeBtn.setText("\u540e\u53f0");
    downloadNegativeBtn.setTextSize(14);
    downloadNegativeBtn.setAllCaps(false);
    downloadNegativeBtn.setTextColor(palette.ink1);
    downloadNegativeBtn.setBackground(buildCardBg(0xFFF0F2F6, dp(14)));
    android.widget.LinearLayout.LayoutParams negLp =
            new android.widget.LinearLayout.LayoutParams(0, dp(46), 1.0f);
    negLp.leftMargin = dp(5);
    downloadNegativeBtn.setOnClickListener(new android.view.View.OnClickListener() {
        @Override public void onClick(android.view.View v) { downloadDialog.dismiss(); }
    });
    row.addView(downloadNegativeBtn, negLp);

    // ---- 4. DIALOG ----
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
    builder.setTitle("\u6b63\u5728\u4e0b\u8f7d v" + latestTag);
    builder.setView(root);
    builder.setCancelable(false);
    downloadDialog = builder.create();
    downloadDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0x00000000));
    // narrower than default
    downloadDialog.getWindow().setLayout(dp(300), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
    java.io.File prev = new java.io.File(getCacheDir() + "/update", "update.apk");
    if (prev.exists()) prev.delete();
    android.util.Log.i("PickUpdate", "starting download: " + url);
    downloadDialog.show();
    currentDownloadTask = new DownloadApkTask();
    currentDownloadTask.execute(url);
}

private void launchInstaller(String apkPath) {
    try {
        java.io.File f = new java.io.File(apkPath);
        if (!f.exists()) {
            android.widget.Toast.makeText(this,
                    "安装文件丢失，请重试",
                    android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        android.net.Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = ApkFileProvider.uriFor(this, f);
        } else {
            uri = android.net.Uri.fromFile(f);
        }
        android.content.Intent intent = new android.content.Intent(
                android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    } catch (Exception e) {
        android.widget.Toast.makeText(this,
                "打不开安装器：" + e.getMessage(),
                android.widget.Toast.LENGTH_LONG).show();
    }
}

private static String formatSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
    return String.format("%.1f MB", bytes / 1048576.0);
}

    /** Build a circular gradient drawable for the icon tile. */
    private android.graphics.drawable.GradientDrawable buildIconGradient(
            int startColor, int endColor, int size, int cornerRadius) {
        android.graphics.drawable.GradientDrawable d =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[] { startColor, endColor });
        d.setCornerRadius(cornerRadius);
        return d;
    }

    /** Build a simple rounded rectangle background (solid color, corners). */
    private android.graphics.drawable.GradientDrawable buildCardBg(
            int color, int cornerRadius) {
        android.graphics.drawable.GradientDrawable d =
                new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(cornerRadius);
        return d;
    }



/** Download APK to cache, then trigger system installer. */
private void downloadAndInstallApk(String url) {
    android.widget.Toast.makeText(this,
            "开始下载...",
            android.widget.Toast.LENGTH_LONG).show();
    new DownloadApkTask().execute(url);
}

private class DownloadApkTask extends android.os.AsyncTask<String, Integer, String> {
    @Override protected String doInBackground(String... urls) {
        String url = urls[0];
        java.io.File out = null;
        try {
            java.net.URL u = new java.net.URL(url);
            java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "PickTool-Android/1.0");
            int code = conn.getResponseCode();
            if (code != 200) return "HTTP_" + code;
            int total = conn.getContentLength();
            java.io.InputStream is = conn.getInputStream();
            java.io.File cacheDir = getCacheDir();
            java.io.File updateDir = new java.io.File(cacheDir, "update");
            updateDir.mkdirs();
            out = new java.io.File(updateDir, "update.apk");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(out);
            byte[] buf = new byte[16384];
            long read = 0;
            int n;
            int lastPct = -1;
            long lastPub = 0;
            while ((n = is.read(buf)) > 0) {
                if (isCancelled()) break;
                fos.write(buf, 0, n);
                read += n;
                long now = System.currentTimeMillis();
                if (total > 0) {
                    int pct = (int) (read * 100L / total);
                    if (pct != lastPct && (now - lastPub) >= 50) {
                        publishProgress(pct, total, (int) read);
                        lastPct = pct;
                        lastPub = now;
                    }
                } else if ((now - lastPub) >= 200) {
                    publishProgress(-1, -1, (int) read);
                    lastPub = now;
                }
            }
            fos.flush();
            fos.close();
            is.close();
            conn.disconnect();
            if (isCancelled()) {
                if (out != null && out.exists()) out.delete();
                return "CANCELLED";
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            if (out != null && out.exists()) out.delete();
            return "ERR_" + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    @Override protected void onProgressUpdate(Integer... values) {
        if (downloadDialog == null || !downloadDialog.isShowing()) return;
        int pct = values[0];
        long read = values.length > 2 ? values[2] : 0;
        long total = values.length > 1 ? values[1] : 0;
        if (pct < 0) {
            downloadPercentText.setText("\u4e0b\u8f7d\u4e2d");
            downloadSizeText.setText("\u5df2\u4e0b\u8f7d " + formatSize(read));
            downloadSpeedText.setText("\u26a1  \u8ba1\u7b97\u4e2d...");
        } else {
            downloadRingFg.setProgress(pct);
            downloadPercentText.setText(pct + "%");
            downloadSizeText.setText(formatSize(read) + " / " + formatSize(total));
            downloadSpeedText.setText("\u26a1  \u4e0b\u8f7d\u4e2d...");
        }
    }

    @Override protected void onPostExecute(String result) {
        currentDownloadTask = null;
        if (result == null || result.equals("CANCELLED")) {
            if (downloadDialog != null && downloadDialog.isShowing()) {
                downloadDialog.dismiss();
            }
            return;
        }
        if (result.startsWith("ERR_") || result.startsWith("HTTP_")) {
            if (downloadProgress != null) downloadProgress.setProgress(0);
            if (downloadPercentText != null) downloadPercentText.setText("\u4e0b\u8f7d\u5931\u8d25");
            if (downloadSizeText != null) {
                downloadSizeText.setText(result.replaceFirst("^ERR_|^HTTP_", ""));
                downloadSizeText.setTextColor(0xFFE53935);
            }
            if (downloadPositiveBtn != null) downloadPositiveBtn.setText("\u91cd\u8bd5");
            if (downloadNegativeBtn != null) downloadNegativeBtn.setText("\u5173\u95ed");
            return;
        }
        downloadedApkPath = result;
        if (downloadRingFg != null) downloadRingFg.setProgress(100);
        if (downloadPercentText != null) {
            downloadPercentText.setText("\u2713");
            downloadPercentText.setTextSize(48);
        }
        if (downloadSizeText != null) {
            java.io.File ff = new java.io.File(result);
            downloadSizeText.setText(formatSize(ff.length()) + " \u00b7 \u70b9\u53f3\u4e0b\u6309\u94ae\u5b89\u88c5");
        }
        if (downloadSpeedText != null) {
            downloadSpeedText.setText("\u2705  \u4e0b\u8f7d\u5b8c\u6210 \u00b7 \u53ef\u70b9\u4e0b\u65b9\u5b89\u88c5");
        }
        if (downloadPositiveBtn != null) {
            downloadPositiveBtn.setText("\u7acb\u5373\u5b89\u88c5");
            downloadPositiveBtn.setEnabled(true);
        }
        if (downloadNegativeBtn != null) {
            downloadNegativeBtn.setText("\u5173\u95ed");
        }
    }
}


    private View buildWidgetConfigCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(glassPanel(palette, dp(22)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(2));
        }
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.topMargin = dp(6);
        card.setLayoutParams(outer);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView lbl = text("桌面小组件配置", 12, palette.ink1, Typeface.BOLD);
        lbl.setLetterSpacing(0.05f);
        head.addView(lbl);

        TextView arrow = text("›", 18, palette.ink3, Typeface.NORMAL);
        arrow.setGravity(Gravity.CENTER);
        arrow.setIncludeFontPadding(false);
        head.addView(arrow, new LinearLayout.LayoutParams(
                dp(22), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams headParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headParams.bottomMargin = dp(6);
        card.addView(head, headParams);

        TextView sub = text("自由选择小组件显示的入口", 10, palette.ink3, Typeface.NORMAL);
        card.addView(sub);

        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription("桌面小组件配置");
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showWidgetConfigDialog(); }
        });
        return card;
    }

    private void showWidgetConfigDialog() {
        final Destination[] all = WidgetConfig.allDestinations();
        final boolean[] checked = new boolean[all.length];
        final java.util.List<Destination> current = WidgetConfig.enabled(this);
        for (int i = 0; i < all.length; i++) {
            checked[i] = current.contains(all[i]);
        }

        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(
                android.graphics.Color.TRANSPARENT));

        final Palette pal = Palette.forMode(ThemeManager.currentMode(this));

        // 根：垂直排列（卡片 + 按钮）
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // 卡片主体
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(glassPanel(pal, dp(28)));
        card.setPadding(dp(22), dp(22), dp(22), dp(14));
        root.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 标题 + 已选计数胶囊
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, 0, 0, dp(4));
        TextView title = text("选择小组件入口", 18, pal.ink1, Typeface.BOLD);
        head.addView(title);

        head.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));

        TextView countChip = text("0/7", 12, pal.accentGradientStart, Typeface.BOLD);
        countChip.setGravity(Gravity.CENTER);
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setCornerRadius(dp(999));
        chipBg.setColor(0xFFEEF1FF);
        chipBg.setStroke(dp(1), 0x337C8CF0);
        countChip.setBackground(chipBg);
        countChip.setPadding(dp(11), dp(5), dp(11), dp(5));
        head.addView(countChip);
        card.addView(head);

        TextView sub = text("勾选要显示在桌面小组件上的快捷入口", 12, pal.ink3, Typeface.NORMAL);
        sub.setPadding(0, dp(2), 0, dp(14));
        card.addView(sub);

        // count reference so onClick can update it
        final TextView[] countRef = new TextView[1];
        countRef[0] = countChip;

        // 7 个入口行
        final LinearLayout[] rows = new LinearLayout[all.length];
        final View[] dots = new View[all.length];
        int[][] grads = new int[][]{
                pal.cCainiaoGrad, pal.cTaobaoGrad, pal.cTaobaoGrad,
                pal.cPinduoduoGrad, pal.cPinduoduoGrad, pal.cJdGrad, pal.cXhsGrad
        };
        String[] details = new String[]{
                "菜鸟驿站取件", "淘宝 App 直达", "末端驿站待取列表",
                "多多买菜身份码", "包裹待取列表", "京东订单列表", "订单列表直达"
        };
        for (int i = 0; i < all.length; i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(12), dp(14), dp(12));
            row.setClickable(true);

            // 徽章
            View badge = makeBadge(grads[i], all[i].mark);
            row.addView(badge, new LinearLayout.LayoutParams(dp(44), dp(44)));

            // 名称 + 详情
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(16), 0, 0, 0);
            TextView name = text(all[i].title, 15, pal.ink1, Typeface.BOLD);
            info.addView(name);
            TextView detail = text(details[i], 11, pal.ink3, Typeface.NORMAL);
            info.addView(detail);
            row.addView(info, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            // 圆形勾选框（选中有渐变底 + ✓；未选空心）
            TextView dot = new TextView(this);
            dot.setGravity(Gravity.CENTER);
            dot.setText("\u2713");
            dot.setTextSize(13);
            dot.setTypeface(Typeface.DEFAULT_BOLD);
            dot.setTextColor(android.graphics.Color.WHITE);
            dots[i] = dot;
            row.addView(dot, new LinearLayout.LayoutParams(dp(26), dp(26)));

            // 点击切换
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    checked[index] = !checked[index];
                    paintConfigRow(rows[index], dots[index], checked[index], pal);
                    int cnt = 0;
                    for (boolean c : checked) if (c) cnt++;
                    countRef[0].setText(cnt + "/7");
                }
            });

            // 行背景（圆角）
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dp(16));
            rowBg.setColor(pal.glassChipFill);
            row.setBackground(rowBg);

            rows[i] = row;
            card.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (i < all.length - 1) {
                ((LinearLayout.LayoutParams) row.getLayoutParams()).bottomMargin = dp(8);
            }
        }

        // 按钮行
        LinearLayout btns = new LinearLayout(this);
        btns.setOrientation(LinearLayout.HORIZONTAL);
        btns.setPadding(dp(22), dp(14), dp(22), dp(20));
        root.addView(btns, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 取消
        TextView cancel = text("取消", 15, pal.ink2, Typeface.BOLD);
        cancel.setGravity(Gravity.CENTER);
        GradientDrawable cancelBg = new GradientDrawable();
        cancelBg.setCornerRadius(dp(16));
        cancelBg.setStroke(dp(1), 0x1A1F2430);
        cancelBg.setColor(0xFFF4F6FA);
        cancel.setBackground(cancelBg);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dialog.dismiss(); }
        });
        btns.addView(cancel, new LinearLayout.LayoutParams(
                0, dp(52), 1f));

        // 确定
        TextView ok = text("确定", 15, android.graphics.Color.WHITE, Typeface.BOLD);
        ok.setGravity(Gravity.CENTER);
        GradientDrawable okBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{pal.accentGradientStart, pal.accentGradientEnd});
        okBg.setCornerRadius(dp(18));
        ok.setBackground(okBg);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                java.util.List<Destination> picked = new java.util.ArrayList<>();
                for (int i = 0; i < all.length; i++) {
                    if (checked[i]) picked.add(all[i]);
                }
                WidgetConfig.setEnabled(MainActivity.this, picked);
                refreshWidgets();
                dialog.dismiss();
            }
        });
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(0, dp(52), 1f);
        okLp.leftMargin = dp(14);
        btns.addView(ok, okLp);

        dialog.setContentView(root);
        android.view.Window win = dialog.getWindow();
        if (win != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(win.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.86f);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);
        }
        dialog.show();

        // 初始状态
        int initCnt = 0;
        for (int i = 0; i < all.length; i++) {
            paintConfigRow(rows[i], dots[i], checked[i], pal);
            if (checked[i]) initCnt++;
        }
        countRef[0].setText(initCnt + "/7");
    }

    private void paintConfigRow(LinearLayout row, View dot, boolean on, Palette pal) {
        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setCornerRadius(dp(18));
        if (on) {
            rowBg.setColor(0xFFEEF1FF);   // 浅紫选中底
            rowBg.setStroke(dp(1), 0xFF7C8CF0);   // 品牌紫描边
        } else {
            rowBg.setColor(pal.glassChipFill);
            rowBg.setStroke(0, android.graphics.Color.TRANSPARENT);
        }
        row.setBackground(rowBg);

        GradientDrawable dotBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF7C8CF0, 0xFFA9BBEC});
        dotBg.setShape(GradientDrawable.OVAL);
        if (on) {
            dotBg.setColor(pal.calTodayFill);
            ((TextView) dot).setTextColor(android.graphics.Color.WHITE);
        } else {
            dotBg.setColor(pal.glassChipFill);
            dotBg.setStroke(dp(2), 0xFFC9CFDA);
            ((TextView) dot).setTextColor(0x00FFFFFF);   // 透明——未选中不显示 ✓
        }
        dot.setBackground(dotBg);
        dot.setVisibility(View.VISIBLE);
    }

    private void refreshWidgets() {
        try {
            android.content.Intent update = new android.content.Intent(
                    android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            update.setPackage(getPackageName());
            update.putExtra(
                    android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    android.appwidget.AppWidgetManager.getInstance(this)
                            .getAppWidgetIds(new android.content.ComponentName(this, PickupWidgetProvider.class))
            );
            sendBroadcast(update);
        } catch (Exception ignored) { }
    }

    private View buildAboutCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(glassPanel(palette, dp(22)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(2));
        }
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.topMargin = dp(6);
        card.setLayoutParams(outer);

        // 标题行：关于软件 + 角标>
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView lbl = text("关于软件", 12, palette.ink1, Typeface.BOLD);
        lbl.setLetterSpacing(0.05f);
        head.addView(lbl);

        TextView arrow = text("›", 18, palette.ink3, Typeface.NORMAL);
        arrow.setGravity(Gravity.CENTER);
        arrow.setIncludeFontPadding(false);
        head.addView(arrow, new LinearLayout.LayoutParams(
                dp(22), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams headParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        headParams.bottomMargin = dp(6);
        card.addView(head, headParams);

        TextView sub = text("作者：亿槑  \u00b7  版本 v" + currentVersionName(), 10, palette.ink3, Typeface.NORMAL);
        card.addView(sub);

        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription("关于软件");
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new android.content.Intent(MainActivity.this, AboutActivity.class));
            }
        });
        return card;
    }

    // ============================================================
    //  Footer
    // ============================================================


    private View buildFooter() {
        TextView f = text("亿槑 · 取件助手 · v" + currentVersionName(), 10, palette.ink3, Typeface.NORMAL);
        f.setGravity(Gravity.CENTER);
        f.setLetterSpacing(0.1f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(22);
        f.setLayoutParams(params);
        return f;
    }

    // ============================================================
    //  Helpers
    // ============================================================

    private View buildGlowLayer() {
        FrameLayout layer = new FrameLayout(this) {
            private final GlowBlobDrawable a = new GlowBlobDrawable(palette.glowA, 0.55f);
            private final GlowBlobDrawable b = new GlowBlobDrawable(palette.glowB, 0.55f);
            private final GlowBlobDrawable c = new GlowBlobDrawable(palette.glowC, 0.55f);
            private final GlowBlobDrawable d = new GlowBlobDrawable(palette.glowD, 0.55f);

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                if (w <= 0 || h <= 0) return;
                a.setBounds(new Rect((int)(-w*0.20f), (int)(-h*0.12f), (int)(w*0.80f), (int)(h*0.45f)));
                b.setBounds(new Rect((int)(w*0.50f), (int)( h*0.05f), (int)(w*1.20f), (int)(h*0.65f)));
                c.setBounds(new Rect((int)(-w*0.15f), (int)( h*0.50f), (int)(w*0.70f), (int)(h*1.00f)));
                d.setBounds(new Rect((int)(w*0.45f), (int)( h*0.60f), (int)(w*1.20f), (int)(h*1.20f)));
                invalidate();
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                a.draw(canvas); b.draw(canvas); c.draw(canvas); d.draw(canvas);
                super.dispatchDraw(canvas);
            }
        };
        layer.setBackgroundColor(Color.TRANSPARENT);
        // 涓嶆帴鍙楃偣鍑讳簨浠讹紝璁╀笅灞?ScrollView 鎺ユ敹
        return layer;
    }

    private View makeBadge(int[] grad, String mark) {
        FrameLayout badge = new FrameLayout(this);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                grad);
        bg.setCornerRadius(dp(13));
        bg.setStroke(dp(1), Color.argb(80, 255, 255, 255));  // 椤堕儴楂樺厜鎻忚竟
        badge.setBackground(bg);

        // 寰芥爣姹夊瓧锛堢櫧銆乥old銆佸眳涓級
        TextView markView = text(mark, 15, Color.WHITE, Typeface.BOLD);
        markView.setGravity(Gravity.CENTER);
        badge.addView(markView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return badge;
    }

    private GlassPanelDrawable glassPanel(Palette p, float cornerRadius) {
        return new GlassPanelDrawable(p.glassFill, p.glassStroke, cornerRadius);
    }

    private Drawable chipBackground(Palette p) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(p.glassChipFill);
        d.setCornerRadius(dp(10));
        d.setStroke(dp(1), p.glassStroke);
        return d;
    }

    private TextView text(String value, float sizeSp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", style));
        view.setIncludeFontPadding(false);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** 璇诲彇绯荤粺鐘舵€佹爮楂樺害锛坧x锛夛紝浼樺厛鐢?WindowInsets锛孉PI 30+锛涢檷绾х敤璧勬簮 dimens銆?*/
    private int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        }
        // 备份：估算 24dp（小米/红米状态栏典型高度）
        return dp(24);
    }

    /** 读取系统导航栏高度（px）。手势导航通常 16~24dp；带按键的设备更高。 */
    private int getNavigationBarHeight() {
        int resId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        }
        // 备份：估算 24dp（手势导航条）
        return dp(24);
    }

    private int dayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    private String monthAbbr() {
        String[] a = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
        return a[Calendar.getInstance().get(Calendar.MONTH)];
    }

    private String weekdayZh() {
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.SUNDAY:    return "星期日";
                        case Calendar.MONDAY:    return "星期一";
                        case Calendar.TUESDAY:   return "星期二";
                        case Calendar.WEDNESDAY: return "星期三";
                        case Calendar.THURSDAY:  return "星期四";
                        case Calendar.FRIDAY:    return "星期五";
                        case Calendar.SATURDAY:  return "星期六";
            default: return "";
        }
    }
}

