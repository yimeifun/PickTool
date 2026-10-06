package com.pickup.tool;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主界面，Material 3 布局。
 *
 * 层次结构：
 * 1. large top app bar —— 页面标题，纯文本，不加容器；
 * 2. 「取件码」——三张 elevated card，图标容器带平台 tonal 色，主要操作；
 * 3. 「查看待取」——低层级 tonal surface 卡片网格，次要操作；
 * 4. 「添加到桌面」——底部 filled tonal 胶囊按钮，标题后附注说明需要权限；
 * 5. 免责说明留在内容末尾，二改署名与项目地址固定在屏幕底部并居中。
 *
 * 层次不靠阴影堆叠，而是靠 M3 的 surface 色调深浅与字阶（400/500）区分，
 * 详见 {@link M3} 与 {@link SurfaceDrawable}。
 */
public final class MainActivity extends Activity {
    static final String ACTION_OPEN = "com.pickup.tool.OPEN";
    static final String EXTRA_DESTINATION = "destination";

    /** M3 圆角令牌：large 28dp 用于卡片，full 用于胶囊按钮。 */
    private static final float RADIUS_CARD_DP = 28f;
    private static final float RADIUS_ICON_DP = 16f;
    private static final float RADIUS_ROW_DP = 20f;

    private static final String PROJECT_GITEE_URL = "https://gitee.com/yimei-fun/picktool";
    private static final String PROJECT_GITHUB_URL = "https://github.com/yimeifun/PickTool";

    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Destination destination = destinationFromIntent();
        if (destination != null) {
            DeepLinkLauncher.open(this, destination);
            finish();
            return;
        }

