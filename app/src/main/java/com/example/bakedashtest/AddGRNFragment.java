package com.example.bakedashtest;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddGRNFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etSupplier, etGRNDate, etDeliveryNote;
    private TextView tvGRNNumber, tvGRNItemsCount, tvTotalItemsBottom, tvTotalQtyBottom, tvGRNTotalAmount;
    private LinearLayout llGRNItemsList;
    private Button btnSaveGRN;

    private Map<Integer, Integer> grnQuantities = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_grn, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        etSupplier = view.findViewById(R.id.etSupplier);
        etGRNDate = view.findViewById(R.id.etGRNDate);
        etDeliveryNote = view.findViewById(R.id.etDeliveryNote);
        tvGRNNumber = view.findViewById(R.id.tvGRNNumber);
        tvGRNItemsCount = view.findViewById(R.id.tvGRNItemsCount);
        tvTotalItemsBottom = view.findViewById(R.id.tvTotalItemsBottom);
        tvTotalQtyBottom = view.findViewById(R.id.tvTotalQtyBottom);
        tvGRNTotalAmount = view.findViewById(R.id.tvGRNTotalAmount);
        llGRNItemsList = view.findViewById(R.id.llGRNItemsList);
        btnSaveGRN = view.findViewById(R.id.btnSaveGRN);
        TextView btnBackToStock = view.findViewById(R.id.btnBackToStock);

        setupDatePickers();
        setupClickListeners(btnBackToStock);
        refreshGRNItems();

        return view;
    }

    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etGRNDate.setText(sdf.format(calendar.getTime()));

        etGRNDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                etGRNDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupClickListeners(TextView btnBackToStock) {
        btnBackToStock.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        View btnCancelGRN = getView() != null ? getView().findViewById(R.id.btnCancelGRN) : null;
        if (btnCancelGRN == null && etSupplier.getRootView() != null) {
            btnCancelGRN = etSupplier.getRootView().findViewById(R.id.btnCancelGRN);
        }
        
        if (btnCancelGRN != null) {
            btnCancelGRN.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        btnSaveGRN.setOnClickListener(v -> saveGRN());
    }

    private void refreshGRNItems() {
        llGRNItemsList.removeAllViews();
        grnQuantities.clear();
        Cursor cursor = dbHelper.getActiveItems();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_grn_input, llGRNItemsList, false);
                ((TextView)row.findViewById(R.id.tvItemName)).setText(name);
                
                EditText etQty = row.findViewById(R.id.etReceivedQty);
                etQty.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            int qty = Integer.parseInt(s.toString());
                            grnQuantities.put(id, qty);
                        } catch (Exception e) {
                            grnQuantities.remove(id);
                        }
                        updateGRNSummary();
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });

                llGRNItemsList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void updateGRNSummary() {
        int totalItems = 0;
        int totalQty = 0;
        double totalAmount = 0;

        for (Map.Entry<Integer, Integer> entry : grnQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                totalItems++;
                totalQty += entry.getValue();
                // We don't have per-item total cost in GRN yet, but could calculate if needed
            }
        }

        tvGRNItemsCount.setText(totalItems + " items");
        tvTotalItemsBottom.setText(String.valueOf(totalItems));
        tvTotalQtyBottom.setText(String.valueOf(totalQty));
    }

    private void saveGRN() {
        if (grnQuantities.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one item quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = false;
        for (Map.Entry<Integer, Integer> entry : grnQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                dbHelper.addStock(entry.getKey(), entry.getValue());
                dbHelper.addGRNLog(entry.getKey(), entry.getValue());
                success = true;
            }
        }

        if (success) {
            Toast.makeText(requireContext(), "GRN Saved successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }
}
