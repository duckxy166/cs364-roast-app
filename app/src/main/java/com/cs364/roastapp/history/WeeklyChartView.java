package com.cs364.roastapp.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.cs364.roastapp.R;

import java.util.ArrayList;
import java.util.List;

public class WeeklyChartView extends View {

    public static final class DayBar {
        public final String label;
        public final int minutes;

        public DayBar(String label, int minutes) {
            this.label = label;
            this.minutes = Math.max(0, minutes);
        }
    }

    private final List<DayBar> bars = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private String unitSuffix = "m";

    public WeeklyChartView(Context context) {
        super(context);
        init(context);
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WeeklyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = getResources().getDisplayMetrics().density;
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        barPaint.setColor(ContextCompat.getColor(context, R.color.pid_brand_coral));
        barPaint.setStyle(Paint.Style.FILL);

        barTrackPaint.setColor(ContextCompat.getColor(context, R.color.pid_outline_variant));
        barTrackPaint.setAlpha(64);
        barTrackPaint.setStyle(Paint.Style.FILL);

        baselinePaint.setColor(ContextCompat.getColor(context, R.color.pid_outline_variant));
        baselinePaint.setStrokeWidth(1f * density);

        labelPaint.setColor(ContextCompat.getColor(context, R.color.pid_on_surface_variant));
        labelPaint.setTextSize(11f * scaledDensity);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(ContextCompat.getColor(context, R.color.pid_on_surface));
        valuePaint.setTextSize(10f * scaledDensity);
        valuePaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<DayBar> dayBars, String minuteUnitSuffix) {
        bars.clear();
        if (dayBars != null) {
            bars.addAll(dayBars);
        }
        if (minuteUnitSuffix != null && !minuteUnitSuffix.isEmpty()) {
            this.unitSuffix = minuteUnitSuffix;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) {
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float height = getHeight() - getPaddingTop() - getPaddingBottom();
        if (width <= 0 || height <= 0) {
            return;
        }

        float topTextSpace = 18f * density;
        float bottomTextSpace = 22f * density;
        float chartTop = getPaddingTop() + topTextSpace;
        float chartBottom = getPaddingTop() + height - bottomTextSpace;
        float maxBarHeight = Math.max(1f, chartBottom - chartTop);

        int maxMinutes = 1;
        for (DayBar bar : bars) {
            if (bar.minutes > maxMinutes) {
                maxMinutes = bar.minutes;
            }
        }

        int count = bars.size();
        float slotWidth = width / count;
        float barWidth = Math.min(28f * density, slotWidth * 0.52f);
        float radius = 6f * density;

        canvas.drawLine(
                getPaddingLeft(),
                chartBottom,
                getPaddingLeft() + width,
                chartBottom,
                baselinePaint
        );

        for (int i = 0; i < count; i++) {
            DayBar bar = bars.get(i);
            float centerX = getPaddingLeft() + slotWidth * (i + 0.5f);
            float left = centerX - barWidth / 2f;
            float right = centerX + barWidth / 2f;

            barRect.set(left, chartTop, right, chartBottom);
            canvas.drawRoundRect(barRect, radius, radius, barTrackPaint);

            if (bar.minutes > 0) {
                float ratio = (float) bar.minutes / (float) maxMinutes;
                float barHeight = Math.max(4f * density, maxBarHeight * ratio);
                barRect.set(left, chartBottom - barHeight, right, chartBottom);
                canvas.drawRoundRect(barRect, radius, radius, barPaint);

                String valText = bar.minutes + unitSuffix;
                canvas.drawText(valText, centerX, chartBottom - barHeight - (5f * density), valuePaint);
            }

            canvas.drawText(bar.label, centerX, chartBottom + (15f * density), labelPaint);
        }
    }
}