        density = getResources().getDisplayMetrics().density;
        applyEdgeToEdgeChrome();
        setContentView(buildContent());
    }

    private Destination destinationFromIntent() {
        String action = getIntent().getAction();
        if (ACTION_OPEN.equals(action)) {
            return Destination.fromKey(getIntent().getStringExtra(EXTRA_DESTINATION));
        }
        if ("com.pickup.tool.OPEN_CAINIAO".equals(action)) {
            return Destination.CAINIAO;
        }
        if ("com.pickup.tool.OPEN_TAOBAO".equals(action)) {
            return Destination.TAOBAO;
        }
        if ("com.pickup.tool.OPEN_PINDUODUO".equals(action)) {
            return Destination.PINDUODUO;
        }
        if ("com.pickup.tool.OPEN_TAOBAO_PENDING".equals(action)) {
            return Destination.TAOBAO_PENDING;
        }
        if ("com.pickup.tool.OPEN_PINDUODUO_PENDING".equals(action)) {
            return Destination.PINDUODUO_PENDING;
        }
        if ("com.pickup.tool.OPEN_JD".equals(action)) {
            return Destination.JD;
        }
        if ("com.pickup.tool.OPEN_XHS".equals(action)) {
            return Destination.XHS;
        }
        if ("com.pickup.tool.OPEN_MEITUAN".equals(action)) {
            return Destination.MEITUAN;
        }
        if ("com.pickup.tool.OPEN_DOUYIN".equals(action)) {
            return Destination.DOUYIN;
        }
        return null;
    }

    /**
     * 状态栏与导航栏透明，背景色由 M3 surface 承担。
     * 浅色背景固定使用深色系统栏图标。
     */
    private void applyEdgeToEdgeChrome() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private View buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(M3.SURFACE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setClipToPadding(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        content.addView(buildAppBar());

        content.addView(sectionLabel("取件码"));
        content.addView(buildPrimaryRow());

        content.addView(sectionLabel("查看待取"));
        content.addView(buildPendingGrid());

        content.addView(sectionLabel(
                "添加到桌面",
                "需要打开桌面快捷方式权限"
        ));
        content.addView(buildPinRow());

        content.addView(buildDisclaimer());

        // 署名与项目地址固定在屏幕底部，不随内容滚动，也不会被中部留白顶上来。
        View creditBar = buildCreditBar();
        root.addView(creditBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        applyInsets(root, content, creditBar);
        return root;
    }

    /**
     * large top app bar：标题 400 字重，作者信息已移至页脚。
     */
    private View buildAppBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(4), dp(8), dp(4), dp(14));

        TextView title = text("取件助手", 34, M3.ON_SURFACE, Typeface.NORMAL);
        bar.addView(title);

        return bar;
    }

    /**
     * 内容流末尾的免责说明。
     */
    private View buildDisclaimer() {
        TextView note = text(
                "第三方应用更新后，内部页面地址可能发生变化。",
                13,
                M3.ON_SURFACE_VARIANT,
                Typeface.NORMAL
        );
        note.setLineSpacing(0f, 1.4f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(26);
        params.bottomMargin = dp(8);
        note.setLayoutParams(params);
        return note;
    }

    /**
     * 屏幕底部的署名与项目地址：整行水平居中，仅 Gitee / GitHub 可点击，
     * 点击后用系统浏览器打开对应项目地址。
     */
    private View buildCreditBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.addView(footerText("Forked by 亿槑 · "));
        bar.addView(footerLink("Gitee", PROJECT_GITEE_URL));
        bar.addView(footerText(" · "));
        bar.addView(footerLink("GitHub", PROJECT_GITHUB_URL));
        return bar;
    }

    private TextView footerText(String value) {
        return text(value, 13, M3.ON_SURFACE_VARIANT, Typeface.NORMAL);
    }

    private TextView footerLink(String label, final String url) {
        TextView link = text(label, 13, M3.ON_SURFACE, Typeface.BOLD);
        link.setClickable(true);
        link.setFocusable(true);
        link.setContentDescription("打开" + label + "项目地址");
        link.setOnClickListener(view -> openProjectUrl(url));
        return link;
    }

    private void openProjectUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, "没有可打开该地址的应用", Toast.LENGTH_SHORT).show();
        }
    }

    private View sectionLabel(String value) {
        return sectionLabel(value, null);
    }

    /**
     * 小节标题，可选一段附注跟在标题后面（例如权限说明）。
     */
    private View sectionLabel(String value, String note) {
        SpannableString labelText = new SpannableString(value);
        int titleLength = value.length();
        if (note != null && !note.isEmpty()) {
            String suffix = "（" + note + "）";
            labelText = new SpannableString(value + suffix);
            int start = titleLength;
            int end = labelText.length();
            labelText.setSpan(
                    new RelativeSizeSpan(0.72f),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            labelText.setSpan(
                    new ForegroundColorSpan(M3.OUTLINE),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            labelText.setSpan(
                    new StyleSpan(Typeface.NORMAL),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        TextView label = text("", 16, M3.ON_SURFACE_VARIANT, Typeface.BOLD);
        label.setText(labelText);
        // 附注较长时允许折到第二行，避免在窄屏被硬裁。
        label.setLineSpacing(0f, 1.25f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = dp(4);
        params.topMargin = dp(18);
        params.bottomMargin = dp(10);
        label.setLayoutParams(params);
        return label;
    }

    /**
     * 三张主入口卡：等宽、等高的水平排布，卡片内容自适应宽度。
     */
    private View buildPrimaryRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(3f);

        row.addView(primaryCard(
                Destination.CAINIAO,
                "菜鸟",
                "身份码",
                M3.CAINIAO_CONTAINER,
                M3.CAINIAO_ON,
                false
        ));
        row.addView(primaryCard(
                Destination.TAOBAO,
                "淘宝",
                "取件码",
                M3.TAOBAO_CONTAINER,
                M3.TAOBAO_ON,
                true
        ));
        row.addView(primaryCard(
                Destination.PINDUODUO,
                "拼多多",
                "身份码",
                M3.PINDUODUO_CONTAINER,
                M3.PINDUODUO_ON,
                false
        ));
        return row;
    }

    /**
     * primaryCard 是核心入口，点击直接跳转对应平台。
     */
    private View primaryCard(
            Destination destination,
            String title,
            String subtitle,
            int containerColor,
            int onContainerColor,
            boolean withStartMargin
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(12), dp(10), dp(12));
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription("打开" + destination.title);
        card.setOnClickListener(view -> DeepLinkLauncher.open(this, destination));
        card.setBackground(M3.surface(
                density,
                RADIUS_CARD_DP,
                M3.SURFACE_CONTAINER_LOWEST,
                2.5f,
                0.10f
        ));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        if (withStartMargin) {
            params.leftMargin = dp(10);
            params.rightMargin = dp(10);
        }
        card.setLayoutParams(params);

        // 多用一行：垂直方向填满，保证三张卡等高。
        card.setMinimumHeight(dp(92));

        TextView badge = text(destination.mark, 19, onContainerColor, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(M3.tonal(density, RADIUS_ICON_DP, containerColor));
        card.addView(badge, new LinearLayout.LayoutParams(dp(34), dp(34)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        labelsParams.leftMargin = dp(6);
        labels.setLayoutParams(labelsParams);

        // 三张卡等宽，窄屏上留给文字的空间很紧；标题单行且随可用宽度自动收小，
        // 避免“拼多多”这类较长的名字被卡片右边缘裁掉。
        TextView titleView = text(title, 13, M3.ON_SURFACE, Typeface.BOLD);
        titleView.setSingleLine(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            titleView.setAutoSizeTextTypeUniformWithConfiguration(
                    10,
                    14,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
            );
        }
        labels.addView(titleView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView subtitleView = text(subtitle, 11, M3.ON_SURFACE_VARIANT, Typeface.NORMAL);
        subtitleView.setSingleLine(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            subtitleView.setAutoSizeTextTypeUniformWithConfiguration(
                    9,
                    11,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
            );
        }
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(3);
        subtitleView.setLayoutParams(subtitleParams);
        labels.addView(subtitleView);

        card.addView(labels);

        return card;
    }

    /**
     * 待取入口：2x2 网格，卡片层级低于主入口（surfaceContainerLow 而非纯白）。
     */
    private View buildPendingGrid() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);

        column.addView(pendingRow(
                pendingItem(Destination.TAOBAO_PENDING, "淘宝", M3.TAOBAO_CONTAINER, M3.TAOBAO_ON, false),
                pendingItem(Destination.PINDUODUO_PENDING, "拼多多", M3.PINDUODUO_CONTAINER, M3.PINDUODUO_ON, true)
        ));

        LinearLayout secondRow = pendingRow(
                pendingItem(Destination.JD, "京东", M3.JD_CONTAINER, M3.JD_ON, false),
                pendingItem(Destination.XHS, "小红书", M3.XHS_CONTAINER, M3.XHS_ON, true)
        );
        LinearLayout.LayoutParams secondParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        secondParams.topMargin = dp(10);
        secondRow.setLayoutParams(secondParams);
        column.addView(secondRow);

        LinearLayout thirdRow = pendingRow(
                pendingItem(Destination.MEITUAN, "美团", M3.MEITUAN_CONTAINER, M3.MEITUAN_ON, false),
                pendingItem(Destination.DOUYIN, "抖音", M3.DOUYIN_CONTAINER, M3.DOUYIN_ON, true)
        );
        LinearLayout.LayoutParams thirdParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        thirdParams.topMargin = dp(10);
        thirdRow.setLayoutParams(thirdParams);
        column.addView(thirdRow);

        return column;
    }

    private LinearLayout pendingRow(View left, View right) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(2f);
        row.addView(left);
        row.addView(right);
        return row;
    }

    private View pendingItem(
            Destination destination,
            String title,
            int containerColor,
            int onContainerColor,
            boolean withStartMargin
    ) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(14), dp(14), dp(12), dp(14));
        item.setMinimumHeight(dp(78));
        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription("打开" + destination.title);
        item.setOnClickListener(view -> DeepLinkLauncher.open(this, destination));
        item.setBackground(M3.surface(
                density,
                RADIUS_ROW_DP,
                M3.SURFACE_CONTAINER_LOW,
                0f,
                0f
        ));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        if (withStartMargin) {
            params.leftMargin = dp(10);
        }
        item.setLayoutParams(params);

        TextView badge = text(destination.mark, 18, onContainerColor, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(M3.tonal(density, 14f, containerColor));
        item.addView(badge, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(12), 0, 0, 0);

        TextView titleView = text(title, 16, M3.ON_SURFACE, Typeface.BOLD);
        titleView.setSingleLine(true);
        labels.addView(titleView);

        TextView subtitleView = text("待取列表", 12, M3.ON_SURFACE_VARIANT, Typeface.NORMAL);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(3);
        subtitleView.setLayoutParams(subtitleParams);
        labels.addView(subtitleView);

        item.addView(labels, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        return item;
    }

    /**
     * 底部固定快捷方式：filled tonal 胶囊，三个一行。
     */
    private View buildPinRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setWeightSum(3f);

        addPinButton(row, Destination.CAINIAO, "菜鸟", "身份码", M3.CAINIAO_ACCENT);
        addPinButton(row, Destination.TAOBAO, "淘宝", "取件码", M3.TAOBAO_ACCENT);
        addPinButton(row, Destination.PINDUODUO, "拼多多", "身份码", M3.PINDUODUO_ACCENT);
        return row;
    }

    private void addPinButton(
            LinearLayout parent,
            Destination destination,
            String appName,
            String codeType,
            int accentColor
    ) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(10), dp(12), dp(10), dp(12));
        button.setMinimumHeight(dp(46));
        button.setClickable(true);
        button.setFocusable(true);
        button.setContentDescription("将" + destination.title + "添加到桌面");
        button.setOnClickListener(view -> ShortcutPinning.request(this, destination));
        button.setBackground(M3.surface(
                density,
                23f,
                M3.SURFACE_CONTAINER_HIGH,
                0f,
                0f
        ));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        if (parent.getChildCount() > 0) {
            params.leftMargin = dp(8);
        }
        button.setLayoutParams(params);

        View dot = new View(this);
        dot.setBackground(M3.tonal(density, 6f, accentColor));
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(12), dp(12));
        dotParams.rightMargin = dp(8);
        button.addView(dot, dotParams);

        // 按钮文案 = 平台名 + 括号标注的码类型，平台名用品牌色以便一眼区分。
        SpannableString label = new SpannableString(appName + "（" + codeType + "）");
        label.setSpan(
                new ForegroundColorSpan(accentColor),
                0,
                appName.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        label.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                appName.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        int openParen = appName.length();
        label.setSpan(
                new RelativeSizeSpan(0.86f),
                openParen,
                label.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        label.setSpan(
                new ForegroundColorSpan(M3.ON_SURFACE_VARIANT),
                openParen,
                label.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        TextView labelView = text("", 13, M3.ON_SURFACE, Typeface.BOLD);
        labelView.setText(label);
        labelView.setSingleLine(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            labelView.setAutoSizeTextTypeUniformWithConfiguration(
                    9,
                    12,
                    1,
                    TypedValue.COMPLEX_UNIT_SP
            );
        }
        // 自动缩放之外再兜底：文字排不下时优先省略平台名之后的括注，而不是整段被裁掉。
        labelView.setEllipsize(null);
        labelView.setHorizontallyScrolling(false);
        button.addView(labelView);

        parent.addView(button);
    }

    /**
     * 内容避开状态栏、导航栏与刘海；底部署名栏单独承担导航栏内边距。
     */
    private void applyInsets(View root, final LinearLayout content, final View creditBar) {
        final int baseTop = dp(12);
        final int contentBottom = dp(8);
        final int horizontal = dp(16);
        final int creditTop = dp(10);
        final int creditBottom = dp(12);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            content.setPadding(
                    horizontal + insets.getSystemWindowInsetLeft(),
                    baseTop + insets.getSystemWindowInsetTop(),
                    horizontal + insets.getSystemWindowInsetRight(),
                    contentBottom
            );
            creditBar.setPadding(
                    horizontal + insets.getSystemWindowInsetLeft(),
                    creditTop,
                    horizontal + insets.getSystemWindowInsetRight(),
                    creditBottom + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        root.requestApplyInsets();
    }

    private TextView text(String value, float sizeSp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        // M3 中文用系统默认无衬线，字重通过 bold 表达 label / title 层级。
        view.setTypeface(Typeface.create("sans-serif", style));
        view.setIncludeFontPadding(false);
        return view;
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
