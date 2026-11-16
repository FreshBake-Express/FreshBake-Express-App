package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CustomerBillingHistoryFragment extends Fragment {

    private static final String ARG_CUSTOMER_ID = "customer_id";
    private static final String ARG_CUSTOMER_NAME = "customer_name";
    private static final String ARG_STORE_NAME = "store_name";
    private static final String ARG_TOTAL_SALES = "total_sales";
    private static final String ARG_CURRENT_DUE = "current_due";

    private int customerId;
    private String customerName;
    private String storeName;
    private double totalSales;
    private double currentDue;

    private DatabaseHelper dbHelper;
    private TextView tvBillingCustomerName, tvBillingStoreName;
    private TextView tvTotalCustomerSales, tvTotalCustomerDue;
    private EditText etSettleAmountHistory;
    private LinearLayout llTransactionList;
    private ImageView btnBackFromBillingHistory;

    public static CustomerBillingHistoryFragment newInstance(int customerId, String customerName, String storeName, double totalSales, double currentDue) {
        CustomerBillingHistoryFragment fragment = new CustomerBillingHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CUSTOMER_ID, customerId);
        args.putString(ARG_CUSTOMER_NAME, customerName);
        args.putString(ARG_STORE_NAME, storeName);
        args.putDouble(ARG_TOTAL_SALES, totalSales);
        args.putDouble(ARG_CURRENT_DUE, currentDue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerId = getArguments().getInt(ARG_CUSTOMER_ID);
            customerName = getArguments().getString(ARG_CUSTOMER_NAME);
            storeName = getArguments().getString(ARG_STORE_NAME);
            totalSales = getArguments().getDouble(ARG_TOTAL_SALES);
            currentDue = getArguments().getDouble(ARG_CURRENT_DUE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_billing_history, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvBillingCustomerName = view.findViewById(R.id.tvBillingCustomerName);
        tvBillingStoreName = view.findViewById(R.id.tvBillingStoreName);
        tvTotalCustomerSales = view.findViewById(R.id.tvTotalCustomerSales);
        tvTotalCustomerDue = view.findViewById(R.id.tvTotalCustomerDue);
        etSettleAmountHistory = view.findViewById(R.id.etSettleAmountHistory);
        llTransactionList = view.findViewById(R.id.llTransactionList);
        btnBackFromBillingHistory = view.findViewById(R.id.btnBackFromBillingHistory);

        tvBillingCustomerName.setText(customerName);
        tvBillingStoreName.setText(storeName);
        tvTotalCustomerSales.setText("Rs. " + (int)totalSales);
        if (currentDue < 0) {
            tvTotalCustomerDue.setText("Credit: Rs. " + (int)Math.abs(currentDue));
            tvTotalCustomerDue.setTextColor(android.graphics.Color.parseColor("#10B981"));
        } else {
            tvTotalCustomerDue.setText("Rs. " + (int)currentDue);
        }

        etSettleAmountHistory.setSelectAllOnFocus(true);

        btnBackFromBillingHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.btnSettlePayment).setOnClickListener(v -> handleSettlePayment());

        refreshTransactions();

        return view;
    }

    private void handleSettlePayment() {
        String amountStr = etSettleAmountHistory.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Enter amount to settle", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) return;

        // Perform settlement logic (similar to what was in MainActivity)
        long result = dbHelper.saveOrder(customerId, "Payment Settlement", 0, amount, currentDue - amount, new java.util.HashMap<>(), new java.util.HashMap<>());
        if (result != -1) {
            dbHelper.updateCustomerDue(customerId, currentDue - amount);
            Toast.makeText(requireContext(), "Payment settled", Toast.LENGTH_SHORT).show();
            currentDue -= amount;
            if (currentDue < 0) {
                tvTotalCustomerDue.setText("Credit: Rs. " + (int)Math.abs(currentDue));
                tvTotalCustomerDue.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                tvTotalCustomerDue.setText("Rs. " + (int)currentDue);
            }
            etSettleAmountHistory.setText("");
            refreshTransactions();
        }
    }

    private void refreshTransactions() {
        llTransactionList.removeAllViews();
        Cursor cursor = dbHelper.getCustomerTransactions(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_TOTAL));
                double paid = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_PAID));
                double due = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DUE));

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_transaction_row, llTransactionList, false);
                ((TextView)row.findViewById(R.id.tvTransactionDate)).setText(date);
                
                TextView tvTotal = row.findViewById(R.id.tvTransactionTotal);
                if (total == 0 && paid > 0) {
                    tvTotal.setText("Payment: " + (int)paid);
                    tvTotal.setTextColor(getResources().getColor(R.color.primary_green, null));
                } else {
                    tvTotal.setText(String.valueOf((int)total));
                    tvTotal.setTextColor(getResources().getColor(R.color.text_dark, null));
                }
                
                TextView tvStatus = row.findViewById(R.id.tvTransactionStatus);
                if (due > 0) {
                    tvStatus.setText("Due " + (int)due);
                    tvStatus.setTextColor(getResources().getColor(R.color.due_text, null));
                } else {
                    tvStatus.setText("Paid");
                    tvStatus.setTextColor(getResources().getColor(R.color.primary_green, null));
                }

                Cursor itemsCursor = dbHelper.getOrderItems(orderId);
                int itemCount = 0;
                int unitCount = 0;
                if (itemsCursor != null && itemsCursor.moveToFirst()) {
                    do {
                        itemCount++;
                        unitCount += itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_QUANTITY));
                    } while (itemsCursor.moveToNext());
                    itemsCursor.close();
                }
                ((TextView)row.findViewById(R.id.tvTransactionItems)).setText(itemCount + " items • " + unitCount + " units");

                row.setOnClickListener(v -> {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.showBillDetailsFragment(orderId, storeName);
                    }
                });

                llTransactionList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
