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
import androidx.fragment.app.FragmentTransaction;

public class ProductsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout llProductList;
    private EditText etProductSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        llProductList = view.findViewById(R.id.llProductList);
        etProductSearch = view.findViewById(R.id.etProductSearch);

        view.findViewById(R.id.fabAddProduct).setOnClickListener(v -> openProductForm(-1));

        etProductSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                refreshProductList(s.toString());
            }
        });

        refreshProductList("");

        return view;
    }

    private void refreshProductList(String query) {
        if (llProductList == null) return;
        llProductList.removeAllViews();

        Cursor cursor;
        if (query.isEmpty()) {
            cursor = dbHelper.getAllItems();
        } else {
            cursor = dbHelper.searchItems(query);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));

                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_product_row, llProductList, false);
                
                TextView tvName = itemView.findViewById(R.id.productName);
                TextView tvCost = itemView.findViewById(R.id.itemCost);
                View editIcon = itemView.findViewById(R.id.editIcon);

                tvName.setText(name);
                tvCost.setText("Rs. " + (int)price);

                itemView.setOnClickListener(v -> openProductForm(id));
                if (editIcon != null) {
                    editIcon.setOnClickListener(v -> openProductForm(id));
                }

                llProductList.addView(itemView);
            } while (cursor.moveToNext());
            cursor.close();
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