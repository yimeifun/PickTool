package cn.pickup.launcher;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

public final class PickupWidgetProvider extends AppWidgetProvider {
    private static final int MAX_SLOTS = 8;
    /** 布局里的默认图标尺寸(dp)；Provider 会按 widget 实际尺寸覆盖 */
    private static final int DEFAULT_ICON_DP = 40;

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds
    ) {
        List<Destination> enabled = WidgetConfig.enabled(context);
        for (int appWidgetId : appWidgetIds) {
            int iconDp = computeIconDp(appWidgetManager, appWidgetId, enabled.size());
            boolean compact = isCompact(appWidgetManager, appWidgetId);
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.pickup_widget
            );
            bindSlots(context, views, enabled, compact, iconDp);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId,
            android.os.Bundle newOptions
    ) {
        List<Destination> enabled = WidgetConfig.enabled(context);
        int iconDp = computeIconDp(appWidgetManager, appWidgetId, enabled.size());
        boolean compact = isCompact(appWidgetManager, appWidgetId);
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                R.layout.pickup_widget
        );
        bindSlots(context, views, enabled, compact, iconDp);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * 计算每个入口图标边长(dp)。
     * 优先用 API 31+ 的 OPTION_APPWIDGET_SIZES（当前实际尺寸），
     * 否则退回 MIN_WIDTH/MIN_HEIGHT 属性值。
     * - 单行（≤4）时图标高度受限；两行（>4）时每行高度减半
     * - 宽度方向按 (宽 - padding) / 列数
     * - 取两者较小值，保证正方形；再夹在 [14, 36] 之间（整体调小）
     */
    private int computeIconDp(AppWidgetManager manager, int appWidgetId, int count) {
        android.os.Bundle opts = manager.getAppWidgetOptions(appWidgetId);
        int minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        int minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);

        // API 31+: 使用当前实际尺寸（用户拖拽后 onAppWidgetOptionsChanged 会更新）
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            try {
                java.util.ArrayList<android.util.SizeF> sizes =
                        opts.getParcelableArrayList(
                                AppWidgetManager.OPTION_APPWIDGET_SIZES,
                                android.util.SizeF.class);
                if (sizes != null && !sizes.isEmpty()) {
                    float bestW = Float.MAX_VALUE;
                    float bestH = Float.MAX_VALUE;
                    for (android.util.SizeF s : sizes) {
                        if (s.getWidth() < bestW) bestW = s.getWidth();
                        if (s.getHeight() < bestH) bestH = s.getHeight();
                    }
                    minW = (int) bestW;
                    minH = (int) bestH;
                }
            } catch (Exception ignored) { }
        }

        if (minW <= 0) minW = 180;
        if (minH <= 0) minH = 64;

        int rows = count > 4 ? 2 : 1;
        // 水平：每列可用宽 ≈ (宽 - 左右 padding 20 - 行列 margin) / 列数
        float perCol = (minW - 26f) / 4f;
        // 垂直：每行可用高 ≈ (高 - 上下 padding 20) / 行数，再扣除 label 高度
        float perRow = (minH - 20f) / rows - (count > 4 ? 12f : 14f);
        float side = Math.min(perCol - 4f, perRow);
        if (side < 14) side = 14;
        if (side > 36) side = 36;
        return (int) side;
    }

    /** 尺寸过小（拖到最小组件）时进入紧凑模式：隐藏入口名称 */
    private boolean isCompact(AppWidgetManager manager, int appWidgetId) {
        android.os.Bundle opts = manager.getAppWidgetOptions(appWidgetId);
        int minW = opts.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        int minH = opts.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            try {
                java.util.ArrayList<android.util.SizeF> sizes =
                        opts.getParcelableArrayList(
                                AppWidgetManager.OPTION_APPWIDGET_SIZES,
                                android.util.SizeF.class);
                if (sizes != null && !sizes.isEmpty()) {
                    float bestW = Float.MAX_VALUE;
                    float bestH = Float.MAX_VALUE;
                    for (android.util.SizeF s : sizes) {
                        if (s.getWidth() < bestW) bestW = s.getWidth();
                        if (s.getHeight() < bestH) bestH = s.getHeight();
                    }
                    return bestW < 140 || bestH < 54;
                }
            } catch (Exception ignored) { }
        }
        return minW < 140 || minH < 54;
    }

    /** 按配置填充 8 个 slot；图标尺寸按 widget 实际尺寸自适应 */
    private void bindSlots(Context context, RemoteViews views, List<Destination> enabled,
                           boolean compact, int iconDp) {
        int[] slotIds = new int[]{
                R.id.widget_slot0, R.id.widget_slot1, R.id.widget_slot2, R.id.widget_slot3,
                R.id.widget_slot4, R.id.widget_slot5, R.id.widget_slot6, R.id.widget_slot7
        };
        int[] frameIds = new int[]{
                R.id.widget_iconframe0, R.id.widget_iconframe1, R.id.widget_iconframe2,
                R.id.widget_iconframe3, R.id.widget_iconframe4, R.id.widget_iconframe5,
                R.id.widget_iconframe6, R.id.widget_iconframe7
        };
        int[] imgIds = new int[]{
                R.id.widget_img0, R.id.widget_img1, R.id.widget_img2, R.id.widget_img3,
                R.id.widget_img4, R.id.widget_img5, R.id.widget_img6, R.id.widget_img7
        };
        int[] charIds = new int[]{
                R.id.widget_char0, R.id.widget_char1, R.id.widget_char2, R.id.widget_char3,
                R.id.widget_char4, R.id.widget_char5, R.id.widget_char6, R.id.widget_char7
        };
        int[] labelIds = new int[]{
                R.id.widget_label0, R.id.widget_label1, R.id.widget_label2, R.id.widget_label3,
                R.id.widget_label4, R.id.widget_label5, R.id.widget_label6, R.id.widget_label7
        };
        int requestBase = 201;

        // 图标尺寸（px）：dp -> px
        int iconPx = Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, iconDp,
                        context.getResources().getDisplayMetrics()));

        for (int i = 0; i < MAX_SLOTS; i++) {
            if (i < enabled.size()) {
                Destination d = enabled.get(i);
                views.setViewVisibility(slotIds[i], View.VISIBLE);
                views.setViewVisibility(imgIds[i], View.VISIBLE);
                views.setViewVisibility(charIds[i], View.VISIBLE);
                views.setImageViewResource(imgIds[i], WidgetConfig.gradientRes(d));
                views.setTextViewText(charIds[i], d.mark);
                // 自适应图标尺寸（RemoteViews API 35: setViewLayoutWidth/Height）
                views.setViewLayoutWidth(frameIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(frameIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutWidth(imgIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(imgIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutWidth(charIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(charIds[i], iconDp, TypedValue.COMPLEX_UNIT_DIP);
                // 字号随图标缩放
                float charSp = iconDp * 0.42f;
                if (charSp < 9) charSp = 9;
                if (charSp > 18) charSp = 18;
                views.setTextViewTextSize(charIds[i], TypedValue.COMPLEX_UNIT_SP, charSp);
                if (compact) {
                    // 紧凑模式：隐藏名称，只留图标
                    views.setViewVisibility(labelIds[i], View.GONE);
                } else {
                    views.setViewVisibility(labelIds[i], View.VISIBLE);
                    views.setTextViewText(labelIds[i], WidgetConfig.shortLabel(d));
                }
                views.setContentDescription(slotIds[i], "打开" + d.title);
                views.setOnClickPendingIntent(
                        slotIds[i],
                        pendingIntent(context, d, requestBase + i)
                );
            } else if (i >= 4) {
                // 第二行的空 slot：INVISIBLE 占位保持列宽对称
                views.setViewVisibility(slotIds[i], View.INVISIBLE);
                views.setViewVisibility(imgIds[i], View.INVISIBLE);
                views.setViewVisibility(charIds[i], View.INVISIBLE);
                views.setViewVisibility(labelIds[i], View.INVISIBLE);
            } else {
                views.setViewVisibility(slotIds[i], View.GONE);
            }
        }

        // 第二行：只有 5+ 个入口时显示
        boolean showRow1 = enabled.size() > 4;
        views.setViewVisibility(R.id.widget_row1,
                showRow1 ? View.VISIBLE : View.GONE);
    }

    private PendingIntent pendingIntent(
            Context context,
            Destination destination,
            int requestCode
    ) {
        Intent intent = new Intent(context, MainActivity.class)
                .setAction(MainActivity.ACTION_OPEN)
                .putExtra(MainActivity.EXTRA_DESTINATION, destination.key);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}