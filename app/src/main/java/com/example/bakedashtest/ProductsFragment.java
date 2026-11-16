package com.example.bakedashtest;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProductsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout llProductList;
    private LinearLayout llEmptyState;
    private TextView tvProductCount;
    private EditText etProductSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        llProductList = view.findViewById(R.id.llProductList);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        tvProductCount = view.findViewById(R.id.tvProductCount);
        etProductSearch = view.findViewById(R.id.etProductSearch);

        View fab = view.findViewById(R.id.fabAddProduct);
        if (fab != null) {
            fab.setOnClickListener(v -> openProductForm(-1));
        }

        etProductSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                refreshProductList(s != null ? s.toString().trim() : "");
            }
        });

        refreshProductList("");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (etProductSearch != null) {
            refreshProductList(etProductSearch.getText() != null ? etProductSearch.getText().toString().trim() : "");
        }
    }

    private void refreshProductList(String query) {
        if (llProductList == null) return;
        llProductList.removeAllViews();

        Cursor cursor;
        if (query == null || query.isEmpty()) {
            cursor = dbHelper.getAllItems();
        } else {
            cursor = dbHelper.searchItems(query);
        }

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                count++;
                final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                final String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                final double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                final int isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE));

                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_product_row, llProductList, false);

                TextView tvTag = itemView.findViewById(R.id.tvProductTag);
                TextView tvName = itemView.findViewById(R.id.productName);
                TextView tvCost = itemView.findViewById(R.id.itemCost);
                View btnToggle = itemView.findViewById(R.id.btnToggleActive);
                TextView tvStatusIcon = itemView.findViewById(R.id.tvStatusIcon);
                TextView tvStatusText = itemView.findViewById(R.id.tvStatusText);
                TextView tvRouteHint = itemView.findViewById(R.id.tvRouteVisibilityHint);
                View editIcon = itemView.findViewById(R.id.editIcon);

                if (tvTag != null) {
                    tvTag.setText(getInitials(name));
                }

                if (tvName != null) {
                    tvName.setText(name);
                }

                if (tvCost != null) {
                    String priceStr = (price % 1 == 0) ? String.valueOf((int) price) : String.format("%.2f", price);
                    tvCost.setText("Price: Rs. " + priceStr);
                }

                // Render active vs inactive state
                if (isActive == 1) {
                    if (btnToggle != null) btnToggle.setBackgroundResource(R.drawable.bg_stock_ok);
                    if (tvStatusIcon != null) tvStatusIcon.setTextColor(Color.parseColor("#10B981"));
                    if (tvStatusText != null) {
                        tvStatusText.setText("ACTIVE");
                        tvStatusText.setTextColor(Color.parseColor("#042F24"));
                    }
                    if (tvRouteHint != null) {
                        tvRouteHint.setText("✓ Active in Delivery & Billing");
                        tvRouteHint.setTextColor(Color.parseColor("#065F46"));
                    }
                } else {
                    if (btnToggle != null) btnToggle.setBackgroundResource(R.drawable.bg_status_inactive);
                    if (tvStatusIcon != null) tvStatusIcon.setTextColor(Color.parseColor("#94A3B8"));
                    if (tvStatusText != null) {
                        tvStatusText.setText("INACTIVE");
                        tvStatusText.setTextColor(Color.parseColor("#64748B"));
                    }
                    if (tvRouteHint != null) {
                        tvRouteHint.setText("✕ Hidden from Delivery & Billing");
                        tvRouteHint.setTextColor(Color.parseColor("#94A3B8"));
                    }
                }

                // Fast toggle listener directly on the status pill
                if (btnToggle != null) {
                    btnToggle.setOnClickListener(v -> {
                        int newStatus = (isActive == 1) ? 0 : 1;
                        dbHelper.toggleItemStatus(id, newStatus);
                        String msg = (newStatus == 1) ? (name + " is now ACTIVE for routes") : (name + " is now INACTIVE");
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        refreshProductList(etProductSearch != null ? etProductSearch.getText().toString().trim() : "");
                    });
                }

                // Edit click
                itemView.setOnClickListener(v -> openProductForm(id));
                if (editIcon != null) {
                    editIcon.setOnClickListener(v -> openProductForm(id));
                }

                llProductList.addView(itemView);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (tvProductCount != null) {
            tvProductCount.setText(count + (count == 1 ? " product" : " products"));
        }

        if (llEmptyState != null) {
            llEmptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "PR";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        } else if (name.trim().length() >= 2) {
            return name.trim().substring(0, 2).toUpperCase();
        } else {
            return name.trim().toUpperCase();
        }
    }

    private void openProductForm(int productId) {
        ProductFormFragment fragment = ProductFormFragment.newInstance(productId);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
