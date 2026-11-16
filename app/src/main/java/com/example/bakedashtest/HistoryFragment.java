package com.example.bakedashtest;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvStatRevenue, tvStatBills, tvStatDue, tvStatDueStores, tvStatProfit, tvStatProfitSub;
    private TextView filterToday, filterWeek, filterCustom, filterAllTime;
    private TextView tabCustomerWise, tabItemWise;
    private TextView tvListHeader, tvListCount;
    private EditText etHistorySearch;
    private LinearLayout llHistoryList, llHistoryEmpty;

    // Top Revenue Bar Chart Components
    private TextView tvChartHeader, tvChartRevenueTotal, tvChartPeriodSub, tvChartBestDay, tvChartHint;
    private SalesChartView salesChartView;

    // Item-wise Pie Chart Components
    private LinearLayout cardItemPieChart, llPieLegend;
    private TextView tvPieChartTitle, tvPieChartSub, tvPieTopItem;
    private ItemPieChartView itemPieChartView;

    private boolean isItemWise = false;

    // 0: Today (Daily), 1: This Week (Weekly), 2: Custom Date, 3: All Time
    private int dateFilterMode = 0;
    private String currentStartDate = "";
    private String currentEndDate = "";
    private String customDateLabel = "Select Date 📅";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvStatRevenue = view.findViewById(R.id.tvStatRevenue);
        tvStatBills = view.findViewById(R.id.tvStatBills);
        tvStatDue = view.findViewById(R.id.tvStatDue);
        tvStatDueStores = view.findViewById(R.id.tvStatDueStores);
        tvStatProfit = view.findViewById(R.id.tvStatProfit);
        tvStatProfitSub = view.findViewById(R.id.tvStatProfitSub);

        filterToday = view.findViewById(R.id.filterToday);
        filterWeek = view.findViewById(R.id.filterWeek);
        filterCustom = view.findViewById(R.id.filterCustom);
        filterAllTime = view.findViewById(R.id.filterAllTime);

        tabCustomerWise = view.findViewById(R.id.tabCustomerWise);
        tabItemWise = view.findViewById(R.id.tabItemWise);

        tvListHeader = view.findViewById(R.id.tvListHeader);
        tvListCount = view.findViewById(R.id.tvListCount);
        etHistorySearch = view.findViewById(R.id.etHistorySearch);
        llHistoryList = view.findViewById(R.id.llHistoryList);
        llHistoryEmpty = view.findViewById(R.id.llHistoryEmpty);

        // Chart Card Views
        tvChartHeader = view.findViewById(R.id.tvChartHeader);
        tvChartRevenueTotal = view.findViewById(R.id.tvChartRevenueTotal);
        tvChartPeriodSub = view.findViewById(R.id.tvChartPeriodSub);
        tvChartBestDay = view.findViewById(R.id.tvChartBestDay);
        tvChartHint = view.findViewById(R.id.tvChartHint);
        salesChartView = view.findViewById(R.id.salesChartView);

        // Item-wise Pie Chart Views
        cardItemPieChart = view.findViewById(R.id.cardItemPieChart);
        tvPieChartTitle = view.findViewById(R.id.tvPieChartTitle);
        tvPieChartSub = view.findViewById(R.id.tvPieChartSub);
        tvPieTopItem = view.findViewById(R.id.tvPieTopItem);
        itemPieChartView = view.findViewById(R.id.itemPieChartView);
        llPieLegend = view.findViewById(R.id.llPieLegend);

        setupDateRange(0);
        setupDateFilterListeners();
        setupTabClickListeners();
        setupSearchListener();
        refreshHistory();

        return view;
    }

    private void setupDateRange(int mode) {
        dateFilterMode = mode;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        switch (mode) {
            case 0: // Today (Daily Sales)
                currentStartDate = sdf.format(cal.getTime());
                currentEndDate = currentStartDate;
                break;
            case 1: // This Week (Weekly Sales - Mon to Sun)
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                currentStartDate = sdf.format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 6);
                currentEndDate = sdf.format(cal.getTime());
                break;
            case 2: // Custom Date (already set by picker)
                break;
            case 3: // All Time
                currentStartDate = "2000-01-01";
                currentEndDate = "2099-12-31";
                break;
        }
    }

    private void setupDateFilterListeners() {
        if (filterToday != null) {
            filterToday.setOnClickListener(v -> {
                setupDateRange(0);
                updateDateFilterStyles();
                refreshHistory();
            });
        }

        if (filterWeek != null) {
            filterWeek.setOnClickListener(v -> {
                setupDateRange(1);
                updateDateFilterStyles();
                refreshHistory();
            });
        }

        if (filterCustom != null) {
            filterCustom.setOnClickListener(v -> showDatePickerDialog());
        }

        if (filterAllTime != null) {
            filterAllTime.setOnClickListener(v -> {
                setupDateRange(3);
                updateDateFilterStyles();
                refreshHistory();
            });
        }

        updateDateFilterStyles();
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            currentStartDate = sdf.format(selected.getTime());
            currentEndDate = currentStartDate;

            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            customDateLabel = "📅 " + displayFormat.format(selected.getTime());

            if (filterCustom != null) {
                filterCustom.setText(customDateLabel);
            }

            setupDateRange(2);
            updateDateFilterStyles();
            refreshHistory();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateDateFilterStyles() {
        styleFilterChip(filterToday, dateFilterMode == 0);
        styleFilterChip(filterWeek, dateFilterMode == 1);
        styleFilterChip(filterCustom, dateFilterMode == 2);
        styleFilterChip(filterAllTime, dateFilterMode == 3);
    }

    private void styleFilterChip(TextView chip, boolean isSelected) {
        if (chip == null) return;
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected);
            chip.setTextColor(Color.parseColor("#64748B"));
        }
    }

    private void setupTabClickListeners() {
        if (tabCustomerWise != null) {
            tabCustomerWise.setOnClickListener(v -> {
                isItemWise = false;
                updateTabStyles();
                refreshHistory();
            });
        }

        if (tabItemWise != null) {
            tabItemWise.setOnClickListener(v -> {
                isItemWise = true;
                updateTabStyles();
                refreshHistory();
            });
        }
    }

    private void updateTabStyles() {
        if (tabItemWise == null || tabCustomerWise == null) return;
        if (isItemWise) {
            tabItemWise.setBackgroundResource(R.drawable.bg_stock_ok);
            tabItemWise.setTextColor(Color.parseColor("#042F24"));
            tabItemWise.setTypeface(null, Typeface.BOLD);

            tabCustomerWise.setBackgroundResource(0);
            tabCustomerWise.setTextColor(Color.parseColor("#64748B"));
            tabCustomerWise.setTypeface(null, Typeface.NORMAL);

            if (tvListHeader != null) tvListHeader.setText("ITEM-WISE SALES BREAKDOWN");
        } else {
            tabCustomerWise.setBackgroundResource(R.drawable.bg_stock_ok);
            tabCustomerWise.setTextColor(Color.parseColor("#042F24"));
            tabCustomerWise.setTypeface(null, Typeface.BOLD);

            tabItemWise.setBackgroundResource(0);
            tabItemWise.setTextColor(Color.parseColor("#64748B"));
            tabItemWise.setTypeface(null, Typeface.NORMAL);

            if (tvListHeader != null) tvListHeader.setText("DELIVERED ORDERS");
        }
    }

    private void setupSearchListener() {
        if (etHistorySearch != null) {
            etHistorySearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshHistory(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void refreshHistory() {
        updateStats();
        refreshPieChart();
        if (llHistoryList == null) return;
        llHistoryList.removeAllViews();

        String query = etHistorySearch != null && etHistorySearch.getText() != null
                ? etHistorySearch.getText().toString().trim()
                : "";

        if (isItemWise) {
            populateItemWiseList(query);
        } else {
            populateCustomerWiseList(query);
        }
    }

    private void updateStats() {
        Cursor cursor = dbHelper.getSalesStatsByDate(currentStartDate, currentEndDate);
        double revenue = 0;
        int bills = 0;
        double due = 0;
        double collected = 0;
        int pendingStores = 0;

        if (cursor != null && cursor.moveToFirst()) {
            revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total_revenue"));
            bills = cursor.getInt(cursor.getColumnIndexOrThrow("total_bills"));
            due = cursor.getDouble(cursor.getColumnIndexOrThrow("total_due"));
            collected = cursor.getDouble(cursor.getColumnIndexOrThrow("total_collected"));
            pendingStores = cursor.getInt(cursor.getColumnIndexOrThrow("pending_stores"));

            if (tvStatRevenue != null) {
                tvStatRevenue.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) revenue));
            }
            if (tvStatBills != null) {
                tvStatBills.setText(bills + (bills == 1 ? " Bill" : " Bills"));
            }
            if (tvStatDue != null) {
                tvStatDue.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) due));
            }
            if (tvStatDueStores != null) {
                tvStatDueStores.setText(pendingStores + " pending");
            }
            if (tvStatProfit != null) {
                double netProfit = (collected > 0) ? collected : Math.max(0, revenue - due);
                tvStatProfit.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) netProfit));
            }
            if (tvStatProfitSub != null) {
                double pct = revenue > 0 ? (collected / revenue) * 100.0 : 0;
                tvStatProfitSub.setText(String.format(Locale.getDefault(), "%.0f%% collected", pct));
            }
            cursor.close();
        }

        updateSalesChart(revenue, bills);
    }

    private void updateSalesChart(double periodRevenue, int periodBills) {
        if (tvChartRevenueTotal != null) {
            tvChartRevenueTotal.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) periodRevenue));
        }

        if (dateFilterMode == 0 || dateFilterMode == 2) {
            // Today or Selected Single Date: 2-Hour Interval Chart starting from 8:00 AM
            buildHourlyIntervalChart(periodBills);
        } else if (dateFilterMode == 1) {
            // Weekly View: 7-Day Monday-to-Sunday Daily Breakdown (Moved from Daily to Weekly)
            buildWeeklyDailyChart(periodBills);
        } else {
            // All Time: 7-Day Trailing Trend
            buildTrailingDailyChart(periodBills);
        }
    }

    /**
     * Builds Today's (or selected single day's) Sales Chart in 2-hour intervals starting from 8:00 AM:
     * 8-10 AM, 10-12 PM, 12-2 PM, 2-4 PM, 4-6 PM, 6-8 PM, 8-10 PM
     */
    private void buildHourlyIntervalChart(int periodBills) {
        if (tvChartHeader != null) {
            tvChartHeader.setText(dateFilterMode == 0 ? "TODAY'S SALES (2-HR INTERVALS)" : "SALES FOR " + customDateLabel);
        }
        if (tvChartPeriodSub != null) {
            String pName = dateFilterMode == 0 ? "Today" : customDateLabel;
            tvChartPeriodSub.setText(pName + " • " + periodBills + (periodBills == 1 ? " bill" : " bills") + " recorded");
        }
        if (tvChartHint != null) {
            tvChartHint.setText("Tap any 2-hour interval bar to inspect sales");
        }

        String[] slotLabels = {"8-10 AM", "10-12 PM", "12-2 PM", "2-4 PM", "4-6 PM", "6-8 PM", "8-10 PM"};
        String[] slotFullLabels = {
                "8:00 AM - 10:00 AM",
                "10:00 AM - 12:00 PM",
                "12:00 PM - 2:00 PM",
                "2:00 PM - 4:00 PM",
                "4:00 PM - 6:00 PM",
                "6:00 PM - 8:00 PM",
                "8:00 PM - 10:00 PM"
        };
        double[] slotRevenue = new double[7];
        int[] slotBills = new int[7];

        Cursor cursor = dbHelper.getHourlySalesIntervals(currentStartDate);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int hour = cursor.getInt(cursor.getColumnIndexOrThrow("order_hour"));
                double rev = cursor.getDouble(cursor.getColumnIndexOrThrow("slot_revenue"));
                int bills = cursor.getInt(cursor.getColumnIndexOrThrow("slot_bills"));

                int slotIndex;
                if (hour < 10) slotIndex = 0;
                else if (hour < 12) slotIndex = 1;
                else if (hour < 14) slotIndex = 2;
                else if (hour < 16) slotIndex = 3;
                else if (hour < 18) slotIndex = 4;
                else if (hour < 20) slotIndex = 5;
                else slotIndex = 6;

                slotRevenue[slotIndex] += rev;
                slotBills[slotIndex] += bills;
            } while (cursor.moveToNext());
            cursor.close();
        }

        List<SalesChartView.BarData> chartData = new ArrayList<>();
        double peakRevenue = 0;
        String peakLabel = "None";

        for (int i = 0; i < 7; i++) {
            String tip = "Rs. " + String.format(Locale.getDefault(), "%,d", (long) slotRevenue[i])
                    + " • " + slotFullLabels[i] + " (" + slotBills[i] + " " + (slotBills[i] == 1 ? "bill" : "bills") + ")";
            chartData.add(new SalesChartView.BarData(slotLabels[i], slotRevenue[i], tip));
            if (slotRevenue[i] > peakRevenue) {
                peakRevenue = slotRevenue[i];
                peakLabel = slotLabels[i];
            }
        }

        if (tvChartBestDay != null) {
            if (peakRevenue > 0) {
                tvChartBestDay.setText("Peak: Rs. " + String.format(Locale.getDefault(), "%,d", (long) peakRevenue) + " (" + peakLabel + ")");
                tvChartBestDay.setVisibility(View.VISIBLE);
            } else {
                tvChartBestDay.setText("Peak: Rs. 0");
            }
        }

        if (salesChartView != null) {
            salesChartView.setData(chartData);
        }
    }

    /**
     * Builds Weekly Sales Chart (Monday to Sunday) - moved from Daily view to Weekly view.
     */
    private void buildWeeklyDailyChart(int periodBills) {
        if (tvChartHeader != null) {
            tvChartHeader.setText("WEEKLY REVENUE TREND");
        }
        if (tvChartPeriodSub != null) {
            tvChartPeriodSub.setText("This Week • " + periodBills + (periodBills == 1 ? " bill" : " bills") + " recorded");
        }
        if (tvChartHint != null) {
            tvChartHint.setText("Tap any day bar to inspect daily sales");
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat fullLabelFormat = new SimpleDateFormat("d MMM (EEE)", Locale.getDefault());

        Calendar startCal = Calendar.getInstance();
        startCal.setFirstDayOfWeek(Calendar.MONDAY);
        startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DAY_OF_MONTH, 6);

        String queryStart = dbFormat.format(startCal.getTime());
        String queryEnd = dbFormat.format(endCal.getTime());

        Map<String, Double> dayRevenueMap = new HashMap<>();
        Cursor trendCursor = dbHelper.getDailySalesTrend(queryStart, queryEnd);
        if (trendCursor != null && trendCursor.moveToFirst()) {
            do {
                String saleDate = trendCursor.getString(trendCursor.getColumnIndexOrThrow("sale_date"));
                double dayRev = trendCursor.getDouble(trendCursor.getColumnIndexOrThrow("day_revenue"));
                dayRevenueMap.put(saleDate, dayRev);
            } while (trendCursor.moveToNext());
            trendCursor.close();
        }

        List<SalesChartView.BarData> chartData = new ArrayList<>();
        Calendar cursorCal = (Calendar) startCal.clone();
        double peakRevenue = 0;
        String peakDay = "None";

        while (!cursorCal.after(endCal)) {
            String dateKey = dbFormat.format(cursorCal.getTime());
            String dayLabel = labelFormat.format(cursorCal.getTime());
            String fullLabel = fullLabelFormat.format(cursorCal.getTime());

            double dayVal = dayRevenueMap.containsKey(dateKey) ? dayRevenueMap.get(dateKey) : 0;
            if (dayVal > peakRevenue) {
                peakRevenue = dayVal;
                peakDay = dayLabel;
            }

            String formattedVal = "Rs. " + String.format(Locale.getDefault(), "%,d", (long) dayVal) + " • " + fullLabel;
            chartData.add(new SalesChartView.BarData(dayLabel, dayVal, formattedVal));
            cursorCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (tvChartBestDay != null) {
            if (peakRevenue > 0) {
                tvChartBestDay.setText("Peak: Rs. " + String.format(Locale.getDefault(), "%,d", (long) peakRevenue) + " (" + peakDay + ")");
                tvChartBestDay.setVisibility(View.VISIBLE);
            } else {
                tvChartBestDay.setText("Peak: Rs. 0");
            }
        }

        if (salesChartView != null) {
            salesChartView.setData(chartData);
        }
    }

    /**
     * Builds 7-Day trailing trend for All-Time view.
     */
    private void buildTrailingDailyChart(int periodBills) {
        if (tvChartHeader != null) {
            tvChartHeader.setText("7-DAY REVENUE TREND");
        }
        if (tvChartPeriodSub != null) {
            tvChartPeriodSub.setText("All Time • " + periodBills + (periodBills == 1 ? " bill" : " bills") + " recorded");
        }
        if (tvChartHint != null) {
            tvChartHint.setText("Tap any day bar to inspect daily sales");
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat fullLabelFormat = new SimpleDateFormat("d MMM (EEE)", Locale.getDefault());

        Calendar endCal = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.DAY_OF_MONTH, -6);

        String queryStart = dbFormat.format(startCal.getTime());
        String queryEnd = dbFormat.format(endCal.getTime());

        Map<String, Double> dayRevenueMap = new HashMap<>();
        Cursor trendCursor = dbHelper.getDailySalesTrend(queryStart, queryEnd);
        if (trendCursor != null && trendCursor.moveToFirst()) {
            do {
                String saleDate = trendCursor.getString(trendCursor.getColumnIndexOrThrow("sale_date"));
                double dayRev = trendCursor.getDouble(trendCursor.getColumnIndexOrThrow("day_revenue"));
                dayRevenueMap.put(saleDate, dayRev);
            } while (trendCursor.moveToNext());
            trendCursor.close();
        }

        List<SalesChartView.BarData> chartData = new ArrayList<>();
        Calendar cursorCal = (Calendar) startCal.clone();
        double peakRevenue = 0;
        String peakDay = "None";

        while (!cursorCal.after(endCal)) {
            String dateKey = dbFormat.format(cursorCal.getTime());
            String dayLabel = labelFormat.format(cursorCal.getTime());
            String fullLabel = fullLabelFormat.format(cursorCal.getTime());

            double dayVal = dayRevenueMap.containsKey(dateKey) ? dayRevenueMap.get(dateKey) : 0;
            if (dayVal > peakRevenue) {
                peakRevenue = dayVal;
                peakDay = dayLabel;
            }

            String formattedVal = "Rs. " + String.format(Locale.getDefault(), "%,d", (long) dayVal) + " • " + fullLabel;
            chartData.add(new SalesChartView.BarData(dayLabel, dayVal, formattedVal));
            cursorCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (tvChartBestDay != null) {
            if (peakRevenue > 0) {
                tvChartBestDay.setText("Peak: Rs. " + String.format(Locale.getDefault(), "%,d", (long) peakRevenue) + " (" + peakDay + ")");
                tvChartBestDay.setVisibility(View.VISIBLE);
            } else {
                tvChartBestDay.setText("Peak: Rs. 0");
            }
        }

        if (salesChartView != null) {
            salesChartView.setData(chartData);
        }
    }

    private static class TempItemSale {
        final String name;
        final double price;
        final int qty;
        final double total;

        TempItemSale(String name, double price, int qty, double total) {
            this.name = name;
            this.price = price;
            this.qty = qty;
            this.total = total;
        }
    }

    private void refreshPieChart() {
        Cursor cursor = dbHelper.getItemWiseSalesByDate(currentStartDate, currentEndDate, "");
        int totalUnitsSold = 0;
        double totalItemRevenue = 0;
        List<TempItemSale> itemsList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));

                totalUnitsSold += qty;
                totalItemRevenue += total;
                itemsList.add(new TempItemSale(name, price, qty, total));
            } while (cursor.moveToNext());
            cursor.close();
        }

        updateItemPieChart(itemsList, totalUnitsSold, totalItemRevenue);
    }

    private void populateItemWiseList(String query) {
        Cursor cursor = dbHelper.getItemWiseSalesByDate(currentStartDate, currentEndDate, query);
        int count = 0;
        int totalUnitsSold = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));

                totalUnitsSold += qty;

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_history_item, llHistoryList, false);
                TextView tvName = row.findViewById(R.id.tvItemName);
                TextView tvDetails = row.findViewById(R.id.tvItemDetails);
                TextView tvTotal = row.findViewById(R.id.tvItemTotal);

                if (tvName != null) tvName.setText(name);
                if (tvDetails != null) {
                    tvDetails.setText("Rs. " + (int) price + " / unit • " + qty + " units sold");
                }
                if (tvTotal != null) {
                    tvTotal.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) total));
                }

                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (tvListCount != null) {
            tvListCount.setText(count + " products (" + totalUnitsSold + " units)");
        }

        if (llHistoryEmpty != null) {
            llHistoryEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void updateItemPieChart(List<TempItemSale> itemsList, int totalUnitsSold, double totalItemRevenue) {
        if (itemPieChartView == null) return;

        if (itemsList == null || itemsList.isEmpty() || totalUnitsSold <= 0) {
            itemPieChartView.setData(new ArrayList<>(), 0, 0);
            if (tvPieTopItem != null) tvPieTopItem.setText("Top: None");
            if (llPieLegend != null) llPieLegend.removeAllViews();
            return;
        }

        // Top Item Badge
        TempItemSale topSale = itemsList.get(0);
        if (tvPieTopItem != null) {
            tvPieTopItem.setText("Top: " + topSale.name + " (" + topSale.qty + ")");
        }

        List<ItemPieChartView.PieEntry> pieEntries = new ArrayList<>();
        int maxSlices = 7;
        int othersQty = 0;
        double othersRevenue = 0;

        for (int i = 0; i < itemsList.size(); i++) {
            TempItemSale item = itemsList.get(i);
            if (i < maxSlices) {
                int color = ItemPieChartView.PALETTE[i % ItemPieChartView.PALETTE.length];
                double percentage = (item.qty * 100.0) / totalUnitsSold;
                String formatted = "Rs. " + String.format(Locale.getDefault(), "%,d", (long) item.total);
                pieEntries.add(new ItemPieChartView.PieEntry(item.name, item.total, item.qty, percentage, color, formatted));
            } else {
                othersQty += item.qty;
                othersRevenue += item.total;
            }
        }

        if (othersQty > 0) {
            int color = Color.parseColor("#64748B"); // Slate for Others
            double percentage = (othersQty * 100.0) / totalUnitsSold;
            String formatted = "Rs. " + String.format(Locale.getDefault(), "%,d", (long) othersRevenue);
            pieEntries.add(new ItemPieChartView.PieEntry("Others", othersRevenue, othersQty, percentage, color, formatted));
        }

        itemPieChartView.setData(pieEntries, totalUnitsSold, totalItemRevenue);

        // Populate Legend
        populatePieLegend(pieEntries);
    }

    private void populatePieLegend(List<ItemPieChartView.PieEntry> pieEntries) {
        if (llPieLegend == null) return;
        llPieLegend.removeAllViews();

        for (int i = 0; i < pieEntries.size(); i++) {
            final int sliceIndex = i;
            final ItemPieChartView.PieEntry entry = pieEntries.get(i);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dpToPx(5), 0, dpToPx(5));
            row.setClickable(true);
            row.setFocusable(true);

            // Color circle dot
            View dot = new View(requireContext());
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
            dotLp.setMargins(0, 0, dpToPx(10), 0);
            dot.setLayoutParams(dotLp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(entry.color);
            dot.setBackground(gd);
            row.addView(dot);

            // Item Name
            TextView tvName = new TextView(requireContext());
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameLp);
            tvName.setText(entry.label);
            tvName.setTextSize(12.5f);
            tvName.setTextColor(Color.parseColor("#0F172A"));
            tvName.setTypeface(null, Typeface.BOLD);
            row.addView(tvName);

            // Volume & Percentage
            TextView tvVolume = new TextView(requireContext());
            tvVolume.setText(entry.quantity + " (" + String.format(Locale.getDefault(), "%.1f%%", entry.percentage) + ")");
            tvVolume.setTextSize(12f);
            tvVolume.setTextColor(Color.parseColor("#047857"));
            tvVolume.setTypeface(null, Typeface.BOLD);
            tvVolume.setPadding(0, 0, dpToPx(12), 0);
            row.addView(tvVolume);

            // Revenue
            TextView tvRev = new TextView(requireContext());
            tvRev.setText(entry.formattedValue);
            tvRev.setTextSize(11.5f);
            tvRev.setTextColor(Color.parseColor("#64748B"));
            row.addView(tvRev);

            // Clicking on legend row highlights slice
            row.setOnClickListener(v -> {
                if (itemPieChartView != null) {
                    itemPieChartView.setSelectedIndex(itemPieChartView.getSelectedIndex() == sliceIndex ? -1 : sliceIndex);
                }
            });

            llPieLegend.addView(row);
        }
    }

    private void populateCustomerWiseList(String query) {
        Cursor cursor = dbHelper.getCustomerWiseSalesByDate(currentStartDate, currentEndDate, query);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                final int customerId = cursor.getInt(cursor.getColumnIndexOrThrow("customer_id"));
                final String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                final String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                final String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                final String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT));
                final String orderDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE));
                final double orderTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_TOTAL));
                final double orderDue = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DUE));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_history_customer, llHistoryList, false);

                TextView tvAvatar = row.findViewById(R.id.tvCustomerAvatar);
                TextView tvName = row.findViewById(R.id.tvCustomerName);
                TextView tvStore = row.findViewById(R.id.tvStoreName);
                TextView tvPhone = row.findViewById(R.id.tvCustomerPhone);
                TextView tvTotal = row.findViewById(R.id.tvCustomerTotal);
                TextView tvDue = row.findViewById(R.id.tvCustomerDue);

                String initials = "";
                if (firstName != null && firstName.length() > 0) initials += firstName.charAt(0);
                if (lastName != null && lastName.length() > 0) initials += lastName.charAt(0);

                if (tvAvatar != null) tvAvatar.setText(initials.toUpperCase(Locale.getDefault()));
                if (tvName != null) tvName.setText(firstName + " " + lastName);
                if (tvStore != null) tvStore.setText(storeName);
                if (tvPhone != null) {
                    tvPhone.setText(orderDate != null ? orderDate : contact);
                }
                if (tvTotal != null) {
                    tvTotal.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) orderTotal));
                }

                if (tvDue != null) {
                    if (orderDue > 0) {
                        tvDue.setText("Due: Rs. " + (int) orderDue);
                        tvDue.setBackgroundResource(R.drawable.bg_stat_due);
                        tvDue.setTextColor(getResources().getColor(R.color.due_text, null));
                        tvDue.setVisibility(View.VISIBLE);
                    } else if (orderDue < 0) {
                        tvDue.setText("Credit: Rs. " + (int) Math.abs(orderDue));
                        tvDue.setBackgroundResource(R.drawable.bg_chip_unselected);
                        tvDue.setTextColor(Color.parseColor("#10B981"));
                        tvDue.setVisibility(View.VISIBLE);
                    } else {
                        tvDue.setText("Settled");
                        tvDue.setBackgroundResource(R.drawable.bg_chip_unselected);
                        tvDue.setTextColor(getResources().getColor(R.color.primary_green, null));
                        tvDue.setVisibility(View.VISIBLE);
                    }
                }

                row.setOnClickListener(v -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showBillDetailsFragment(orderId, storeName);
                    }
                });

                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (tvListCount != null) {
            tvListCount.setText(count + (count == 1 ? " delivery bill" : " delivery bills"));
        }

        if (llHistoryEmpty != null) {
            llHistoryEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
