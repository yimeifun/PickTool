package cn.pickup.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 桌面小组件入口配置：用户自由勾选要在小组件上显示的入口。
 * 存 SharedPreferences，PickupWidgetProvider 按配置填充 slot。
 */
final class WidgetConfig {
    private static final String PREFS = "pickup_widget_prefs";
    private static final String KEY_ENABLED = "enabled_keys";

    /** 默认显示这 5 个（与旧版小组件一致） */
    private static final String DEFAULT_KEYS =
            "cainiao,taobao,pinduoduo,jd";

    private WidgetConfig() {}

    /** 所有可选入口（7 个） */
    static Destination[] allDestinations() {
        return new Destination[]{
                Destination.CAINIAO,
                Destination.TAOBAO,
                Destination.TAOBAO_PENDING,
                Destination.PINDUODUO,
                Destination.PINDUODUO_PENDING,
                Destination.JD,
                Destination.XHS
        };
    }

    static List<Destination> enabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_ENABLED, DEFAULT_KEYS);
        List<Destination> result = new ArrayList<>();
        for (String key : raw.split(",")) {
            Destination d = Destination.fromKey(key.trim());
            if (d != null && !result.contains(d)) {
                result.add(d);
            }
        }
        return result;
    }

    static void setEnabled(Context context, List<Destination> destinations) {
        StringBuilder sb = new StringBuilder();
        for (Destination d : destinations) {
            if (sb.length() > 0) sb.append(',');
            sb.append(d.key);
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ENABLED, sb.toString()).apply();
    }

    /** 单个入口的渐变背景资源 */
    static int gradientRes(Destination d) {
        switch (d) {
            case CAINIAO:          return R.drawable.widget_grad_cainiao;
            case TAOBAO:
            case TAOBAO_PENDING:  return R.drawable.widget_grad_taobao;
            case PINDUODUO:
            case PINDUODUO_PENDING: return R.drawable.widget_grad_pinduoduo;
            case JD:               return R.drawable.widget_grad_jd;
            case XHS:              return R.drawable.widget_grad_xhs;
            default:               return R.drawable.widget_grad_pinduoduo;
        }
    }

    /** 入口简称（图标下方的字） */
    static String shortLabel(Destination d) {
        switch (d) {
            case CAINIAO:           return "菜鸟";
            case TAOBAO:            return "淘宝取件";
            case TAOBAO_PENDING:    return "淘宝待取";
            case PINDUODUO:         return "拼多多取件";
            case PINDUODUO_PENDING: return "拼多多待取";
            case JD:                return "京东";
            case XHS:               return "小红书";
            default:                return d.mark;
        }
    }
}