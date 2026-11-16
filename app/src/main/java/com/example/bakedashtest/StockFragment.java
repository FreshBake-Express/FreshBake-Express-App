package com.example.bakedashtest;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class StockFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvStatTotalUnits, tvStatLowAlerts, tvStatStockValue;
    private TextView tvStockItemsCount;
    private EditText etStockSearch;
    private LinearLayout llStockList, llEmptyState;
    private TextView tvFilterAll, tvFilterInStock, tvFilterLowStock, tvFilterOutOfStock;
    private MaterialButton btnRestockLow, btnAddGRN;

    private int stockFilterMode = 0; // 0: All, 1: In Stock, 2: Low Stock, 3: Out of Stock

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvStatTotalUnits = view.findViewById(R.id.tvStatTotalUnits);
        tvStatLowAlerts = view.findViewById(R.id.tvStatLowAlerts);
        tvStatStockValue = view.findViewById(R.id.tvStatStockValue);
        tvStockItemsCount = view.findViewById(R.id.tvStockItemsCount);
        etStockSearch = view.findViewById(R.id.etStockSearch);
        llStockList = view.findViewById(R.id.llStockList);
        llEmptyState = view.findViewById(R.id.llEmptyState);

        tvFilterAll = view.findViewById(R.id.tvFilterAll);
        tvFilterInStock = view.findViewById(R.id.tvFilterInStock);
        tvFilterLowStock = view.findViewById(R.id.tvFilterLowStock);
        tvFilterOutOfStock = view.findViewById(R.id.tvFilterOutOfStock);

        btnRestockLow = view.findViewById(R.id.btnRestockLow);
        btnAddGRN = view.findViewById(R.id.btnAddGRN);

        setupClickListeners();
        setupSearchListener();
        refreshStockList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStockList();
    }

    private void setupClickListeners() {
        tvFilterAll.setOnClickListener(v -> { stockFilterMode = 0; updateStockFilterStyles(); refreshStockList(); });
        tvFilterInStock.setOnClickListener(v -> { stockFilterMode = 1; updateStockFilterStyles(); refreshStockList(); });
        tvFilterLowStock.setOnClickListener(v -> { stockFilterMode = 2; updateStockFilterStyles(); refreshStockList(); });
        tvFilterOutOfStock.setOnClickListener(v -> { stockFilterMode = 3; updateStockFilterStyles(); refreshStockList(); });

        if (btnAddGRN != null) {
            btnAddGRN.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddGRNFragment();
                }
            });
        }

        if (btnRestockLow != null) {
            btnRestockLow.setOnClickListener(v -> restockAllLowItems());
        }
    }

    private void setupSearchListener() {
        if (etStockSearch != null) {
            etStockSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshStockList(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updateStockFilterStyles() {
        styleFilterChip(tvFilterAll, stockFilterMode == 0);
        styleFilterChip(tvFilterInStock, stockFilterMode == 1);
        styleFilterChip(tvFilterLowStock, stockFilterMode == 2);
        styleFilterChip(tvFilterOutOfStock, stockFilterMode == 3);
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

    private void refreshStockList() {
        if (llStockList == null) return;
        llStockList.removeAllViews();

        Cursor cursor = dbHelper.getAllItems();
        int totalLoaded = 0;
        int lowAlerts = 0;
        double totalStockValue = 0;
        int visibleCount = 0;

        String query = etStockSearch != null && etStockSearch.getText() != null
                ? etStockSearch.getText().toString().trim().toLowerCase(Locale.getDefault())
                : "";

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                final String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
                final String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                final double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                final int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));
                final int reserved = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVED));
                final int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL));
                final int isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE));

                totalLoaded += onHand;
                totalStockValue += (onHand * cost);

                boolean isOut = (onHand <= 0);
                boolean isLow = (onHand > 0 && onHand <= reorder);
                if (isOut || isLow) {
                    lowAlerts++;
                }

                boolean matchesQuery = query.isEmpty()
                        || (name != null && name.toLowerCase(Locale.getDefault()).contains(query))
                        || (sku != null && sku.toLowerCase(Locale.getDefault()).contains(query));

                boolean matchesFilter = true;
                if (stockFilterMode == 1 && (onHand <= reorder)) {
                    matchesFilter = false; // In Stock only
                } else if (stockFilterMode == 2 && !isLow) {
                    matchesFilter = false; // Low Stock only
                } else if (stockFilterMode == 3 && !isOut) {
                    matchesFilter = false; // Out of Stock only
                }

                if (matchesQuery && matchesFilter) {
                    visibleCount++;
                    addStockRow(id, name, sku, category, cost, onHand, reserved, reorder, isActive);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Update KPI Strip
        if (tvStatTotalUnits != null) {
            tvStatTotalUnits.setText(String.valueOf(totalLoaded));
        }
        if (tvStatLowAlerts != null) {
            tvStatLowAlerts.setText(String.valueOf(lowAlerts));
        }
        if (tvStatStockValue != null) {
            tvStatStockValue.setText("Rs. " + String.format(Locale.getDefault(), "%,d", (long) totalStockValue));
        }
        if (tvStockItemsCount != null) {
            tvStockItemsCount.setText(visibleCount + (visibleCount == 1 ? " item" : " items"));
        }
        if (llEmptyState != null) {
            llEmptyState.setVisibility(visibleCount == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void addStockRow(final int id, final String name, final String sku, final String category,
                             final double cost, final int onHand, final int reserved, final int reorder, final int isActive) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_stock_row, llStockList, false);

        TextView tvTag = row.findViewById(R.id.tvStockTag);
        TextView tvName = row.findViewById(R.id.tvStockItemName);
        TextView tvMeta = row.findViewById(R.id.tvStockMeta);
        TextView tvStatus = row.findViewById(R.id.tvStockStatus);
        TextView tvLevelText = row.findViewById(R.id.tvStockLevelText);
        TextView tvItemValue = row.findViewById(R.id.tvStockItemValue);
        ProgressBar progressBar = row.findViewById(R.id.progressStockLevel);

        TextView btnMinus = row.findViewById(R.id.btnStockMinus);
        TextView tvQty = row.findViewById(R.id.tvStockQty);
        TextView btnPlus = row.findViewById(R.id.btnStockPlus);
        TextView btnQuickTen = row.findViewById(R.id.btnQuickPlusTen);
        TextView btnAdjust = row.findViewById(R.id.btnAdjustStock);

        if (tvTag != null) {
            tvTag.setText(getInitials(name));
        }
        if (tvName != null) {
            tvName.setText(name);
        }
        if (tvMeta != null) {
            String priceStr = (cost % 1 == 0) ? String.valueOf((int) cost) : String.format(Locale.getDefault(), "%.2f", cost);
            tvMeta.setText("Price: Rs. " + priceStr + " • Reorder at " + reorder + " units");
        }

        // Status badge
        if (tvStatus != null) {
            if (onHand <= 0) {
                tvStatus.setText("Out of Stock");
                tvStatus.setBackgroundResource(R.drawable.bg_stock_out);
                tvStatus.setTextColor(Color.parseColor("#E11D48"));
            } else if (onHand <= reorder) {
                tvStatus.setText("Low Stock");
                tvStatus.setBackgroundResource(R.drawable.bg_due_amber);
                tvStatus.setTextColor(Color.parseColor("#B45309"));
            } else {
                tvStatus.setText("In Stock");
                tvStatus.setBackgroundResource(R.drawable.bg_stock_ok);
                tvStatus.setTextColor(Color.parseColor("#065F46"));
            }
        }

        // Level & Capacity Bar
        int maxCapacity = Math.max(100, Math.max(onHand, reorder * 3));
        if (progressBar != null) {
            progressBar.setMax(maxCapacity);
            progressBar.setProgress(onHand);
        }
        if (tvLevelText != null) {
            int available = Math.max(0, onHand - reserved);
            tvLevelText.setText(onHand + " on hand (" + available + " avail)");
        }
        if (tvItemValue != null) {
            long val = (long) (onHand * cost);
            tvItemValue.setText("Value: Rs. " + String.format(Locale.getDefault(), "%,d", val));
        }

        // Live Stepper
        if (tvQty != null) {
            tvQty.setText(String.valueOf(onHand));
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (onHand > 0) {
                    dbHelper.setOnHandStock(id, onHand - 1);
                    refreshStockList();
                }
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                dbHelper.setOnHandStock(id, onHand + 1);
                refreshStockList();
            });
        }

        if (btnQuickTen != null) {
            btnQuickTen.setOnClickListener(v -> {
                dbHelper.addStock(id, 10);
                Toast.makeText(requireContext(), "+10 units added to " + name, Toast.LENGTH_SHORT).show();
                refreshStockList();
            });
        }

        if (btnAdjust != null) {
            btnAdjust.setOnClickListener(v -> showAdjustDialog(id, name, sku, category, cost, onHand, reorder, isActive));
        }

        llStockList.addView(row);
    }

    private void showAdjustDialog(final int id, final String name, final String sku, final String category,
                                  final double cost, final int currentOnHand, final int currentReorder, final int isActive) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_adjust_stock, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogProductTitle);
        final EditText etStock = dialogView.findViewById(R.id.etDialogStockQty);
        final EditText etAlert = dialogView.findViewById(R.id.etDialogMinAlert);

        if (tvTitle != null) {
            tvTitle.setText("Adjust Stock — " + name);
        }
        if (etStock != null) {
            etStock.setText(String.valueOf(currentOnHand));
        }
        if (etAlert != null) {
            etAlert.setText(String.valueOf(currentReorder));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String stockStr = etStock != null ? etStock.getText().toString().trim() : "";
                    String alertStr = etAlert != null ? etAlert.getText().toString().trim() : "";

                    int newOnHand = stockStr.isEmpty() ? currentOnHand : Integer.parseInt(stockStr);
                    int newReorder = alertStr.isEmpty() ? currentReorder : Integer.parseInt(alertStr);

                    dbHelper.setOnHandStock(id, newOnHand);
                    dbHelper.updateItem(id, name, cost, sku != null ? sku : "", category != null ? category : "", newReorder, isActive);

                    Toast.makeText(requireContext(), "Updated " + name + " stock", Toast.LENGTH_SHORT).show();
                    refreshStockList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void restockAllLowItems() {
        Cursor cursor = dbHelper.getAllItems();
        int restockedCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));
                int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL));

                if (onHand <= reorder) {
                    int target = Math.max(reorder + 25, 50);
                    dbHelper.setOnHandStock(id, target);
                    restockedCount++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (restockedCount > 0) {
            Toast.makeText(requireContext(), "Restocked " + restockedCount + " low items to safe capacity!", Toast.LENGTH_SHORT).show();
            refreshStockList();
        } else {
            Toast.makeText(requireContext(), "All items are currently well-stocked!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.getDefault());
        } else if (name.trim().length() >= 2) {
            return name.trim().substring(0, 2).toUpperCase(Locale.getDefault());
        } else {
            return name.trim().toUpperCase(Locale.getDefault());
        }
    }
}
