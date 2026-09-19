package cn.pickup.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 主题模式：用户手动选择 light / dark，默认 light。
 * 跟随系统未实现——按用户要求"亮色 + 暗色可自由切换"。
 */
final class ThemeManager {
    static final int MODE_LIGHT = 0;
    static final int MODE_DARK = 1;
    private static final String PREFS = "pickup_launcher_prefs";
    private static final String KEY_THEME = "theme_mode";

    private ThemeManager() {
    }

    static int currentMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, MODE_LIGHT);
    }

    static boolean isDark(Context context) {
        return currentMode(context) == MODE_DARK;
    }

    static void toggle(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int next = currentMode(activity) == MODE_LIGHT ? MODE_DARK : MODE_LIGHT;
        prefs.edit().putInt(KEY_THEME, next).apply();
        activity.recreate();
    }
}
