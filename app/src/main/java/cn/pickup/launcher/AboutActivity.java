package cn.pickup.launcher;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * \u5173\u4e8e\u8f6f\u4ef6\u9875\uff1a\u56fe\u6807\u3001\u540d\u79f0\u3001\u529f\u80fd\u3001\u5f00\u53d1\u8005\u3001\u8054\u7cfb\u65b9\u5f0f\u3001\u5176\u4ed6\u4fe1\u606f\u3002
 */
public final class AboutActivity extends Activity {

    private Palette palette;

    /** Get current app versionName from PackageInfo. */
    private String currentVersionName() {
        try {
            android.content.pm.PackageInfo pi =
                    getPackageManager().getPackageInfo(getPackageName(), 0);
            return pi.versionName == null ? "0.0.0" : pi.versionName;
        } catch (Exception e) {
            return "0.0.0";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        palette = Palette.forMode(ThemeManager.currentMode(this));
        applySystemBars();
        setContentView(buildContent());
    }

    private void applySystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (palette.statusBarMode == 0) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
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

    private View buildContent() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(palette.pageBackground);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16),
                getStatusBarHeight() + dp(10),
                dp(16),
                getNavigationBarHeight() + dp(20));
        scroll.addView(column, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        // ---------- \u9876\u90e8\uff1a\u8fd4\u56de + \u6807\u9898 ----------
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView backBtn = text("\u2039", 26, palette.ink1, Typeface.NORMAL);
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setPadding(dp(8), dp(6), dp(14), dp(6));
        backBtn.setClickable(true);
        backBtn.setFocusable(true);
        backBtn.setContentDescription("\u8fd4\u56de");
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        topBar.addView(backBtn);

        TextView title = text("\u5173\u4e8e\u8f6f\u4ef6", 17, palette.ink1, Typeface.BOLD);
        topBar.addView(title);

        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        topParams.bottomMargin = dp(12);
        column.addView(topBar, topParams);

        // ---------- Hero\uff1a\u73bb\u7483\u5149\u73af + \u56fe\u6807 + \u540d\u79f0 + \u7248\u672c\u80f6\u56ca ----------
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(dp(6), dp(10), dp(6), dp(16));

        // \u73bb\u7483\u5149\u73af\uff08\u5916\u5c42 88dp\uff09
        LinearLayout ring = new LinearLayout(this);
        ring.setGravity(Gravity.CENTER);
        ring.setBackground(glassPanel(palette, dp(26)));
        LinearLayout.LayoutParams ringLp = new LinearLayout.LayoutParams(dp(88), dp(88));
        hero.addView(ring, ringLp);

        // \u5185\u5c42\u56fe\u6807 68dp
        LinearLayout iconBox = new LinearLayout(this);
        iconBox.setGravity(Gravity.CENTER);
        try {
            Drawable ic = getResources().getDrawable(R.drawable.ic_launcher, getTheme());
            iconBox.setBackground(ic);
        } catch (Throwable ignored) { }
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(68), dp(68));
        ring.addView(iconBox, iconLp);

        TextView name = text("\u53d6\u4ef6\u52a9\u624b", 19, palette.ink1, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        name.setLetterSpacing(0.04f);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nameLp.topMargin = dp(12);
        hero.addView(name, nameLp);

        // \u7248\u672c\u80f6\u56ca
        TextView ver = text("\u7248\u672c v" + currentVersionName(), 11, 0xFF6C7A99, Typeface.NORMAL);
        ver.setGravity(Gravity.CENTER);
        GradientDrawable verBg = new GradientDrawable();
        verBg.setShape(GradientDrawable.RECTANGLE);
        verBg.setCornerRadius(dp(999));
        verBg.setColor(0xFFE8EDFA);
        ver.setBackground(verBg);
        ver.setPadding(dp(14), dp(4), dp(14), dp(4));
        LinearLayout.LayoutParams verLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        verLp.topMargin = dp(8);
        hero.addView(ver, verLp);

        column.addView(hero);

        // ---------- \u529f\u80fd ----------
        column.addView(section(0xFF8A9BF2, "\u529f\u80fd", new String[]{
                "\u5feb\u901f\u6253\u5f00\u83dc\u9e1f / \u6dd8\u5b9d / \u62fc\u591a\u591a\u53d6\u4ef6\u7801",
                "\u67e5\u770b\u6dd8\u5b9d / \u62fc\u591a\u591a / \u4eac\u4e1c / \u5c0f\u7ea2\u4e66\u5f85\u53d6\u5feb\u9012",
                "\u4e00\u952e\u5c06\u5165\u53e3\u56fa\u5b9a\u5230\u684c\u9762",
                "\u652f\u6301\u5e94\u7528\u957f\u6309\u5feb\u6377\u64cd\u4f5c",
                "\u652f\u6301\u684c\u9762\u5c0f\u7ec4\u4ef6",
                "\u6bcf\u65e5\u4e00\u8a00 + \u4e3b\u9898\u5207\u6362"
        }));

        // ---------- \u5f00\u53d1\u8005 ----------
        LinearLayout devCard = card();
        View devHead = cardHead(0xFFF5B64C, "\u5f00\u53d1\u8005");
        devCard.addView(devHead);

        LinearLayout devRow = new LinearLayout(this);
        devRow.setOrientation(LinearLayout.HORIZONTAL);
        devRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView avatar = text("\u4ebf", 13, 0xFFFFFFFF, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF7C8CF0, 0xFFA9BBEC});
        avatarBg.setCornerRadius(dp(12));
        avatar.setBackground(avatarBg);
        LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(32), dp(32));
        devRow.addView(avatar, avLp);

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.VERTICAL);
        TextView devName = text("\u4ebf\u69d1", 13, palette.ink1, Typeface.BOLD);
        meta.addView(devName);
        TextView devSlogan = text("\u4e13\u6ce8\u7cbe\u54c1\u5c0f\u5de5\u5177\uff0c\u8ba9\u53d6\u4ef6\u66f4\u7b80\u5355", 10.5f, palette.ink3, Typeface.NORMAL);
        LinearLayout.LayoutParams sloganLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        sloganLp.topMargin = dp(2);
        meta.addView(devSlogan, sloganLp);
        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        metaLp.leftMargin = dp(10);
        devRow.addView(meta, metaLp);

        devCard.addView(devRow);
        column.addView(devCard);

        // ---------- \u8054\u7cfb\u65b9\u5f0f ----------
        column.addView(contactCard());

        // ---------- \u5176\u4ed6\u4fe1\u606f ----------
        column.addView(section(0xFFB8C0CC, "\u5176\u4ed6\u4fe1\u606f", new String[]{
                "\u4e0d\u8bfb\u53d6\u77ed\u4fe1 / \u901a\u77e5 / \u76f8\u518c / \u8d26\u53f7\u6570\u636e",
                "\u4ec5\u4f7f\u7528\u7f51\u7edc\u6743\u9650\u52a0\u8f7d\u6bcf\u65e5\u4e00\u8a00",
                "\u58f0\u660e\uff1a\u672c\u5e94\u7528\u4e0e\u5404\u5e73\u53f0\u65e0\u4efb\u4f55\u8054\u7cfb"
        }));

        // ---------- \u5e95\u90e8 foot ----------
        TextView foot = text("\u4ebf\u69d1 \u00b7 \u53d6\u4ef6\u52a9\u624b \u00b7 v" + currentVersionName(), 10, palette.ink3, Typeface.NORMAL);
        foot.setGravity(Gravity.CENTER);
        foot.setLetterSpacing(0.1f);
        LinearLayout.LayoutParams footLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        footLp.topMargin = dp(16);
        column.addView(foot, footLp);

        return root;
    }

    // ---------- \u5361\u7247\u5e95 ----------
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.setBackground(glassPanel(palette, dp(20)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.setElevation(dp(2));
        }
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outer.topMargin = dp(10);
        c.setLayoutParams(outer);
        return c;
    }

    // ---------- \u6807\u9898\u884c\uff1a\u5c0f\u65b9\u5757 + \u6807\u9898 ----------
    private View cardHead(int dotColor, String head) {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        View sq = new View(this);
        GradientDrawable sqBg = new GradientDrawable();
        sqBg.setShape(GradientDrawable.RECTANGLE);
        sqBg.setCornerRadius(dp(3));
        sqBg.setColor(dotColor);
        sq.setBackground(sqBg);
        LinearLayout.LayoutParams sqLp = new LinearLayout.LayoutParams(dp(6), dp(6));
        h.addView(sq, sqLp);
        TextView t = text(head, 13, palette.ink1, Typeface.BOLD);
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tLp.leftMargin = dp(6);
        h.addView(t, tLp);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hLp.bottomMargin = dp(8);
        h.setLayoutParams(hLp);
        return h;
    }

    // ---------- \u5206\u7ec4\u5361\u7247\uff08\u6807\u9898 + \u884c\uff09 ----------
    private View section(int dotColor, String head, String[] rows) {
        LinearLayout card = card();
        card.addView(cardHead(dotColor, head));
        for (String row : rows) {
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            TextView d = text("\u00b7", 12, 0xFF8A9BF2, Typeface.BOLD);
            d.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            dLp.rightMargin = dp(8);
            r.addView(d, dLp);
            TextView t = text(row, 11.5f, palette.ink2, Typeface.NORMAL);
            t.setLineSpacing(dp(3), 1f);
            r.addView(t, new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rLp.topMargin = dp(3);
            card.addView(r, rLp);
        }
        return card;
    }

    // ---------- \u8054\u7cfb\u65b9\u5f0f\uff08\u70b9\u51fb\u590d\u5236 + \u590d\u5236\u80f6\u56ca\uff09 ----------
    private View contactCard() {
        LinearLayout card = card();
        card.addView(cardHead(0xFF3FD68B, "\u8054\u7cfb\u65b9\u5f0f"));
        card.addView(contactRow("\u90ae\u7bb1", "yimei@ymei.top"));
        card.addView(contactRow("QQ", "3629424534"));
        return card;
    }

    private View contactRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), dp(6), dp(2), dp(6));

        TextView lbl = text(label, 11, palette.ink3, Typeface.BOLD);
        lbl.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                dp(42), LinearLayout.LayoutParams.WRAP_CONTENT);
        row.addView(lbl, lp1);

        TextView val = text(value, 12.5f, palette.accentGradientEnd, Typeface.NORMAL);
        val.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(val, lp2);

        TextView copy = text("\u590d\u5236", 10, 0xFF7A86A8, Typeface.NORMAL);
        copy.setGravity(Gravity.CENTER);
        GradientDrawable copyBg = new GradientDrawable();
        copyBg.setShape(GradientDrawable.RECTANGLE);
        copyBg.setCornerRadius(dp(999));
        copyBg.setColor(0xFFF0F3FA);
        copy.setBackground(copyBg);
        copy.setPadding(dp(10), dp(3), dp(10), dp(3));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.leftMargin = dp(8);
        row.addView(copy, cp);

        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription(label + "\uff1a" + value);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText(label, value));
                    android.widget.Toast.makeText(AboutActivity.this,
                            label + "\u5df2\u590d\u5236", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        return row;
    }

    // ---------- helpers ----------
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

    private Drawable glassPanel(Palette p, float cornerRadius) {
        return new GlassPanelDrawable(p.glassFill, p.glassStroke, cornerRadius);
    }

    private int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        }
        return dp(24);
    }

    private int getNavigationBarHeight() {
        int resId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        }
        return dp(24);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
