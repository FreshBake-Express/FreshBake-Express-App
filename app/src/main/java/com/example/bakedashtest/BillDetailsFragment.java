package com.example.bakedashtest;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class BillDetailsFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String ARG_STORE_NAME = "store_name";

    private int orderId;
    private String storeName;

    private DatabaseHelper dbHelper;
    private TextView tvBillDetailStoreName, tvBillDetailDateTime, tvBillDetailOrderId;
    private TextView tvBillDetailSubTotal, tvBillDetailDue;
    private EditText etBillDetailPaid;
    private LinearLayout llBillItemsList;
    private ImageView btnBackFromBillDetails, btnEditBill, btnPrintBill;
    private Button btnBottomPrint;

    private boolean isEditingBill = false;
    private double currentBillArrears = 0;
    private Map<Integer, Integer> editOrderQuantities = new HashMap<>();
    private Map<Integer, Double> editOrderPrices = new HashMap<>();

    public static BillDetailsFragment newInstance(int orderId, String storeName) {
        BillDetailsFragment fragment = new BillDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        args.putString(ARG_STORE_NAME, storeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt(ARG_ORDER_ID);
            storeName = getArguments().getString(ARG_STORE_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bill_details, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvBillDetailStoreName = view.findViewById(R.id.tvBillDetailStoreName);
        tvBillDetailDateTime = view.findViewById(R.id.tvBillDetailDateTime);
        tvBillDetailOrderId = view.findViewById(R.id.tvBillDetailOrderId);
        tvBillDetailSubTotal = view.findViewById(R.id.tvBillDetailSubTotal);
        tvBillDetailDue = view.findViewById(R.id.tvBillDetailDue);
        etBillDetailPaid = view.findViewById(R.id.etBillDetailPaid);
        llBillItemsList = view.findViewById(R.id.llBillItemsList);
        btnBackFromBillDetails = view.findViewById(R.id.btnBackFromBillDetails);
        btnEditBill = view.findViewById(R.id.btnEditBill);
        btnPrintBill = view.findViewById(R.id.btnPrintBill);
        btnBottomPrint = view.findViewById(R.id.btnBottomPrint);

        setupClickListeners();
        loadBillDetails();

        return view;
    }

    private void setupClickListeners() {
        btnBackFromBillDetails.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnEditBill.setOnClickListener(v -> toggleEditMode());
        
        btnBottomPrint.setOnClickListener(v -> {
            if (isEditingBill) {
                saveChanges();
            } else {
                printBill();
            }
        });
        
        btnPrintBill.setOnClickListener(v -> printBill());
    }

    private void loadBillDetails() {
        Cursor orderCursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_ORDERS,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(orderId)},
                null, null, null);

        if (orderCursor != null && orderCursor.moveToFirst()) {
            double total = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_TOTAL));
            double due = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DUE));
            double paid = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_PAID));
            String date = orderCursor.getString(orderCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE));
            
            currentBillArrears = due - total + paid;

            tvBillDetailStoreName.setText(storeName);
            tvBillDetailDateTime.setText(date);
            tvBillDetailOrderId.setText("#" + orderId);
            tvBillDetailSubTotal.setText(String.valueOf((int)total));
            etBillDetailPaid.setText(String.valueOf((int)paid));
            tvBillDetailDue.setText(String.valueOf((int)due));
            orderCursor.close();
        }

        refreshBillDetailsItems();
    }

    private void toggleEditMode() {
        isEditingBill = !isEditingBill;
        btnEditBill.setColorFilter(getResources().getColor(isEditingBill ? R.color.due_text : R.color.primary_green, null));
        etBillDetailPaid.setEnabled(isEditingBill);
        refreshBillDetailsItems();
    }

    private void refreshBillDetailsItems() {
        llBillItemsList.removeAllViews();
        editOrderQuantities.clear();
        editOrderPrices.clear();

        btnBottomPrint.setText(isEditingBill ? "Save Changes" : "Print Bill");

        Cursor itemsCursor = dbHelper.getOrderItems(orderId);
        if (itemsCursor != null && itemsCursor.moveToFirst()) {
            do {
                int itemId = itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_ITEM_ID));
                String itemName = itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int qty = itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_QUANTITY));
                double price = itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_PRICE));

                editOrderQuantities.put(itemId, qty);
                editOrderPrices.put(itemId, price);

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_bill_detail_row, llBillItemsList, false);
                ((TextView)row.findViewById(R.id.tvItemName)).setText(itemName);
                ((TextView)row.findViewById(R.id.tvItemPrice)).setText(String.valueOf((int)price));
                
                TextView tvQty = row.findViewById(R.id.tvItemQty);
                tvQty.setText(String.valueOf(qty));
                
                View btnMinus = row.findViewById(R.id.btnMinus);
                View btnPlus = row.findViewById(R.id.btnPlus);
                
                if (isEditingBill) {
                    btnMinus.setVisibility(View.VISIBLE);
                    btnPlus.setVisibility(View.VISIBLE);
                    
                    btnMinus.setOnClickListener(v -> {
                        int current = editOrderQuantities.get(itemId);
                        if (current > 0) {
                            editOrderQuantities.put(itemId, current - 1);
                            tvQty.setText(String.valueOf(current - 1));
                            recalculateTotals();
                        }
                    });
                    
                    btnPlus.setOnClickListener(v -> {
                        int current = editOrderQuantities.get(itemId);
                        editOrderQuantities.put(itemId, current + 1);
                        tvQty.setText(String.valueOf(current + 1));
                        recalculateTotals();
                    });
                } else {
                    btnMinus.setVisibility(View.GONE);
                    btnPlus.setVisibility(View.GONE);
                }

                llBillItemsList.addView(row);
            } while (itemsCursor.moveToNext());
            itemsCursor.close();
        }

        Cursor returnsCursor = dbHelper.getOrderReturns(orderId);
        if (returnsCursor != null && returnsCursor.moveToFirst()) {
            do {
                String rName = returnsCursor.getString(returnsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int rQty = returnsCursor.getInt(returnsCursor.getColumnIndexOrThrow("quantity"));
                double rPrice = returnsCursor.getDouble(returnsCursor.getColumnIndexOrThrow("price_at_time"));
                String rReason = returnsCursor.getString(returnsCursor.getColumnIndexOrThrow("reason"));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_bill_detail_row, llBillItemsList, false);
                TextView tvItemName = row.findViewById(R.id.tvItemName);
                if (tvItemName != null) {
                    tvItemName.setText("↩ " + rName + " (" + rReason + ")");
                    tvItemName.setTextColor(Color.parseColor("#BE123C"));
                }

                TextView tvPrice = row.findViewById(R.id.tvItemPrice);
                if (tvPrice != null) {
                    tvPrice.setText("-Rs. " + (int)(rQty * rPrice));
                    tvPrice.setTextColor(Color.parseColor("#BE123C"));
                }

                TextView tvQty = row.findViewById(R.id.tvItemQty);
                if (tvQty != null) {
                    tvQty.setText("-" + rQty);
                    tvQty.setTextColor(Color.parseColor("#BE123C"));
                }

                View btnMinus = row.findViewById(R.id.btnMinus);
                View btnPlus = row.findViewById(R.id.btnPlus);
                if (btnMinus != null) btnMinus.setVisibility(View.GONE);
                if (btnPlus != null) btnPlus.setVisibility(View.GONE);

                llBillItemsList.addView(row);
            } while (returnsCursor.moveToNext());
            returnsCursor.close();
        }
    }

    private void recalculateTotals() {
        double newTotal = 0;
        for (Map.Entry<Integer, Integer> entry : editOrderQuantities.entrySet()) {
            newTotal += entry.getValue() * editOrderPrices.get(entry.getKey());
        }
        tvBillDetailSubTotal.setText(String.valueOf((int)newTotal));
        
        double paid = 0;
        try {
            paid = Double.parseDouble(etBillDetailPaid.getText().toString());
        } catch (Exception ignored) {}
        
        tvBillDetailDue.setText(String.valueOf((int)(newTotal + currentBillArrears - paid)));
    }

    private void saveChanges() {
        double newTotal = 0;
        for (Map.Entry<Integer, Integer> entry : editOrderQuantities.entrySet()) {
            newTotal += entry.getValue() * editOrderPrices.get(entry.getKey());
        }
        
        double paid = 0;
        try {
            paid = Double.parseDouble(etBillDetailPaid.getText().toString());
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid paid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double newDue = newTotal + currentBillArrears - paid;

        boolean success = dbHelper.updateOrder(orderId, newTotal, paid, newDue, editOrderQuantities, editOrderPrices);
        if (success) {
            Toast.makeText(requireContext(), "Bill updated", Toast.LENGTH_SHORT).show();
            toggleEditMode();
            loadBillDetails();
        } else {
            Toast.makeText(requireContext(), "Failed to update bill", Toast.LENGTH_SHORT).show();
        }
    }

    private void printBill() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.printBill(orderId);
        }
    }
}
