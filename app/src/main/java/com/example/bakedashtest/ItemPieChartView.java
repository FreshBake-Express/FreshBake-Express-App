package com.example.bakedashtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemPieChartView extends View {

    public static class PieEntry {
        public final String label;
        public final double value;
        public final int quantity;
        public final double percentage;
        public final int color;
        public final String formattedValue;

        public PieEntry(String label, double value, int quantity, double percentage, int color, String formattedValue) {
            this.label = label;
            this.value = value;
            this.quantity = quantity;
            this.percentage = percentage;
            this.color = color;
            this.formattedValue = formattedValue;
        }
    }

    public interface OnSliceSelectedListener {
        void onSliceSelected(@Nullable PieEntry entry, int index);
    }

    public static final int[] PALETTE = new int[] {
            Color.parseColor("#10B981"), // Emerald
            Color.parseColor("#3B82F6"), // Blue
            Color.parseColor("#F59E0B"), // Amber / Gold
            Color.parseColor("#8B5CF6"), // Purple
            Color.parseColor("#EC4899"), // Pink
            Color.parseColor("#06B6D4"), // Cyan / Teal
            Color.parseColor("#F97316"), // Orange
            Color.parseColor("#64748B")  // Slate
    };

    private final List<PieEntry> entries = new ArrayList<>();
    private int selectedIndex = -1;
    private int totalQuantity = 0;
    private double totalRevenue = 0;

    private Paint slicePaint;
    private Paint centerHolePaint;
    private Paint centerTextPrimaryPaint;
    private Paint centerTextSecondaryPaint;
    private Paint centerTextTertiaryPaint;
    private Paint highlightBorderPaint;
    private Paint emptyRingPaint;

    private final RectF pieBounds = new RectF();
    private final RectF selectedBounds = new RectF();
    private OnSliceSelectedListener sliceListener;

    public ItemPieChartView(Context context) {
        super(context);
        init();
    }

    public ItemPieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ItemPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);

        centerHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerHolePaint.setColor(Color.WHITE);
        centerHolePaint.setStyle(Paint.Style.FILL);

        highlightBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightBorderPaint.setStyle(Paint.Style.STROKE);
        highlightBorderPaint.setColor(Color.parseColor("#042F24"));
        highlightBorderPaint.setStrokeWidth(dpToPx(2.5f));

        centerTextPrimaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextPrimaryPaint.setColor(Color.parseColor("#0F172A"));
        centerTextPrimaryPaint.setTextSize(spToPx(15));
        centerTextPrimaryPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPrimaryPaint.setFakeBoldText(true);

        centerTextSecondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextSecondaryPaint.setColor(Color.parseColor("#10B981"));
        centerTextSecondaryPaint.setTextSize(spToPx(12));
        centerTextSecondaryPaint.setTextAlign(Paint.Align.CENTER);
        centerTextSecondaryPaint.setFakeBoldText(true);

        centerTextTertiaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextTertiaryPaint.setColor(Color.parseColor("#64748B"));
        centerTextTertiaryPaint.setTextSize(spToPx(10.5f));
        centerTextTertiaryPaint.setTextAlign(Paint.Align.CENTER);

        emptyRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyRingPaint.setStyle(Paint.Style.STROKE);
        emptyRingPaint.setStrokeWidth(dpToPx(18));
        emptyRingPaint.setColor(Color.parseColor("#E2E8F0"));
    }

    public void setOnSliceSelectedListener(OnSliceSelectedListener listener) {
        this.sliceListener = listener;
    }

    public void setData(List<PieEntry> data, int totalQty, double totalRev) {
        entries.clear();
        if (data != null) {
            entries.addAll(data);
        }
        this.totalQuantity = totalQty;
        this.totalRevenue = totalRev;
        this.selectedIndex = -1;
        invalidate();
    }

    public void setSelectedIndex(int index) {
        if (index >= -1 && index < entries.size()) {
            this.selectedIndex = index;
            invalidate();
            if (sliceListener != null) {
                sliceListener.onSliceSelected(selectedIndex >= 0 ? entries.get(selectedIndex) : null, selectedIndex);
            }
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;

        float minDim = Math.min(w, h);
        float outerRadius = (minDim / 2f) - dpToPx(14);
        float innerRadius = outerRadius * 0.58f;

        if (outerRadius <= 0) return;

        // Empty state
        if (entries.isEmpty() || totalQuantity <= 0) {
            canvas.drawCircle(cx, cy, (outerRadius + innerRadius) / 2f, emptyRingPaint);
            centerTextPrimaryPaint.setTextSize(spToPx(13.5f));
            centerTextPrimaryPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("No items sold", cx, cy - dpToPx(2), centerTextPrimaryPaint);
            centerTextTertiaryPaint.setTextSize(spToPx(11f));
            canvas.drawText("in selected period", cx, cy + dpToPx(16), centerTextTertiaryPaint);
            return;
        }

        pieBounds.set(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius);

        float currentAngle = -90f; // Start at 12 o'clock
        float gap = entries.size() > 1 ? 2.0f : 0f;

        // Draw Slices
        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            float sweepAngle = (float) (entry.percentage / 100f * 360f);
            if (sweepAngle <= 0) continue;

            slicePaint.setColor(entry.color);

            float drawSweep = Math.max(1f, sweepAngle - gap);
            float drawStart = currentAngle + (gap / 2f);

            if (i == selectedIndex) {
                // Expand selected slice slightly
                float expand = dpToPx(6);
                selectedBounds.set(
                        cx - outerRadius - expand,
                        cy - outerRadius - expand,
                        cx + outerRadius + expand,
                        cy + outerRadius + expand
                );
                canvas.drawArc(selectedBounds, drawStart, drawSweep, true, slicePaint);
                canvas.drawArc(selectedBounds, drawStart, drawSweep, true, highlightBorderPaint);
            } else {
                canvas.drawArc(pieBounds, drawStart, drawSweep, true, slicePaint);
            }

            currentAngle += sweepAngle;
        }

        // Draw Center Cutout (Donut Hole)
        canvas.drawCircle(cx, cy, innerRadius, centerHolePaint);

        // Center Content Text
        if (selectedIndex >= 0 && selectedIndex < entries.size()) {
            PieEntry sel = entries.get(selectedIndex);

            // Line 1: Item Label (Truncate if too long)
            String label = sel.label != null ? sel.label : "Item";
            if (label.length() > 14) {
                label = label.substring(0, 12) + "…";
            }
            centerTextPrimaryPaint.setTextSize(spToPx(12.5f));
            centerTextPrimaryPaint.setColor(Color.parseColor("#0F172A"));
            canvas.drawText(label, cx, cy - dpToPx(12), centerTextPrimaryPaint);

            // Line 2: Quantity & Share Percentage
            centerTextSecondaryPaint.setTextSize(spToPx(11.5f));
            centerTextSecondaryPaint.setColor(sel.color);
            String share = sel.quantity + " units (" + String.format(Locale.getDefault(), "%.1f%%", sel.percentage) + ")";
            canvas.drawText(share, cx, cy + dpToPx(4), centerTextSecondaryPaint);

            // Line 3: Revenue
            centerTextTertiaryPaint.setTextSize(spToPx(10f));
            centerTextTertiaryPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText(sel.formattedValue, cx, cy + dpToPx(18), centerTextTertiaryPaint);
        } else {
            // Line 1: Total Units Sold
            centerTextPrimaryPaint.setTextSize(spToPx(15f));
            centerTextPrimaryPaint.setColor(Color.parseColor("#0F172A"));
            canvas.drawText(totalQuantity + " Units", cx, cy - dpToPx(10), centerTextPrimaryPaint);

            // Line 2: Total Revenue
            centerTextSecondaryPaint.setTextSize(spToPx(12f));
            centerTextSecondaryPaint.setColor(Color.parseColor("#10B981"));
            String revStr = String.format(Locale.getDefault(), "Rs. %,d", (long) totalRevenue);
            canvas.drawText(revStr, cx, cy + dpToPx(6), centerTextSecondaryPaint);

            // Line 3: Product Count
            centerTextTertiaryPaint.setTextSize(spToPx(10f));
            centerTextTertiaryPaint.setColor(Color.parseColor("#64748B"));
            String countStr = entries.size() + (entries.size() == 1 ? " product" : " products");
            canvas.drawText(countStr, cx, cy + dpToPx(20), centerTextTertiaryPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            int w = getWidth();
            int h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;

            float minDim = Math.min(w, h);
            float outerRadius = (minDim / 2f) - dpToPx(14);
            float innerRadius = outerRadius * 0.58f;

            float dx = x - cx;
            float dy = y - cy;
            float dist = (float) Math.hypot(dx, dy);

            // Tap in donut body or center hole
            if (dist <= innerRadius) {
                // Tapping center toggles back to all
                if (selectedIndex != -1) {
                    selectedIndex = -1;
                    invalidate();
                    if (sliceListener != null) sliceListener.onSliceSelected(null, -1);
                    return true;
                }
            } else if (dist <= outerRadius + dpToPx(12)) {
                // Calculate touch angle in degrees relative to -90 deg (12 o'clock)
                double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
                // Normalise so -90 deg becomes 0 deg
                double normalizedAngle = (angleDeg + 90.0) % 360.0;
                if (normalizedAngle < 0) normalizedAngle += 360.0;

                double accumulated = 0.0;
                int clickedIndex = -1;
                for (int i = 0; i < entries.size(); i++) {
                    double sweep = (entries.get(i).percentage / 100.0) * 360.0;
                    if (normalizedAngle >= accumulated && normalizedAngle <= accumulated + sweep) {
                        clickedIndex = i;
                        break;
                    }
                    accumulated += sweep;
                }

                if (clickedIndex != -1) {
                    selectedIndex = (selectedIndex == clickedIndex) ? -1 : clickedIndex;
                    invalidate();
                    if (sliceListener != null) {
                        sliceListener.onSliceSelected(selectedIndex >= 0 ? entries.get(selectedIndex) : null, selectedIndex);
                    }
                    performClick();
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
