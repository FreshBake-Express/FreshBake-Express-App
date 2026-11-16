package com.example.bakedashtest;

import android.database.Cursor;
import android.graphics.Color;
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

import com.google.android.material.button.MaterialButton;

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
    private double currentCustomerArrears = 0.0;

    private LinearLayout llDeliveryItemsList;
    private View cardPaymentDetails, rlDeliveryTotal, llDeliveryButtons;
    private TextView tvDeliveryTotal, tvPrevArrears, tvTotalDue, tvRemainingBalance, tvFullPayHelper;
    private EditText etPaymentAmount;
    private ImageView btnClearSelection;
    private MaterialButton btnSaveAndPrintBill;

    private final Map<Integer, Integer> deliveryQuantities = new HashMap<>();
    private final Map<Integer, Double> itemPrices = new HashMap<>();
    private final Map<Integer, Integer> itemStocks = new HashMap<>();
    private final Map<Integer, String> itemNames = new HashMap<>();

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
        btnSaveAndPrintBill = view.findViewById(R.id.btnSaveAndPrintBill);

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
                double deliveryTotal = calculateDeliveryTotal();
                double netPayable = deliveryTotal + currentCustomerArrears;
                // If net payable is > 0, full pay is netPayable. If customer has surplus credit, pay is 0.
                double fullPay = Math.max(0, netPayable);
                etPaymentAmount.setText(String.valueOf((int) fullPay));
            });
        }

        if (btnClearSelection != null) {
            btnClearSelection.setOnClickListener(v -> resetDeliveryScreen());
        }

        if (btnSaveAndPrintBill != null) {
            btnSaveAndPrintBill.setOnClickListener(v -> saveCurrentBill(true));
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
        itemStocks.clear();
        itemNames.clear();

        // Prefer active items, fallback to all items if none marked active
        Cursor cursor = dbHelper.getActiveItems();
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            cursor = dbHelper.getAllItems();
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));

                itemPrices.put(id, cost);
                deliveryQuantities.put(id, 0);
                itemStocks.put(id, onHand);
                itemNames.put(id, name);

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
            String priceStr = (price % 1 == 0) ? String.valueOf((int) price) : String.format(Locale.getDefault(), "%.2f", price);
            tvPriceDisplay.setText("Price: Rs. " + priceStr + " / " + stock);
        }
        etQuantity.setText("0");
        tvTotal.setText("0");

        btnPlus.setOnClickListener(v -> {
            int currentQty = deliveryQuantities.containsKey(id) ? deliveryQuantities.get(id) : 0;
            if (currentQty >= stock) {
                Toast.makeText(requireContext(), "Only " + stock + " units available in stock for " + name, Toast.LENGTH_SHORT).show();
                return;
            }
            int qty = currentQty + 1;
            deliveryQuantities.put(id, qty);
            etQuantity.setText(String.valueOf(qty));
            tvTotal.setText(String.valueOf((int) (qty * price)));
            updateItemCardStyle(itemCardRoot, qty);
            updateDeliveryTotals();
        });

        btnMinus.setOnClickListener(v -> {
            int currentQty = deliveryQuantities.containsKey(id) ? deliveryQuantities.get(id) : 0;
            if (currentQty > 0) {
                int qty = currentQty - 1;
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

        etQuantity.setSelectAllOnFocus(true);
        etQuantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if ("0".equals(etQuantity.getText().toString().trim())) {
                    etQuantity.setText("");
                }
            } else {
                if (etQuantity.getText().toString().trim().isEmpty()) {
                    etQuantity.setText("0");
                }
            }
        });

        etQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 1 && s.toString().startsWith("0")) {
                    String stripped = s.toString().replaceFirst("^0+", "");
                    if (stripped.isEmpty()) stripped = "0";
                    etQuantity.setText(stripped);
                    etQuantity.setSelection(stripped.length());
                    return;
                }

                int qty = 0;
                if (s != null && s.length() > 0) {
                    try { qty = Integer.parseInt(s.toString()); } catch (NumberFormatException e) { qty = 0; }
                }

                if (qty > stock) {
                    qty = Math.max(0, stock);
                    Toast.makeText(requireContext(), "Only " + stock + " units available in stock for " + name, Toast.LENGTH_SHORT).show();
                    etQuantity.setText(String.valueOf(qty));
                    etQuantity.setSelection(String.valueOf(qty).length());
                    return;
                }

                Integer existingQty = deliveryQuantities.get(id);
                if (existingQty == null || existingQty != qty) {
                    deliveryQuantities.put(id, qty);
                    tvTotal.setText(qty == 0 ? "0" : String.valueOf((int) (qty * price)));
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

    private double calculateDeliveryTotal() {
        double deliveryTotal = 0;
        for (Map.Entry<Integer, Integer> entry : deliveryQuantities.entrySet()) {
            Double price = itemPrices.get(entry.getKey());
            if (price != null && entry.getValue() > 0) {
                deliveryTotal += entry.getValue() * price;
            }
        }
        return deliveryTotal;
    }

    private void updateDeliveryTotals() {
        double deliveryTotal = calculateDeliveryTotal();

        tvDeliveryTotal.setText("Rs. " + (int) deliveryTotal);

        if (cardPaymentDetails != null) cardPaymentDetails.setVisibility(View.VISIBLE);
        if (rlDeliveryTotal != null) rlDeliveryTotal.setVisibility(View.VISIBLE);
        if (llDeliveryButtons != null) llDeliveryButtons.setVisibility(View.VISIBLE);

        // Display Previous Arrears or Advance Credit
        if (selectedCustomerId != -1) {
            if (currentCustomerArrears > 0) {
                tvPrevArrears.setText("Rs. " + (int) currentCustomerArrears);
                tvPrevArrears.setTextColor(Color.WHITE);
            } else if (currentCustomerArrears < 0) {
                tvPrevArrears.setText("-Rs. " + (int) Math.abs(currentCustomerArrears) + " (Credit)");
                tvPrevArrears.setTextColor(Color.parseColor("#6EE7B7"));
            } else {
                tvPrevArrears.setText("Rs. 0");
                tvPrevArrears.setTextColor(Color.WHITE);
            }
        } else {
            tvPrevArrears.setText("Rs. 0");
            tvPrevArrears.setTextColor(Color.WHITE);
        }

        // Net Amount Due to Pay Today
        double netPayable = deliveryTotal + currentCustomerArrears;
        if (netPayable > 0) {
            tvTotalDue.setText("Rs. " + (int) netPayable);
            tvTotalDue.setTextColor(Color.WHITE);
        } else if (netPayable < 0) {
            tvTotalDue.setText("Rs. 0 (Covered by Credit)");
            tvTotalDue.setTextColor(Color.parseColor("#6EE7B7"));
        } else {
            tvTotalDue.setText("Rs. 0");
            tvTotalDue.setTextColor(Color.parseColor("#6EE7B7"));
        }

        updateRemainingBalance();
    }

    private void setupPaymentInput() {
        if (etPaymentAmount == null) return;
        etPaymentAmount.setSelectAllOnFocus(true);
        etPaymentAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if ("0".equals(etPaymentAmount.getText().toString().trim())) {
                    etPaymentAmount.setText("");
                }
            }
        });
        etPaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateRemainingBalance(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateRemainingBalance() {
        double deliveryTotal = calculateDeliveryTotal();
        double netPayable = deliveryTotal + currentCustomerArrears;

        double payment = 0;
        String paymentStr = etPaymentAmount.getText() != null ? etPaymentAmount.getText().toString().trim() : "";
        if (!paymentStr.isEmpty()) {
            try { payment = Double.parseDouble(paymentStr); } catch (Exception ignored) { payment = 0; }
        }

        double remaining = netPayable - payment;
        if (remaining > 0) {
            tvRemainingBalance.setText("Due: Rs. " + (int) remaining);
            tvRemainingBalance.setTextColor(Color.parseColor("#FDA4AF"));
        } else if (remaining < 0) {
            tvRemainingBalance.setText("Credit: Rs. " + (int) Math.abs(remaining));
            tvRemainingBalance.setTextColor(Color.parseColor("#6EE7B7"));
        } else {
            tvRemainingBalance.setText("Settled: Rs. 0");
            tvRemainingBalance.setTextColor(Color.parseColor("#6EE7B7"));
        }
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
                final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                final String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                final String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                final String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                final double oldBill = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));

                View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_search_result, llSearchResults, false);
                ((TextView) itemView.findViewById(R.id.textName)).setText(firstName + " " + lastName);
                ((TextView) itemView.findViewById(R.id.textStore)).setText(storeName);

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
        currentCustomerArrears = oldBill;
        selectedCustomerCard.setVisibility(View.VISIBLE);

        String initials = "";
        if (firstName != null && firstName.length() > 0) initials += firstName.charAt(0);
        if (lastName != null && lastName.length() > 0) initials += lastName.charAt(0);

        selectedAvatar.setText(initials.toUpperCase(Locale.getDefault()));
        selectedCustomerName.setText(firstName + " " + lastName);
        selectedStoreName.setText(storeName);

        if (oldBill > 0) {
            selectedDueAmount.setText("Due: Rs. " + (int) oldBill);
            selectedDueAmount.setTextColor(Color.parseColor("#E11D48"));
        } else if (oldBill < 0) {
            selectedDueAmount.setText("Credit: Rs. " + (int) Math.abs(oldBill));
            selectedDueAmount.setTextColor(Color.parseColor("#10B981"));
        } else {
            selectedDueAmount.setText("Settled: Rs. 0");
            selectedDueAmount.setTextColor(Color.parseColor("#10B981"));
        }

        updateDeliveryTotals();
    }

    private void saveCurrentBill(boolean autoPrint) {
        if (selectedCustomerId == -1) {
            Toast.makeText(requireContext(), "Please select a customer first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasValidItems = false;
        for (Integer qty : deliveryQuantities.values()) {
            if (qty != null && qty > 0) {
                hasValidItems = true;
                break;
            }
        }

        if (!hasValidItems) {
            Toast.makeText(requireContext(), "Please add items with quantity greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify no items exceed available on-hand stock
        for (Map.Entry<Integer, Integer> entry : deliveryQuantities.entrySet()) {
            int itemId = entry.getKey();
            int qty = entry.getValue() != null ? entry.getValue() : 0;
            if (qty > 0) {
                Integer avail = itemStocks.get(itemId);
                int stockLimit = avail != null ? avail : 0;
                if (qty > stockLimit) {
                    String itemName = itemNames.containsKey(itemId) ? itemNames.get(itemId) : "Item";
                    Toast.makeText(requireContext(), "Cannot complete bill: " + itemName + " has only " + stockLimit + " units in stock (selected: " + qty + ")", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        try {
            double deliveryTotal = calculateDeliveryTotal();
            String paymentStr = etPaymentAmount.getText() != null ? etPaymentAmount.getText().toString().trim() : "";
            double paid = paymentStr.isEmpty() ? 0 : Double.parseDouble(paymentStr);

            // Final cumulative customer due = (deliveryTotal + previousArrears) - paid
            double remaining = (deliveryTotal + currentCustomerArrears) - paid;

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long orderId = dbHelper.saveOrder(selectedCustomerId, date, deliveryTotal, paid, remaining, deliveryQuantities, itemPrices);

            if (orderId != -1) {
                dbHelper.updateCustomerDue(selectedCustomerId, remaining);
                Toast.makeText(requireContext(), "Bill saved successfully!", Toast.LENGTH_SHORT).show();
                if (autoPrint) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).printBill((int) orderId);
                    }
                }
                resetDeliveryScreen();
            } else {
                Toast.makeText(requireContext(), "Failed to save bill in database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving bill: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void resetDeliveryScreen() {
        selectedCustomerId = -1;
        currentCustomerArrears = 0.0;
        selectedCustomerCard.setVisibility(View.GONE);
        etCustomerSearch.setText("");
        etPaymentAmount.setText("");
        refreshDeliveryItems();
    }
}
