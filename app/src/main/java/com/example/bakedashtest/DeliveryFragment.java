package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DeliveryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etCustomerSearch;
    private LinearLayout llSearchResults;
    private View selectedCustomerCard;
    private TextView selectedAvatar, selectedCustomerName, selectedStoreName, selectedDueAmount;
    private int selectedCustomerId = -1;
    private LinearLayout llDeliveryItemsList;
    private View cardPaymentDetails, rlDeliveryTotal, llDeliveryButtons;
    private TextView tvDeliveryTotal, tvPrevArrears, tvTotalDue, tvRemainingBalance, tvFullPayHelper;
    private EditText etPaymentAmount;
    private ImageView btnClearSelection;
    private Map<Integer, Integer> deliveryQuantities = new HashMap<>();
    private Map<Integer, Double> itemPrices = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        etCustomerSearch = view.findViewById(R.id.etCustomerSearch);
        llSearchResults = view.findViewById(R.id.llSearchResults);
        selectedCustomerCard = view.findViewById(R.id.selectedCustomerCard);
        selectedAvatar = view.findViewById(R.id.selectedAvatar);
        selectedCustomerName = view.findViewById(R.id.selectedCustomerName);
        selectedStoreName = view.findViewById(R.id.selectedStoreName);
        selectedDueAmount = view.findViewById(R.id.selectedDueAmount);
        llDeliveryItemsList = view.findViewById(R.id.llDeliveryItemsList);
        cardPaymentDetails = view.findViewById(R.id.cardPaymentDetails);
        rlDeliveryTotal = view.findViewById(R.id.rlDeliveryTotal);
        llDeliveryButtons = view.findViewById(R.id.llDeliveryButtons);
        tvDeliveryTotal = view.findViewById(R.id.tvDeliveryTotal);
        tvPrevArrears = view.findViewById(R.id.tvPrevArrears);
        tvTotalDue = view.findViewById(R.id.tvTotalDue);
        tvRemainingBalance = view.findViewById(R.id.tvRemainingBalance);
        etPaymentAmount = view.findViewById(R.id.etPaymentAmount);
        tvFullPayHelper = view.findViewById(R.id.tvFullPayHelper);
        btnClearSelection = view.findViewById(R.id.btnClearSelection);

        setupClickListeners();
        setupSearch();
        setupPaymentInput();
        updateDeliveryDate(view);
        refreshDeliveryItems();

        if (getArguments() != null && getArguments().containsKey("selected_customer_id")) {
            int customerId = getArguments().getInt("selected_customer_id");
            loadSelectedCustomer(customerId);
        }

        return view;
    }

    private void loadSelectedCustomer(int customerId) {
        Cursor cursor = dbHelper.getCustomerById(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
            String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
            double oldBill = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));
            
            selectCustomer(customerId, firstName, lastName, storeName, oldBill);
            cursor.close();
        }
    }

    private void setupClickListeners() {
        if (tvFullPayHelper != null) {
            tvFullPayHelper.setOnClickListener(v -> {
                try {
                    String totalDueStr = tvTotalDue.getText().toString().replace("Rs. ", "").replace(",", "").trim();
                    double totalDue = Double.parseDouble(totalDueStr);
                    etPaymentAmount.setText(String.valueOf((int) totalDue));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error calculating full pay", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnClearSelection != null) {
            btnClearSelection.setOnClickListener(v -> resetDeliveryScreen());
        }

        View btnSaveAndPrint = getView() != null ? getView().findViewById(R.id.btnSaveAndPrintBill) : null;
        if (btnSaveAndPrint != null) {
            btnSaveAndPrint.setOnClickListener(v -> saveCurrentBill(true));
        }
    }

    private void updateDeliveryDate(View view) {
        TextView tvDate = view.findViewById(R.id.deliveryDateText);
        if (tvDate != null) {
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            tvDate.setText(getString(R.string.delivery_date_format, currentDate));
        }
    }

    private void refreshDeliveryItems() {
        if (llDeliveryItemsList == null) return;
        llDeliveryItemsList.removeAllViews();
        deliveryQuantities.clear();
        itemPrices.clear();

        Cursor cursor = dbHelper.getAllItems();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));

                itemPrices.put(id, cost);
                deliveryQuantities.put(id, 0);

                View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_delivery_row, llDeliveryItemsList, false);
                setupDeliveryItemRow(row, id, name, cost, onHand);
                llDeliveryItemsList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
        updateDeliveryTotals();
    }

    private void setupDeliveryItemRow(View row, int id, String name, double price, int stock) {
        TextView tvName = row.findViewById(R.id.itemName);
        TextView tvPriceDisplay = row.findViewById(R.id.itemPriceDisplay);
        EditText etQuantity = row.findViewById(R.id.quantity);
        TextView tvTotal = row.findViewById(R.id.itemTotal);
        View btnMinus = row.findViewById(R.id.btnMinus);
        View btnPlus = row.findViewById(R.id.btnPlus);
        View itemCardRoot = row.findViewById(R.id.itemCardRoot);

        tvName.setText(name);
        if (tvPriceDisplay != null) {
            tvPriceDisplay.setText("Price: " + (int) price + "/" + stock);
        }
        etQuantity.setText("0");
        tvTotal.setText("0");

        btnPlus.setOnClickListener(v -> {
            int qty = deliveryQuantities.get(id) + 1;
            deliveryQuantities.put(id, qty);
            etQuantity.setText(String.valueOf(qty));
            tvTotal.setText(String.valueOf((int) (qty * price)));
            updateItemCardStyle(itemCardRoot, qty);
            updateDeliveryTotals();
        });

        btnMinus.setOnClickListener(v -> {
            int qty = deliveryQuantities.get(id);
            if (qty > 0) {
                qty--;
                deliveryQuantities.put(id, qty);
                etQuantity.setText(String.valueOf(qty));
                tvTotal.setText(qty == 0 ? "0" : String.valueOf((int) (qty * price)));
                updateItemCardStyle(itemCardRoot, qty);
                updateDeliveryTotals();
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable resetRunnable = () -> {
            deliveryQuantities.put(id, 0);
            etQuantity.setText("0");
            tvTotal.setText("0");
            updateItemCardStyle(itemCardRoot, 0);
            updateDeliveryTotals();
            Toast.makeText(requireContext(), name + " reset to 0", Toast.LENGTH_SHORT).show();
        };

        btnMinus.setOnLongClickListener(v -> {
            handler.postDelayed(resetRunnable, 1000);
            return true;
        });

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                int qty = 0;
                if (s != null && s.length() > 0) {
                    try { qty = Integer.parseInt(s.toString()); } catch (NumberFormatException e) { qty = 0; }
                }
                if (deliveryQuantities.get(id) != qty) {
                    deliveryQuantities.put(id, qty);
                    tvTotal.setText(String.valueOf((int) (qty * price)));
                    updateItemCardStyle(itemCardRoot, qty);
                    updateDeliveryTotals();
                }
            }
        });
    }

    private void updateItemCardStyle(View cardRoot, int qty) {
        if (cardRoot == null) return;
        cardRoot.setBackgroundResource(qty > 0 ? R.drawable.bg_item_card_active : R.drawable.bg_item_card_normal);
    }

    private void updateDeliveryTotals() {
        double deliveryTotal = 0;
        for (Integer id : deliveryQuantities.keySet()) {
            deliveryTotal += deliveryQuantities.get(id) * itemPrices.get(id);
        }

        tvDeliveryTotal.setText("Rs. " + (int) deliveryTotal);

        if (cardPaymentDetails != null) cardPaymentDetails.setVisibility(View.VISIBLE);
        if (rlDeliveryTotal != null) rlDeliveryTotal.setVisibility(View.VISIBLE);
        if (llDeliveryButtons != null) llDeliveryButtons.setVisibility(View.VISIBLE);

        double arrears = 0;
        if (selectedCustomerId != -1) {
            try {
                String arrearsText = selectedDueAmount.getText().toString().replace("Rs. ", "").replace(",", "").trim();
                arrears = Double.parseDouble(arrearsText);
            } catch (Exception e) {
                arrears = 0;
            }
        }
        tvPrevArrears.setText("Rs. " + (int) arrears);
        
        double totalDue = deliveryTotal + arrears;
        tvTotalDue.setText("Rs. " + (int) totalDue);
        
        updateRemainingBalance();
    }

    private void setupPaymentInput() {
        if (etPaymentAmount == null) return;
        etPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateRemainingBalance(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateRemainingBalance() {
        double totalDue = 0;
        try {
            String dueStr = tvTotalDue.getText().toString().replace("Rs. ", "").replace(",", "").trim();
            totalDue = Double.parseDouble(dueStr);
        } catch (Exception e) { totalDue = 0; }

        double payment = 0;
        String paymentStr = etPaymentAmount.getText().toString();
        if (!paymentStr.isEmpty()) {
            try { payment = Double.parseDouble(paymentStr); } catch (Exception e) { payment = 0; }
        }

        double remaining = totalDue - payment;
        tvRemainingBalance.setText("Rs. " + (int) remaining);
    }

    private void setupSearch() {
        if (etCustomerSearch == null) return;
        etCustomerSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 1) performCustomerSearch(query);
                else llSearchResults.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void performCustomerSearch(String query) {
        llSearchResults.removeAllViews();
        Cursor cursor = dbHelper.searchCustomers(query);
        
        if (cursor != null && cursor.moveToFirst()) {
            llSearchResults.setVisibility(View.VISIBLE);
            int count = 0;
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                double oldBill = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));

                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_search_result, llSearchResults, false);
                ((TextView)itemView.findViewById(R.id.textName)).setText(firstName + " " + lastName);
                ((TextView)itemView.findViewById(R.id.textStore)).setText(storeName);
                
                itemView.setOnClickListener(v -> {
                    selectCustomer(id, firstName, lastName, storeName, oldBill);
                    llSearchResults.setVisibility(View.GONE);
                    etCustomerSearch.setText("");
                });

                llSearchResults.addView(itemView);
                count++;
                if (count >= 5) break;
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            llSearchResults.setVisibility(View.GONE);
        }
    }

    private void selectCustomer(int id, String firstName, String lastName, String storeName, double oldBill) {
        selectedCustomerId = id;
        selectedCustomerCard.setVisibility(View.VISIBLE);
        
        String initials = "";
        if (firstName.length() > 0) initials += firstName.charAt(0);
        if (lastName.length() > 0) initials += lastName.charAt(0);
        
        selectedAvatar.setText(initials.toUpperCase());
        selectedCustomerName.setText(firstName + " " + lastName);
        selectedStoreName.setText(storeName);
        selectedDueAmount.setText("Rs. " + (int) oldBill);
        
        updateDeliveryTotals();
    }

    private void saveCurrentBill(boolean autoPrint) {
        if (selectedCustomerId == -1) {
            Toast.makeText(requireContext(), "Please select a customer first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasValidItems = false;
        for (Integer qty : deliveryQuantities.values()) {
            if (qty > 0) { hasValidItems = true; break; }
        }

        if (!hasValidItems) {
            Toast.makeText(requireContext(), "Please add items with quantity greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double deliveryTotal = Double.parseDouble(tvDeliveryTotal.getText().toString().replace("Rs. ", "").replace(",", "").trim());
            double totalDue = Double.parseDouble(tvTotalDue.getText().toString().replace("Rs. ", "").replace(",", "").trim());
            String paymentStr = etPaymentAmount.getText().toString().trim();
            double paid = paymentStr.isEmpty() ? 0 : Double.parseDouble(paymentStr);
            double remaining = totalDue - paid;

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long orderId = dbHelper.saveOrder(selectedCustomerId, date, deliveryTotal, paid, remaining, deliveryQuantities, itemPrices);

            if (orderId != -1) {
                dbHelper.updateCustomerDue(selectedCustomerId, remaining);
                Toast.makeText(requireContext(), "Bill saved successfully!", Toast.LENGTH_SHORT).show();
                if (autoPrint) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) activity.printBill((int) orderId);
                }
                resetDeliveryScreen();
            } else {
                Toast.makeText(requireContext(), "Failed to save bill", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetDeliveryScreen() {
        selectedCustomerId = -1;
        selectedCustomerCard.setVisibility(View.GONE);
        etCustomerSearch.setText("");
        etPaymentAmount.setText("");
        refreshDeliveryItems();
    }
}
