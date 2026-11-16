package com.example.bakedashtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalesChartView extends View {

    public static class BarData {
        public final String label;
        public final double value;
        public final String valueFormatted;

        public BarData(String label, double value, String valueFormatted) {
            this.label = label;
            this.value = value;
            this.valueFormatted = valueFormatted;
        }
    }

    private final List<BarData> dataList = new ArrayList<>();
    private int selectedIndex = -1;

    private Paint gridPaint;
    private Paint barPaint;
    private Paint textPaint;
    private Paint tooltipBgPaint;
    private Paint tooltipTextPaint;
    private Paint highlightPaint;

    private final RectF barRect = new RectF();
    private final RectF tooltipRect = new RectF();
    private final List<RectF> renderedBars = new ArrayList<>();

    public SalesChartView(Context context) {
        super(context);
        init();
    }

    public SalesChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SalesChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E2E8F0"));
        gridPaint.setStrokeWidth(dpToPx(1));

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(Color.parseColor("#042F24"));
        highlightPaint.setStrokeWidth(dpToPx(2));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextSize(spToPx(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipBgPaint.setColor(Color.parseColor("#042F24"));
        tooltipBgPaint.setStyle(Paint.Style.FILL);

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(spToPx(11));
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);
    }

    public void setData(List<BarData> data) {
        dataList.clear();
        if (data != null) {
            dataList.addAll(data);
        }
        selectedIndex = -1;
        // Default select highest bar if exists
        double max = 0;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).value > max) {
                max = dataList.get(i).value;
                selectedIndex = i;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderedBars.clear();

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float padLeft = dpToPx(12);
        float padRight = dpToPx(12);
        float padTop = dpToPx(36); // room for top tooltip
        float padBottom = dpToPx(28); // room for X labels

        float chartH = h - padTop - padBottom;
        float chartW = w - padLeft - padRight;

        // Draw 3 subtle guide lines
        for (int i = 0; i <= 2; i++) {
            float y = padTop + (chartH * (i / 2f));
            canvas.drawLine(padLeft, y, w - padRight, y, gridPaint);
        }

        if (dataList.isEmpty()) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No revenue recorded for this period", w / 2f, h / 2f, textPaint);
            return;
        }

        // Find max value
        double maxVal = 0;
        for (BarData b : dataList) {
            if (b.value > maxVal) maxVal = b.value;
        }
        if (maxVal <= 0) maxVal = 100;

        int count = dataList.size();
        float slotW = chartW / count;
        float maxBarW = dpToPx(32);
        float barWidth = Math.min(maxBarW, slotW * 0.65f);
        float cornerRadius = dpToPx(6);

        // Gradient shader for bars
        LinearGradient gradient = new LinearGradient(
                0, padTop, 0, padTop + chartH,
                Color.parseColor("#10B981"), Color.parseColor("#065F46"),
                Shader.TileMode.CLAMP
        );
        barPaint.setShader(gradient);

        for (int i = 0; i < count; i++) {
            BarData item = dataList.get(i);
            float centerX = padLeft + (i * slotW) + (slotW / 2f);
            float left = centerX - (barWidth / 2f);
            float right = centerX + (barWidth / 2f);

            float barH = (float) ((item.value / maxVal) * chartH);
            if (item.value > 0 && barH < dpToPx(4)) barH = dpToPx(4); // min visible height
            float top = padTop + chartH - barH;
            float bottom = padTop + chartH;

            barRect.set(left, top, right, bottom);
            renderedBars.add(new RectF(barRect));

            // Draw Bar
            if (item.value > 0) {
                canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, barPaint);
                // Flatten bottom corners
                canvas.drawRect(left, bottom - cornerRadius, right, bottom, barPaint);

                if (i == selectedIndex) {
                    canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, highlightPaint);
                }
            } else {
                // Empty dot for 0 value
                Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                emptyPaint.setColor(Color.parseColor("#CBD5E1"));
                canvas.drawCircle(centerX, bottom - dpToPx(3), dpToPx(3), emptyPaint);
            }

            // Draw X Axis Label
            float labelTextSize = (item.label != null && (item.label.length() > 5 || count > 6)) ? spToPx(9.5f) : spToPx(11f);
            textPaint.setTextSize(labelTextSize);
            textPaint.setColor(i == selectedIndex ? Color.parseColor("#042F24") : Color.parseColor("#64748B"));
            textPaint.setFakeBoldText(i == selectedIndex);
            canvas.drawText(item.label, centerX, h - dpToPx(8), textPaint);
        }

        // Draw Tooltip over selected bar
        if (selectedIndex >= 0 && selectedIndex < dataList.size()) {
            BarData sel = dataList.get(selectedIndex);
            float centerX = padLeft + (selectedIndex * slotW) + (slotW / 2f);
            String tip = sel.valueFormatted != null ? sel.valueFormatted : ("Rs. " + (int) sel.value);

            float textW = tooltipTextPaint.measureText(tip);
            float tipW = textW + dpToPx(16);
            float tipH = dpToPx(24);

            float tipLeft = centerX - (tipW / 2f);
            float tipRight = centerX + (tipW / 2f);
            if (tipLeft < padLeft) {
                tipLeft = padLeft;
                tipRight = tipLeft + tipW;
            }
            if (tipRight > w - padRight) {
                tipRight = w - padRight;
                tipLeft = tipRight - tipW;
            }

            float tipBottom = padTop - dpToPx(6);
            float tipTop = tipBottom - tipH;

            tooltipRect.set(tipLeft, tipTop, tipRight, tipBottom);
            canvas.drawRoundRect(tooltipRect, dpToPx(8), dpToPx(8), tooltipBgPaint);

            float fontMetrics = tooltipTextPaint.descent() + tooltipTextPaint.ascent();
            float textY = tipTop + (tipH / 2f) - (fontMetrics / 2f);
            canvas.drawText(tip, (tipLeft + tipRight) / 2f, textY, tooltipTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < renderedBars.size(); i++) {
                RectF r = renderedBars.get(i);
                // Expand touch target
                if (x >= r.left - dpToPx(12) && x <= r.right + dpToPx(12)) {
                    if (selectedIndex != i) {
                        selectedIndex = i;
                        invalidate();
                    }
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
