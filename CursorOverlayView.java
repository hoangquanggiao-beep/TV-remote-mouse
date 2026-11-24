package com.tvmouse.cursor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class CursorOverlayView extends View {
    private float cursorX = 500f;
    private float cursorY = 300f;
    private boolean isClicking = false;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrowPath = new Path();

    public CursorOverlayView(Context context) {
        super(context);
        fillPaint.setColor(Color.parseColor("#38BDF8"));
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(3.5f);
        strokePaint.setStyle(Paint.Style.STROKE);

        ringPaint.setColor(Color.parseColor("#8038BDF8"));
        ringPaint.setStrokeWidth(4f);
        ringPaint.setStyle(Paint.Style.STROKE);
    }

    public void updatePosition(float x, float y) {
        this.cursorX = x;
        this.cursorY = y;
        invalidate();
    }

    public void setClicking(boolean clicking) {
        this.isClicking = clicking;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isClicking) {
            canvas.drawCircle(cursorX, cursorY, 26f, ringPaint);
        }
        arrowPath.reset();
        arrowPath.moveTo(cursorX, cursorY);
        arrowPath.lineTo(cursorX, cursorY + 34f);
        arrowPath.lineTo(cursorX + 9f, cursorY + 26f);
        arrowPath.lineTo(cursorX + 16f, cursorY + 40f);
        arrowPath.lineTo(cursorX + 21f, cursorY + 37f);
        arrowPath.lineTo(cursorX + 14f, cursorY + 23f);
        arrowPath.lineTo(cursorX + 25f, cursorY + 23f);
        arrowPath.close();

        canvas.drawPath(arrowPath, fillPaint);
        canvas.drawPath(arrowPath, strokePaint);
    }
}
