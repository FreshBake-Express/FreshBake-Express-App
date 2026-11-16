package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProductFormFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private int productId = -1;

    private DatabaseHelper dbHelper;
    private TextInputEditText etProductName, etProductPrice;
    private SwitchMaterial switchActive;
    private TextView tvActiveDescription;
    private MaterialButton btnSave, btnDelete;
    private MaterialToolbar toolbar;

    // Preserve existing category & SKU if editing, or empty strings if adding
    private String existingSku = "";
    private String existingCategory = "";
    private int existingReorderLevel = 15;

    public static ProductFormFragment newInstance(int productId) {
        ProductFormFragment fragment = new ProductFormFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_form, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        
        toolbar = view.findViewById(R.id.toolbarProductForm);
        etProductName = view.findViewById(R.id.etProductName);
        etProductPrice = view.findViewById(R.id.etProductPrice);
        switchActive = view.findViewById(R.id.switchActive);
        tvActiveDescription = view.findViewById(R.id.tvActiveDescription);
        btnSave = view.findViewById(R.id.btnSaveProduct);
        btnDelete = view.findViewById(R.id.btnDeleteProduct);

        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvActiveDescription.setText("Show this product in daily delivery loading, catalog, and customer billing.");
            } else {
                tvActiveDescription.setText("Hidden from delivery loading and billing until re-activated.");
            }
        });

        if (productId != -1) {
            loadProductData();
            btnDelete.setVisibility(View.VISIBLE);
            toolbar.setTitle("Edit Product");
            toolbar.setSubtitle("Update details & pricing");
        } else {
            toolbar.setTitle("Add Product");
            toolbar.setSubtitle("Product details & pricing");
        }

        btnSave.setOnClickListener(v -> saveProduct());
        btnDelete.setOnClickListener(v -> deleteProduct());

        return view;
    }

    private void loadProductData() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_ITEMS,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(productId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            etProductName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME)));
            double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
            etProductPrice.setText(cost % 1 == 0 ? String.valueOf((int)cost) : String.valueOf(cost));

            // Retain background data
            existingSku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
            existingCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
            existingReorderLevel = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL));

            boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1;
            switchActive.setChecked(isActive);
            cursor.close();
        }
    }

    private void saveProduct() {
        String name = etProductName.getText() != null ? etProductName.getText().toString().trim() : "";
        String priceStr = etProductPrice.getText() != null ? etProductPrice.getText().toString().trim() : "";
        int isActive = switchActive.isChecked() ? 1 : 0;

        if (name.isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etProductPrice.setError("Price is required");
            etProductPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etProductPrice.setError("Enter a valid price");
            etProductPrice.requestFocus();
            return;
        }

        long result;
        if (productId == -1) {
            // New product: default reorder level 15, empty sku/category
            result = dbHelper.addItem(name, price, "", "", 15, isActive);
        } else {
            // Update existing
            result = dbHelper.updateItem(productId, name, price, existingSku != null ? existingSku : "", existingCategory != null ? existingCategory : "", existingReorderLevel, isActive);
        }

        if (result != -1) {
            Toast.makeText(requireContext(), productId == -1 ? "Product created successfully" : "Product updated successfully", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Error saving product", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (dbHelper.toggleItemStatus(productId, 0)) {
            Toast.makeText(requireContext(), "Product marked inactive", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
