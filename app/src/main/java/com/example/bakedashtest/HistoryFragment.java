package com.example.bakedashtest;

import android.database.Cursor;
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

public class HistoryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvStatRevenue, tvStatBills, tvStatDue, tvStatDueStores;
    private TextView tabCustomerWise, tabItemWise;
    private TextView tvListHeader, tvListCount;
    private EditText etHistorySearch;
    private LinearLayout llHistoryList;
    private boolean isItemWise = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvStatRevenue = view.findViewById(R.id.tvStatRevenue);
        tvStatBills = view.findViewById(R.id.tvStatBills);
        tvStatDue = view.findViewById(R.id.tvStatDue);
        tvStatDueStores = view.findViewById(R.id.tvStatDueStores);

        tabCustomerWise = view.findViewById(R.id.tabCustomerWise);
        tabItemWise = view.findViewById(R.id.tabItemWise);
        
        tvListHeader = view.findViewById(R.id.tvListHeader);
        tvListCount = view.findViewById(R.id.tvListCount);
        etHistorySearch = view.findViewById(R.id.etHistorySearch);
        llHistoryList = view.findViewById(R.id.llHistoryList);

        setupClickListeners();
        setupSearchListener();
        refreshHistory();

        return view;
    }

    private void setupClickListeners() {
        tabCustomerWise.setOnClickListener(v -> {
            isItemWise = false;
            updateTabStyles();
            refreshHistory();
        });

        tabItemWise.setOnClickListener(v -> {
            isItemWise = true;
            updateTabStyles();
            refreshHistory();
        });
    }

    private void updateTabStyles() {
        if (isItemWise) {
            tabItemWise.setBackgroundResource(R.drawable.bg_tab_selected);
            tabItemWise.setTextColor(getResources().getColor(R.color.revenue_text, null));
            tabItemWise.setTypeface(null, android.graphics.Typeface.BOLD);

            tabCustomerWise.setBackgroundResource(0);
            tabCustomerWise.setTextColor(getResources().getColor(R.color.text_grey, null));
            tabCustomerWise.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            tvListHeader.setText("TOP MOVING ITEMS");
        } else {
            tabCustomerWise.setBackgroundResource(R.drawable.bg_tab_selected);
            tabCustomerWise.setTextColor(getResources().getColor(R.color.revenue_text, null));
            tabCustomerWise.setTypeface(null, android.graphics.Typeface.BOLD);

            tabItemWise.setBackgroundResource(0);
            tabItemWise.setTextColor(getResources().getColor(R.color.text_grey, null));
            tabItemWise.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            tvListHeader.setText("DELIVERED STORES");
        }
    }

    private void setupSearchListener() {
        etHistorySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshHistory(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshHistory() {
        updateStats();
        llHistoryList.removeAllViews();
        
        String query = etHistorySearch.getText().toString().trim();
        
        if (isItemWise) {
            populateItemWiseList(query);
        } else {
            populateCustomerWiseList(query);
        }
    }

    private void updateStats() {
        Cursor cursor = dbHelper.getTodayStats();
        if (cursor != null && cursor.moveToFirst()) {
            double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total_revenue"));
            int bills = cursor.getInt(cursor.getColumnIndexOrThrow("total_bills"));
            double due = cursor.getDouble(cursor.getColumnIndexOrThrow("total_due"));
            int pendingStores = cursor.getInt(cursor.getColumnIndexOrThrow("pending_stores"));

            tvStatRevenue.setText(String.valueOf((int)revenue));
            tvStatBills.setText(String.valueOf(bills));
            tvStatDue.setText(String.valueOf((int)due));
            tvStatDueStores.setText(pendingStores + " stores pending");
            cursor.close();
        }
    }

    private void populateItemWiseList(String query) {
        Cursor cursor = dbHelper.getItemWiseSales(query);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_history_item, llHistoryList, false);
                ((TextView)row.findViewById(R.id.tvItemName)).setText(name);
                ((TextView)row.findViewById(R.id.tvItemDetails)).setText((int)price + " / unit • " + qty + " units");
                ((TextView)row.findViewById(R.id.tvItemTotal)).setText(String.valueOf((int)total));
                
                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        tvListCount.setText(count + " items sold");
    }

    private void populateCustomerWiseList(String query) {
        Cursor cursor = dbHelper.getCustomerWiseSales(query);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT));
                double totalSales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_sales"));
                double currentDue = cursor.getDouble(cursor.getColumnIndexOrThrow("current_due"));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_history_customer, llHistoryList, false);
                
                String initials = "";
                if (firstName.length() > 0) initials += firstName.charAt(0);
                if (lastName.length() > 0) initials += lastName.charAt(0);
                
                ((TextView)row.findViewById(R.id.tvCustomerAvatar)).setText(initials.toUpperCase());
                ((TextView)row.findViewById(R.id.tvCustomerName)).setText(firstName + " " + lastName);
                ((TextView)row.findViewById(R.id.tvStoreName)).setText(storeName);
                ((TextView)row.findViewById(R.id.tvCustomerPhone)).setText(contact);
                ((TextView)row.findViewById(R.id.tvCustomerTotal)).setText(String.valueOf((int)totalSales));
                
                TextView tvDue = row.findViewById(R.id.tvCustomerDue);
                if (currentDue > 0) {
                    tvDue.setText("Due " + (int)currentDue);
                    tvDue.setVisibility(View.VISIBLE);
                } else {
                    tvDue.setText("Settled");
                    tvDue.setBackgroundResource(R.drawable.bg_filter_unselected);
                    tvDue.setTextColor(getResources().getColor(R.color.primary_green, null));
                }

                int customerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                row.setOnClickListener(v -> {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.showCustomerBillingHistoryFragment(customerId, firstName + " " + lastName, storeName, totalSales, currentDue);
                    }
                });

                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        tvListCount.setText(count + " stores");
    }
}
