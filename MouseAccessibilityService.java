package com.tvmouse.cursor;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class MouseAccessibilityService extends AccessibilityService {

    private static MouseAccessibilityService instance;
    private WindowManager windowManager;
    private CursorOverlayView cursorView;
    private boolean cursorEnabled = true;

    private float cursorX = 640f;
    private float cursorY = 360f;
    private int screenWidth = 1920;
    private int screenHeight = 1080;

    private float speed = 14f;
    private int holdTicks = 0;
    private int activeDirection = 0;
    private long lastMenuPressTime = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable moveLoop = new Runnable() {
        @Override
        public void run() {
            if (activeDirection != 0 && cursorEnabled) {
                holdTicks++;
                float accel = Math.min(3.5f, 1.0f + holdTicks * 0.12f);
                float step = speed * accel;

                if (activeDirection == KeyEvent.KEYCODE_DPAD_UP) cursorY -= step;
                if (activeDirection == KeyEvent.KEYCODE_DPAD_DOWN) cursorY += step;
                if (activeDirection == KeyEvent.KEYCODE_DPAD_LEFT) cursorX -= step;
                if (activeDirection == KeyEvent.KEYCODE_DPAD_RIGHT) cursorX += step;

                cursorX = Math.max(0, Math.min(screenWidth - 4, cursorX));
                cursorY = Math.max(0, Math.min(screenHeight - 4, cursorY));

                if (cursorY <= 8 && activeDirection == KeyEvent.KEYCODE_DPAD_UP) {
                    performScroll(screenWidth / 2f, screenHeight * 0.35f, screenWidth / 2f, screenHeight * 0.75f);
                } else if (cursorY >= screenHeight - 12 && activeDirection == KeyEvent.KEYCODE_DPAD_DOWN) {
                    performScroll(screenWidth / 2f, screenHeight * 0.75f, screenWidth / 2f, screenHeight * 0.35f);
                }

                if (cursorView != null) {
                    cursorView.updatePosition(cursorX, cursorY);
                }
                handler.postDelayed(this, 16);
            }
        }
    };

    public static MouseAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        cursorX = screenWidth / 2f;
        cursorY = screenHeight / 2f;

        attachOverlay();
        Toast.makeText(this, "TV Remote Mouse đã sẵn sàng!", Toast.LENGTH_SHORT).show();
    }

    private void attachOverlay() {
        if (cursorView != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return;

        cursorView = new CursorOverlayView(this);
        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(cursorView, params);
        cursorView.updatePosition(cursorX, cursorY);
    }

    public void toggleCursorMode() {
        cursorEnabled = !cursorEnabled;
        if (cursorView != null) {
            cursorView.setVisibility(cursorEnabled ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        Toast.makeText(this, cursorEnabled ? "Đã BẬT con trỏ chuột" : "Đã TẮT con trỏ (chế độ D-pad thường)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO)) {
            long now = System.currentTimeMillis();
            if (now - lastMenuPressTime < 500) {
                toggleCursorMode();
                return true;
            }
            lastMenuPressTime = now;
        }

        if (!cursorEnabled) {
            return super.onKeyEvent(event);
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (activeDirection != keyCode) {
                    activeDirection = keyCode;
                    holdTicks = 0;
                    handler.removeCallbacks(moveLoop);
                    handler.post(moveLoop);
                }
            } else if (action == KeyEvent.ACTION_UP) {
                if (activeDirection == keyCode) {
                    activeDirection = 0;
                    holdTicks = 0;
                    handler.removeCallbacks(moveLoop);
                }
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (cursorView != null) cursorView.setClicking(true);
            } else if (action == KeyEvent.ACTION_UP) {
                if (cursorView != null) cursorView.setClicking(false);
                boolean isLongPress = (event.getEventTime() - event.getDownTime()) > 450;
                performClickAt(cursorX, cursorY, isLongPress ? 550 : 60);
            }
            return true;
        }

        return super.onKeyEvent(event);
    }

    private void performClickAt(float x, float y, long durationMs) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(clickPath, 0, durationMs);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    private void performScroll(float startX, float startY, float endX, float endY) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(swipePath, 0, 220);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();
        dispatchGesture(gesture, null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        if (cursorView != null && windowManager != null) {
            windowManager.removeView(cursorView);
            cursorView = null;
        }
    }
}
