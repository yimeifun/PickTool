package cn.pickup.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 每日一言：从 hitokoto.cn 异步拉取一句话，缓存在 SharedPreferences。
 *
 * 调用方式：DailyQuote.fetch(this, new Callback() { onQuote(quote) });
 *
 * - 失败回退到本地默认一句（"今天也要好好取快递。"），保证 UI 永远有内容
 * - 今日已拉过则直接返回缓存
 * - 拉取在后台线程，UI 在主线程回调
 */
final class DailyQuote {
    private static final String TAG = "PickupDailyQuote";
    private static final String API = "https://v1.hitokoto.cn/?encode=text&charset=utf-8&max_length=18";
    private static final String PREFS = "pickup_daily_quote";
    private static final String KEY_QUOTE = "quote";
    private static final String KEY_DATE  = "date";
    private static final String FALLBACK = "今天也要好好取快递。";

    interface Callback {
        void onQuote(String quote);
    }

    private DailyQuote() {}

    static void fetch(Context ctx, Callback cb) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String today = String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR));
        String savedDay  = prefs.getString(KEY_DATE, "");
        String savedQuote = prefs.getString(KEY_QUOTE, "");
        if (today.equals(savedDay) && !savedQuote.isEmpty()) {
            cb.onQuote(savedQuote);
            return;
        }
        // 异步拉取
        new Thread(() -> {
            String q = httpGet();
            if (q == null || q.isEmpty()) q = FALLBACK;
            final String quote = q;
            prefs.edit().putString(KEY_QUOTE, quote).putString(KEY_DATE, today).apply();
            final Callback cbFinal = cb;
            new Handler(Looper.getMainLooper()).post(() -> cbFinal.onQuote(quote));
        }, "DailyQuote-fetch").start();
    }

    /** GET 一次 v1.hitokoto.cn，返回解码后的句子；失败 null。 */
    private static String httpGet() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(API).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "PickupLauncher/2.1 (Android)");
            int code = conn.getResponseCode();
            if (code != 200) {
                Log.w(TAG, "http " + code);
                return null;
            }
            try (InputStream is = conn.getInputStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                String body = sb.toString().trim();
                // hitokoto.cn 的 encode=text 直接返回纯文本句子（无 JSON 包装）
                if (body.isEmpty()) return null;
                return body;
            }
        } catch (Exception e) {
            Log.w(TAG, "fetch failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}