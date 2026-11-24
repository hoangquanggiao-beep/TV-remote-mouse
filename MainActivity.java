package com.tvmouse.cursor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0B1120"));
        root.setPadding(64, 48, 64, 48);

        TextView title = new TextView(this);
        title.setText("TV REMOTE MOUSE");
        title.setTextColor(Color.parseColor("#38BDF8"));
        title.setTextSize(28f);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Biến phím điều hướng Remote TV thành con trỏ chuột.\n" +
                "• Bấm 4 phím mũi tên để di chuyển chuột\n" +
                "• Bấm OK / Enter để Click trái\n" +
                "• Nhấn giữ OK để cuộn trang / mở menu\n" +
                "• Bấm 2 lần nút Menu hoặc Info để bật/tắt chuột");
        desc.setTextColor(Color.parseColor("#CBD5E1"));
        desc.setTextSize(16f);
        desc.setPadding(0, 24, 0, 32);
        root.addView(desc);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#FACC15"));
        statusText.setTextSize(16f);
        statusText.setPadding(0, 0, 0, 24);
        root.addView(statusText);

        Button btnOverlay = createTvButton("1. Cấp quyền Hiển thị trên ứng dụng khác");
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Đã cấp quyền hiển thị lớp phủ!", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(btnOverlay);

        Button btnAccessibility = createTvButton("2. Bật dịch vụ Trợ năng (TV Remote Mouse)");
        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        root.addView(btnAccessibility);

        Button btnToggle = createTvButton("3. Bật / Tắt Con Trỏ Chuột Ngay");
        btnToggle.setOnClickListener(v -> {
            MouseAccessibilityService service = MouseAccessibilityService.getInstance();
            if (service != null) {
                service.toggleCursorMode();
            } else {
                Toast.makeText(this, "Hãy bật dịch vụ Trợ năng ở Bước 2 trước!", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(btnToggle);

        setContentView(root);
        btnOverlay.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean overlayOk = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
        boolean serviceOk = MouseAccessibilityService.getInstance() != null;
        statusText.setText("Trạng thái: Lớp phủ [" + (overlayOk ? "OK" : "Chưa bật") +
                "]  |  Dịch vụ Remote [" + (serviceOk ? "Đang chạy" : "Chưa bật") + "]");
    }

    private Button createTvButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setFocusable(true);
        btn.setFocusableInTouchMode(true);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#1E293B"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 12);
        btn.setLayoutParams(params);
        btn.setPadding(32, 24, 32, 24);
        btn.setOnFocusChangeListener((v, hasFocus) -> {
            v.setBackgroundColor(hasFocus ? Color.parseColor("#0284C7") : Color.parseColor("#1E293B"));
            v.setScaleX(hasFocus ? 1.03f : 1.0f);
            v.setScaleY(hasFocus ? 1.03f : 1.0f);
        });
        return btn;
    }
}
