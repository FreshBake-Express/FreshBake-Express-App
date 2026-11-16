package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StockFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvInventoryDate, tvTotalItems, tvAvailableStock, tvLowStock, tvOutOfStock;
    private TextView tvStockItemsCount, tvCategoryFilter;
    private EditText etStockSearch;
    private LinearLayout llStockList, llAlertBox;
    private TextView tvAlertTitle, tvAlertDesc;
    private TextView tvFilterAll, tvFilterInStock, tvFilterLowStock, tvFilterOutOfStock;
    private Button btnAddGRN, btnViewGRNHistory;

    private int stockFilterMode = 0; // 0: All, 1: In Stock, 2: Low Stock, 3: Out of Stock
    private String stockCategoryFilter = "All categories";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvInventoryDate = view.findViewById(R.id.tvInventoryDate);
        tvTotalItems = view.findViewById(R.id.tvTotalItems);
        tvAvailableStock = view.findViewById(R.id.tvAvailableStock);
        tvLowStock = view.findViewById(R.id.tvLowStock);
        tvOutOfStock = view.findViewById(R.id.tvOutOfStock);
        tvStockItemsCount = view.findViewById(R.id.tvStockItemsCount);
        tvCategoryFilter = view.findViewById(R.id.tvCategoryFilter);
        etStockSearch = view.findViewById(R.id.etStockSearch);
        llStockList = view.findViewById(R.id.llStockList);
        llAlertBox = view.findViewById(R.id.llAlertBox);
        tvAlertTitle = view.findViewById(R.id.tvAlertTitle);
        tvAlertDesc = view.findViewById(R.id.tvAlertDesc);

        tvFilterAll = view.findViewById(R.id.tvFilterAll);
        tvFilterInStock = view.findViewById(R.id.tvFilterInStock);
        tvFilterLowStock = view.findViewById(R.id.tvFilterLowStock);
        tvFilterOutOfStock = view.findViewById(R.id.tvFilterOutOfStock);

        btnAddGRN = view.findViewById(R.id.btnAddGRN);
        btnViewGRNHistory = view.findViewById(R.id.btnViewGRNHistory);

        tvInventoryDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()));

        setupClickListeners();
        setupSearchListener();
        refreshStockList();

        return view;
    }

    private void setupClickListeners() {
        tvFilterAll.setOnClickListener(v -> { stockFilterMode = 0; updateStockFilterStyles(); refreshStockList(); });
        tvFilterInStock.setOnClickListener(v -> { stockFilterMode = 1; updateStockFilterStyles(); refreshStockList(); });
        tvFilterLowStock.setOnClickListener(v -> { stockFilterMode = 2; updateStockFilterStyles(); refreshStockList(); });
        tvFilterOutOfStock.setOnClickListener(v -> { stockFilterMode = 3; updateStockFilterStyles(); refreshStockList(); });

        btnAddGRN.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.showAddGRNFragment();
            }
        });
        
        btnViewGRNHistory.setOnClickListener(v -> {
            // TODO: Implement GRN History Fragment if needed, or show dialog
        });
    }

    private void setupSearchListener() {
        etStockSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshStockList(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateStockFilterStyles() {
        tvFilterAll.setBackgroundResource(stockFilterMode == 0 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        tvFilterAll.setTextColor(getResources().getColor(stockFilterMode == 0 ? R.color.stockroom_teal : R.color.text_grey, null));

        tvFilterInStock.setBackgroundResource(stockFilterMode == 1 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        tvFilterInStock.setTextColor(getResources().getColor(stockFilterMode == 1 ? R.color.stockroom_teal : R.color.text_grey, null));

        tvFilterLowStock.setBackgroundResource(stockFilterMode == 2 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        tvFilterLowStock.setTextColor(getResources().getColor(stockFilterMode == 2 ? R.color.stockroom_teal : R.color.text_grey, null));

        tvFilterOutOfStock.setBackgroundResource(stockFilterMode == 3 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        tvFilterOutOfStock.setTextColor(getResources().getColor(stockFilterMode == 3 ? R.color.stockroom_teal : R.color.text_grey, null));
    }

    private void refreshStockList() {
        llStockList.removeAllViews();
        Cursor cursor = dbHelper.getAllItems();
        int total = 0, available = 0, low = 0, out = 0;
        int listCount = 0;
        String query = etStockSearch.getText().toString().toLowerCase();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));
                int reserved = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVED));
                int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL));
                int avail = onHand - reserved;

                total++;
                available += avail;
                if (onHand <= 0) out++;
                else if (onHand <= reorder) low++;

                boolean matchesQuery = name.toLowerCase().contains(query) || sku.toLowerCase().contains(query);
                boolean matchesFilter = true;
                if (stockFilterMode == 1 && onHand <= reorder) matchesFilter = false;
                else if (stockFilterMode == 2 && (onHand <= 0 || onHand > reorder)) matchesFilter = false;
                else if (stockFilterMode == 3 && onHand > 0) matchesFilter = false;

                if (matchesQuery && matchesFilter) {
                    addStockRow(id, name, sku, category, onHand, reserved, reorder);
                    listCount++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        tvTotalItems.setText(String.valueOf(total));
        tvAvailableStock.setText(String.valueOf(available));
        tvLowStock.setText(String.valueOf(low));
        tvOutOfStock.setText(String.valueOf(out));
        tvStockItemsCount.setText(listCount + " items");

        if (low + out > 0) {
            llAlertBox.setVisibility(View.VISIBLE);
            tvAlertTitle.setText((low + out) + " items need replenishment");
            tvAlertDesc.setText(low + " low stock - " + out + " out of stock. Use Add GRN to record deliveries.");
        } else {
            llAlertBox.setVisibility(View.GONE);
        }
    }

    private void addStockRow(int id, String name, String sku, String category, int onHand, int reserved, int reorder) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stock_row, llStockList, false);
        ((TextView)row.findViewById(R.id.tvStockItemName)).setText(name);
        ((TextView)row.findViewById(R.id.tvStockSKU)).setText(sku + " • " + category);
        ((TextView)row.findViewById(R.id.tvOnHandUnits)).setText(String.valueOf(onHand));
        ((TextView)row.findViewById(R.id.tvReservedUnits)).setText(String.valueOf(reserved));
        ((TextView)row.findViewById(R.id.tvReorderLevel)).setText("Reorder at " + reorder + " units");
        
        TextView tvAvail = row.findViewById(R.id.tvAvailableUnits);
        int avail = onHand - reserved;
        tvAvail.setText(String.valueOf(avail));

        TextView tvStatus = row.findViewById(R.id.tvStockStatus);
        if (onHand <= 0) {
            tvStatus.setText("Out of Stock");
            tvStatus.setBackgroundResource(R.drawable.bg_status_out);
            tvStatus.setTextColor(getResources().getColor(R.color.nav_logout_text, null));
        } else if (onHand <= reorder) {
            tvStatus.setText("Low Stock");
            tvStatus.setBackgroundResource(R.drawable.bg_status_low);
            tvStatus.setTextColor(getResources().getColor(R.color.due_text, null));
        } else {
            tvStatus.setText("In Stock");
            tvStatus.setBackgroundResource(R.drawable.bg_status_in);
            tvStatus.setTextColor(getResources().getColor(R.color.primary_green, null));
        }

        llStockList.addView(row);
    }
}
