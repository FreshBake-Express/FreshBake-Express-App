package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private TextInputEditText etProductName, etProductPrice, etProductSKU, etProductCategory, etReorderLevel;
    private SwitchMaterial switchActive;
    private MaterialButton btnSave, btnDelete;
    private MaterialToolbar toolbar;

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
        etProductSKU = view.findViewById(R.id.etProductSKU);
        etProductCategory = view.findViewById(R.id.etProductCategory);
        etReorderLevel = view.findViewById(R.id.etReorderLevel);
        switchActive = view.findViewById(R.id.switchActive);
        btnSave = view.findViewById(R.id.btnSaveProduct);
        btnDelete = view.findViewById(R.id.btnDeleteProduct);

        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (productId != -1) {
            loadProductData();
            btnDelete.setVisibility(View.VISIBLE);
            toolbar.setTitle("Edit Product");
        } else {
            toolbar.setTitle("New Product");
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
            etProductPrice.setText(String.valueOf((int)cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST))));
            etProductSKU.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU)));
            etProductCategory.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
            etReorderLevel.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL))));
            switchActive.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ACTIVE)) == 1);
            cursor.close();
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String sku = etProductSKU.getText().toString().trim();
        String category = etProductCategory.getText().toString().trim();
        String reorderStr = etReorderLevel.getText().toString().trim();
        int isActive = switchActive.isChecked() ? 1 : 0;

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int reorderLevel = reorderStr.isEmpty() ? 15 : Integer.parseInt(reorderStr);

        long result;
        if (productId == -1) {
            result = dbHelper.addItem(name, price, sku, category, reorderLevel, isActive);
        } else {
            result = dbHelper.updateItem(productId, name, price, sku, category, reorderLevel, isActive);
        }

        if (result != -1) {
            Toast.makeText(requireContext(), "Product saved", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Error saving product", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (dbHelper.toggleItemStatus(productId, 0)) {
            Toast.makeText(requireContext(), "Product deactivated", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }
}