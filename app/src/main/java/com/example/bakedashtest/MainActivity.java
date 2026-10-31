package com.example.bakedashtest;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Rect;
import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.print.PrintJob;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout llCustomerList, llItemsList;
    private TextView tvCustomerCount;
    private int editingCustomerId = -1;
    private int editingItemId = -1;
    private TextView tvAddCustomerTitle;
    private BottomNavigationView bottomNav;

    // Form fields
    private EditText etFirstName, etLastName, etStoreName, etContactNumber, etAddress, etOldBillAmount;
    private EditText etItemName, etOurPrice, etShopPrice;

    // Delivery Search and Selected Customer
    private EditText etCustomerSearch;
    private LinearLayout llSearchResults;
    private View selectedCustomerCard;
    private TextView selectedAvatar, selectedCustomerName, selectedStoreName, selectedDueAmount;
    private int selectedCustomerId = -1;
    private LinearLayout llDeliveryItemsList;
    private View cardPaymentDetails, rlDeliveryTotal, llDeliveryButtons;
    private TextView tvDeliveryTotal, tvPrevArrears, tvTotalDue, tvRemainingBalance;
    private EditText etPaymentAmount;
    private Map<Integer, Integer> deliveryQuantities = new HashMap<>();
    private Map<Integer, Double> itemPrices = new HashMap<>();

    private WebView mWebView;
    private List<PrintJob> printJobs = new ArrayList<>();

    // History Section Fields
    private TextView tvStatRevenue, tvStatBills, tvStatDue, tvStatDueStores;
    private TextView tabItemWise, tabCustomerWise;
    private TextView tvListHeader, tvListCount;
    private LinearLayout llHistoryList;
    private boolean isItemWise = false;

    // Customer Billing History Fields
    private View customerBillingHistoryLayout;
    private TextView tvBillingCustomerName, tvBillingStoreName, tvTotalCustomerSales, tvTotalCustomerDue;
    private EditText etSettleAmountHistory;
    private LinearLayout llTransactionList;
    private int currentBillingCustomerId = -1;

    // Bill Details Fields
    private View billDetailsLayout;
    private TextView tvBillDetailStoreName, tvBillDetailDateTime, tvBillDetailOrderId, tvBillDetailSubTotal, tvBillDetailDue;
    private EditText etBillDetailPaid;
    private LinearLayout llBillItemsList;
    private ImageView btnEditBill;
    private boolean isEditingBill = false;
    private int currentDetailOrderId = -1;
    private double currentBillArrears = 0;
    private Map<Integer, Integer> editOrderQuantities = new HashMap<>();
    private Map<Integer, Double> editOrderPrices = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(getResources().getColor(R.color.primary_green, getTheme())),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        llCustomerList = findViewById(R.id.llCustomerList);
        llItemsList = findViewById(R.id.llItemsList);
        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvAddCustomerTitle = findViewById(R.id.tvAddCustomerTitle);
        bottomNav = findViewById(R.id.bottomNavigation);

        View deliveryLayout = findViewById(R.id.deliveryLayout);
        View customersLayout = findViewById(R.id.customersLayout);
        View addCustomerLayout = findViewById(R.id.addCustomerLayout);
        View historyLayout = findViewById(R.id.historyLayout);
        customerBillingHistoryLayout = findViewById(R.id.customerBillingHistoryLayout);
        View itemsLayout = findViewById(R.id.itemsLayout);
        View addItemLayout = findViewById(R.id.addItemLayout);

        // Add Customer Form Fields
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etStoreName = findViewById(R.id.etStoreName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etAddress = findViewById(R.id.etAddress);
        etOldBillAmount = findViewById(R.id.etOldBillAmount);

        // Add Item Form Fields
        etItemName = findViewById(R.id.etItemName);
        etOurPrice = findViewById(R.id.etOurPrice);
        etShopPrice = findViewById(R.id.etShopPrice);

        // Delivery Section Fields
        etCustomerSearch = findViewById(R.id.etCustomerSearch);
        llSearchResults = findViewById(R.id.llSearchResults);
        selectedCustomerCard = findViewById(R.id.selectedCustomerCard);
        selectedAvatar = findViewById(R.id.selectedAvatar);
        selectedCustomerName = findViewById(R.id.selectedCustomerName);
        selectedStoreName = findViewById(R.id.selectedStoreName);
        selectedDueAmount = findViewById(R.id.selectedDueAmount);
        llDeliveryItemsList = findViewById(R.id.llDeliveryItemsList);
        cardPaymentDetails = findViewById(R.id.cardPaymentDetails);
        rlDeliveryTotal = findViewById(R.id.rlDeliveryTotal);
        llDeliveryButtons = findViewById(R.id.llDeliveryButtons);
        tvDeliveryTotal = findViewById(R.id.tvDeliveryTotal);
        tvPrevArrears = findViewById(R.id.tvPrevArrears);
        tvTotalDue = findViewById(R.id.tvTotalDue);
        tvRemainingBalance = findViewById(R.id.tvRemainingBalance);
        etPaymentAmount = findViewById(R.id.etPaymentAmount);

        // History Section Initializations
        tvStatRevenue = findViewById(R.id.tvStatRevenue);
        tvStatBills = findViewById(R.id.tvStatBills);
        tvStatDue = findViewById(R.id.tvStatDue);
        tvStatDueStores = findViewById(R.id.tvStatDueStores);
        tabItemWise = findViewById(R.id.tabItemWise);
        tabCustomerWise = findViewById(R.id.tabCustomerWise);
        tvListHeader = findViewById(R.id.tvListHeader);
        tvListCount = findViewById(R.id.tvListCount);
        llHistoryList = findViewById(R.id.llHistoryList);

        // Billing History Initializations
        tvBillingCustomerName = findViewById(R.id.tvBillingCustomerName);
        tvBillingStoreName = findViewById(R.id.tvBillingStoreName);
        tvTotalCustomerSales = findViewById(R.id.tvTotalCustomerSales);
        tvTotalCustomerDue = findViewById(R.id.tvTotalCustomerDue);
        etSettleAmountHistory = findViewById(R.id.etSettleAmountHistory);
        llTransactionList = findViewById(R.id.llTransactionList);
        View btnSettlePayment = findViewById(R.id.btnSettlePayment);
        if (btnSettlePayment != null) {
            btnSettlePayment.setOnClickListener(v -> showSettlePaymentDialog());
        }

        // Bill Details Initializations
        billDetailsLayout = findViewById(R.id.billDetailsLayout);
        tvBillDetailStoreName = findViewById(R.id.tvBillDetailStoreName);
        tvBillDetailDateTime = findViewById(R.id.tvBillDetailDateTime);
        tvBillDetailOrderId = findViewById(R.id.tvBillDetailOrderId);
        tvBillDetailSubTotal = findViewById(R.id.tvBillDetailSubTotal);
        etBillDetailPaid = findViewById(R.id.etBillDetailPaid);
        tvBillDetailDue = findViewById(R.id.tvBillDetailDue);
        llBillItemsList = findViewById(R.id.llBillItemsList);
        btnEditBill = findViewById(R.id.btnEditBill);

        etBillDetailPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditingBill) {
                    updateEditBillSummary();
                }
            }
        });

        btnEditBill.setOnClickListener(v -> {
            isEditingBill = !isEditingBill;
            if (isEditingBill) {
                btnEditBill.setColorFilter(getResources().getColor(R.color.revenue_text, getTheme()));
                etBillDetailPaid.setEnabled(true);
            } else {
                btnEditBill.setColorFilter(getResources().getColor(R.color.primary_green, getTheme()));
                etBillDetailPaid.setEnabled(false);
            }
            refreshBillDetailsItems();
        });

        findViewById(R.id.btnBackFromBillDetails).setOnClickListener(v -> {
            isEditingBill = false;
            btnEditBill.setColorFilter(getResources().getColor(R.color.primary_green, getTheme()));
            billDetailsLayout.setVisibility(View.GONE);
            customerBillingHistoryLayout.setVisibility(View.VISIBLE);
        });

        View.OnClickListener printAction = v -> {
            if (isEditingBill) {
                saveEditedBill();
            } else {
                if (currentDetailOrderId != -1) {
                    printBill(currentDetailOrderId);
                } else {
                    Toast.makeText(this, "No bill selected to print", Toast.LENGTH_SHORT).show();
                }
            }
        };
        findViewById(R.id.btnBottomPrint).setOnClickListener(printAction);

        findViewById(R.id.btnPrintBill).setOnClickListener(v -> {
            if (selectedCustomerId == -1) {
                Toast.makeText(this, "Please select a customer first", Toast.LENGTH_SHORT).show();
                return;
            }
            // For the delivery screen "Print Bill" button, we might want to print a preview 
            // or just save and print. Usually, printing happens after saving.
            // Let's make it trigger save if not already saved, or show a message.
            Toast.makeText(this, "Please save the bill first to print", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBackFromBillingHistory).setOnClickListener(v -> {
            customerBillingHistoryLayout.setVisibility(View.GONE);
            historyLayout.setVisibility(View.VISIBLE);
            updateHistoryTabs();
        });

        if (tabItemWise != null) {
            tabItemWise.setOnClickListener(v -> {
                isItemWise = true;
                updateHistoryTabs();
                refreshHistory();
            });
        }
        if (tabCustomerWise != null) {
            tabCustomerWise.setOnClickListener(v -> {
                isItemWise = false;
                updateHistoryTabs();
                refreshHistory();
            });
        }

        setupDeliverySearch();
        setupPaymentInput();

        updateDeliveryDate();
        updateHistoryTabs();

        // Initial load for delivery items
        refreshDeliveryItems();

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            addCustomerLayout.setVisibility(View.GONE);
            addItemLayout.setVisibility(View.GONE);
            deliveryLayout.setVisibility(View.GONE);
            customersLayout.setVisibility(View.GONE);
            historyLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.GONE);

            if (itemId == R.id.nav_delivery) {
                deliveryLayout.setVisibility(View.VISIBLE);
                refreshDeliveryItems();
                return true;
            } else if (itemId == R.id.nav_customers) {
                customersLayout.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_history) {
                historyLayout.setVisibility(View.VISIBLE);
                updateHistoryTabs();
                refreshHistory();
                return true;
            } else if (itemId == R.id.nav_items) {
                itemsLayout.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        findViewById(R.id.btnSaveBill).setOnClickListener(v -> saveCurrentBill());

        // Add Customer Flow
        findViewById(R.id.btnCreateCustomer).setOnClickListener(v -> {
            editingCustomerId = -1;
            if (tvAddCustomerTitle != null) tvAddCustomerTitle.setText("Add Customer");
            // Clear fields
            etFirstName.setText("");
            etLastName.setText("");
            etStoreName.setText("");
            etContactNumber.setText("");
            etAddress.setText("");
            etOldBillAmount.setText("");
            
            deliveryLayout.setVisibility(View.GONE);
            customersLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.GONE);
            addCustomerLayout.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.GONE);
        });

        findViewById(R.id.backButton).setOnClickListener(v -> {
            editingCustomerId = -1;
            addCustomerLayout.setVisibility(View.GONE);
            customersLayout.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String storeName = etStoreName.getText().toString().trim();
            String contact = etContactNumber.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String oldBillStr = etOldBillAmount.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || storeName.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (contact.length() != 10) {
                Toast.makeText(this, "Contact number must be 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            double oldBill = 0;
            if (!oldBillStr.isEmpty()) {
                try {
                    oldBill = Double.parseDouble(oldBillStr);
                } catch (NumberFormatException e) {
                    oldBill = 0;
                }
            }

            long result;
            if (editingCustomerId == -1) {
                result = dbHelper.addCustomer(firstName, lastName, storeName, contact, address, oldBill);
            } else {
                result = dbHelper.updateCustomer(editingCustomerId, firstName, lastName, storeName, contact, address, oldBill);
            }

            if (result != -1) {
                Toast.makeText(this, editingCustomerId == -1 ? "Customer Saved Successfully" : "Customer Updated Successfully", Toast.LENGTH_SHORT).show();
                // Clear fields
                etFirstName.setText("");
                etLastName.setText("");
                etStoreName.setText("");
                etContactNumber.setText("");
                etAddress.setText("");
                etOldBillAmount.setText("");
                editingCustomerId = -1;

                refreshCustomers();

                addCustomerLayout.setVisibility(View.GONE);
                customersLayout.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error saving customer", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Item Flow
        findViewById(R.id.btnAddItem).setOnClickListener(v -> {
            editingItemId = -1;
            etItemName.setText("");
            etOurPrice.setText("");
            etShopPrice.setText("");
            
            itemsLayout.setVisibility(View.GONE);
            addItemLayout.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.GONE);
        });

        findViewById(R.id.backButtonItems).setOnClickListener(v -> {
            addItemLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.saveItemButton).setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String ourPriceStr = etOurPrice.getText().toString().trim();
            String shopPriceStr = etShopPrice.getText().toString().trim();

            if (itemName.isEmpty() || ourPriceStr.isEmpty() || shopPriceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double ourPrice;
            double shopPrice;
            try {
                ourPrice = Double.parseDouble(ourPriceStr);
                shopPrice = Double.parseDouble(shopPriceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid prices", Toast.LENGTH_SHORT).show();
                return;
            }

            long result;
            if (editingItemId == -1) {
                result = dbHelper.addItem(itemName, ourPrice, shopPrice);
            } else {
                result = dbHelper.updateItem(editingItemId, itemName, ourPrice, shopPrice);
            }

            if (result != -1) {
                Toast.makeText(this, editingItemId == -1 ? "Item Saved Successfully" : "Item Updated Successfully", Toast.LENGTH_SHORT).show();
                
                // Clear the form
                etItemName.setText("");
                etOurPrice.setText("");
                etShopPrice.setText("");
                editingItemId = -1;

                // Refresh the items list
                refreshItems();

                // Dismiss screen and open Manage Items view
                addItemLayout.setVisibility(View.GONE);
                itemsLayout.setVisibility(View.VISIBLE);
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
            }
        });

        refreshItems();
        refreshCustomers();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateDeliveryDate() {
        TextView tvDate = findViewById(R.id.deliveryDateText);
        if (tvDate != null) {
            String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            tvDate.setText(getString(R.string.delivery_date_format, currentDate));
        }
    }

    private void refreshItems() {
        if (llItemsList == null) return;
        llItemsList.removeAllViews();

        Cursor cursor = dbHelper.getAllItems();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double ourPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OUR_PRICE));
                double shopPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SHOP_PRICE));

                View row = LayoutInflater.from(this).inflate(R.layout.item_product_row, llItemsList, false);
                setupProductRow(row, id, name, String.valueOf((int)ourPrice), String.valueOf((int)shopPrice));
                llItemsList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void setupProductRow(View row, int id, String name, String ourPrice, String shopPrice) {
        if (row == null) return;
        android.widget.TextView tvName = row.findViewById(R.id.productName);
        android.widget.TextView tvOurPrice = row.findViewById(R.id.ourPrice);
        android.widget.TextView tvShopPrice = row.findViewById(R.id.shopPrice);
        View editIcon = row.findViewById(R.id.editIcon);

        if (tvName != null) tvName.setText(name);
        if (tvOurPrice != null) tvOurPrice.setText(ourPrice);
        if (tvShopPrice != null) tvShopPrice.setText(shopPrice);

        if (editIcon != null) {
            editIcon.setOnClickListener(v -> {
                editingItemId = id;
                etItemName.setText(name);
                etOurPrice.setText(ourPrice);
                etShopPrice.setText(shopPrice);

                findViewById(R.id.itemsLayout).setVisibility(View.GONE);
                findViewById(R.id.addItemLayout).setVisibility(View.VISIBLE);
                if (bottomNav != null) bottomNav.setVisibility(View.GONE);
            });
        }
    }

    private void setupCustomerRow(View row, int id, String avatar, String firstName, String lastName, String store, String phone, String address, double amount, boolean isSelected) {
        if (row == null) return;
        
        android.widget.TextView tvAvatar = row.findViewById(R.id.customerAvatar);
        android.widget.TextView tvName = row.findViewById(R.id.customerName);
        android.widget.TextView tvStore = row.findViewById(R.id.storeName);
        android.widget.TextView tvPhone = row.findViewById(R.id.phoneNumber);
        android.widget.TextView tvAmount = row.findViewById(R.id.dueAmount);
        View arrowIcon = row.findViewById(R.id.arrowIcon);

        tvAvatar.setText(avatar);
        tvName.setText(firstName + " " + lastName);
        tvStore.setText(store);
        tvPhone.setText(phone);
        tvAmount.setText(String.valueOf((int) amount));

        if (isSelected) {
            row.setBackgroundResource(R.drawable.bg_customer_card);
        } else {
            row.setBackgroundResource(R.drawable.bg_customer_card_white);
        }

        if (arrowIcon != null) {
            arrowIcon.setOnClickListener(v -> {
                editingCustomerId = id;
                if (tvAddCustomerTitle != null) tvAddCustomerTitle.setText("Edit Customer");
                
                etFirstName.setText(firstName);
                etLastName.setText(lastName);
                etStoreName.setText(store);
                etContactNumber.setText(phone);
                etAddress.setText(address);
                etOldBillAmount.setText(String.valueOf((int) amount));

                findViewById(R.id.customersLayout).setVisibility(View.GONE);
                findViewById(R.id.addCustomerLayout).setVisibility(View.VISIBLE);
                if (bottomNav != null) bottomNav.setVisibility(View.GONE);
            });
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
                double ourPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OUR_PRICE));

                itemPrices.put(id, ourPrice);
                deliveryQuantities.put(id, 0);

                View row = LayoutInflater.from(this).inflate(R.layout.item_delivery_row, llDeliveryItemsList, false);
                setupDeliveryItemRow(row, id, name, ourPrice);
                llDeliveryItemsList.addView(row);

                // Add a divider
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(getResources().getColor(R.color.divider_grey, getTheme()));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider.getLayoutParams();
                params.setMargins(0, 32, 0, 32);
                llDeliveryItemsList.addView(divider);

            } while (cursor.moveToNext());
            cursor.close();
        }
        updateDeliveryTotals();
    }

    private void setupDeliveryItemRow(View row, int id, String name, double price) {
        TextView tvName = row.findViewById(R.id.itemName);
        TextView tvPriceDisplay = row.findViewById(R.id.itemPriceDisplay);
        EditText etQuantity = row.findViewById(R.id.quantity);
        TextView tvTotal = row.findViewById(R.id.itemTotal);
        View btnMinus = row.findViewById(R.id.btnMinus);
        View btnPlus = row.findViewById(R.id.btnPlus);

        tvName.setText(name);
        if (tvPriceDisplay != null) {
            tvPriceDisplay.setText("Price: " + (int) price);
        }
        etQuantity.setText("0");
        tvTotal.setText("0");

        btnPlus.setOnClickListener(v -> {
            int qty = deliveryQuantities.get(id) + 5;
            deliveryQuantities.put(id, qty);
            etQuantity.setText(String.valueOf(qty));
            tvTotal.setText(String.valueOf((int) (qty * price)));
            updateDeliveryTotals();
        });

        // 3-second long press reset logic
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable resetRunnable = () -> {
            deliveryQuantities.put(id, 0);
            etQuantity.setText("0");
            tvTotal.setText("0");
            updateDeliveryTotals();
            Toast.makeText(MainActivity.this, name + " reset to 0", Toast.LENGTH_SHORT).show();
        };

        btnMinus.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.postDelayed(resetRunnable, 3000);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(resetRunnable);
                    break;
            }
            return false;
        });

        btnMinus.setOnClickListener(v -> {
            int qty = deliveryQuantities.get(id);
            if (qty > 0) {
                qty--;
                deliveryQuantities.put(id, qty);
                etQuantity.setText(String.valueOf(qty));
                if (qty == 0) {
                    tvTotal.setText("0");
                } else {
                    tvTotal.setText(String.valueOf((int) (qty * price)));
                }
                updateDeliveryTotals();
            }
        });

        etQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                int qty = 0;
                if (s != null && s.length() > 0) {
                    try {
                        qty = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        qty = 0;
                    }
                }
                
                // Only update if value actually changed to avoid infinite loops if setText is called
                if (deliveryQuantities.get(id) != qty) {
                    deliveryQuantities.put(id, qty);
                    tvTotal.setText(String.valueOf((int) (qty * price)));
                    updateDeliveryTotals();
                }
            }
        });
    }

    private void updateDeliveryTotals() {
        double deliveryTotal = 0;
        for (Integer id : deliveryQuantities.keySet()) {
            deliveryTotal += deliveryQuantities.get(id) * itemPrices.get(id);
        }

        tvDeliveryTotal.setText(String.valueOf((int) deliveryTotal));

        // Keep payment details and buttons visible
        int visibility = View.VISIBLE;
        if (cardPaymentDetails != null) cardPaymentDetails.setVisibility(visibility);
        if (rlDeliveryTotal != null) rlDeliveryTotal.setVisibility(visibility);
        if (llDeliveryButtons != null) llDeliveryButtons.setVisibility(visibility);

        double arrears = 0;
        if (selectedCustomerId != -1) {
            // In a real app, we'd fetch the latest arrears from DB.
            // For now, we use the value displayed in the selected customer card.
            try {
                arrears = Double.parseDouble(selectedDueAmount.getText().toString());
            } catch (Exception e) {
                arrears = 0;
            }
        }
        tvPrevArrears.setText(String.valueOf((int) arrears));
        
        double totalDue = deliveryTotal + arrears;
        tvTotalDue.setText(String.valueOf((int) totalDue));
        
        updateRemainingBalance();
    }

    private void setupPaymentInput() {
        if (etPaymentAmount == null) return;
        etPaymentAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRemainingBalance();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void updateRemainingBalance() {
        double totalDue;
        try {
            totalDue = Double.parseDouble(tvTotalDue.getText().toString());
        } catch (Exception e) {
            totalDue = 0;
        }

        double arrears = 0;
        try {
            arrears = Double.parseDouble(tvPrevArrears.getText().toString());
        } catch (Exception e) {}

        double payment;
        String paymentStr = etPaymentAmount.getText().toString();
        if (paymentStr.isEmpty()) {
            payment = 0;
        } else {
            try {
                payment = Double.parseDouble(paymentStr);
            } catch (Exception e) {
                payment = 0;
            }
        }

        double remaining = totalDue - payment;
        tvRemainingBalance.setText(String.valueOf((int) remaining));
    }

    private void setupDeliverySearch() {
        if (etCustomerSearch == null) return;

        etCustomerSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 1) {
                    performCustomerSearch(query);
                } else {
                    llSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
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

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_search_result, llSearchResults, false);
                TextView textName = itemView.findViewById(R.id.textName);
                TextView textStore = itemView.findViewById(R.id.textStore);

                textName.setText(firstName + " " + lastName);
                textStore.setText(storeName);
                
                itemView.setOnClickListener(v -> {
                    selectCustomer(id, firstName, lastName, storeName, oldBill);
                    llSearchResults.setVisibility(View.GONE);
                    etCustomerSearch.setText("");
                });

                llSearchResults.addView(itemView);
                
                // Add a divider
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(Color.LTGRAY);
                llSearchResults.addView(divider);

                count++;
                if (count >= 5) break; // Limit results
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
        selectedDueAmount.setText(String.valueOf((int) oldBill));
        
        // Trigger billing update when customer is selected
        updateDeliveryTotals();
    }

    private void refreshCustomers() {
        if (llCustomerList == null) return;
        llCustomerList.removeAllViews();

        int count = dbHelper.getCustomersCount();
        tvCustomerCount.setText(getString(R.string.customer_count, count));

        Cursor cursor = dbHelper.getAllCustomers();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
                double oldBill = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));

                String initials = "";
                if (firstName.length() > 0) initials += firstName.charAt(0);
                if (lastName.length() > 0) initials += lastName.charAt(0);

                View row = LayoutInflater.from(this).inflate(R.layout.item_customer_row, llCustomerList, false);
                setupCustomerRow(row, id, initials.toUpperCase(), firstName, lastName, storeName, contact, address, oldBill, false);
                llCustomerList.addView(row);

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void saveCurrentBill() {
        if (selectedCustomerId == -1) {
            Toast.makeText(this, "Please select a customer first", Toast.LENGTH_SHORT).show();
            return;
        }

        double deliveryTotal = 0;
        boolean hasValidItems = false;
        for (Integer qty : deliveryQuantities.values()) {
            if (qty > 0) {
                hasValidItems = true;
                break;
            }
        }

        if (!hasValidItems) {
            Toast.makeText(this, "Please add items with quantity greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            deliveryTotal = Double.parseDouble(tvDeliveryTotal.getText().toString());
            double arrears = Double.parseDouble(tvPrevArrears.getText().toString());
            double totalDue = Double.parseDouble(tvTotalDue.getText().toString());
            String paymentStr = etPaymentAmount.getText().toString().trim();
            
            double paid;
            if (paymentStr.isEmpty()) {
                paid = 0;
            } else {
                paid = Double.parseDouble(paymentStr);
            }
            
            double remaining = totalDue - paid;

            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            long orderId = dbHelper.saveOrder(selectedCustomerId, date, deliveryTotal, paid, remaining, deliveryQuantities, itemPrices);

                if (orderId != -1) {
                    // Update customer's old_bill (arrears) in the database
                    dbHelper.updateCustomerDue(selectedCustomerId, remaining);

                    Toast.makeText(this, "Bill saved successfully!", Toast.LENGTH_SHORT).show();

                    // Ask for printing
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Print Bill")
                        .setMessage("Do you want to print this bill now?")
                        .setPositiveButton("Print", (dialog, which) -> {
                            printBill((int) orderId);
                            resetDeliveryScreen();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            resetDeliveryScreen();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Toast.makeText(this, "Failed to save bill", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private void resetDeliveryScreen() {
            selectedCustomerId = -1;
            selectedCustomerCard.setVisibility(View.GONE);
            etCustomerSearch.setText("");
            etPaymentAmount.setText("");
            refreshDeliveryItems();
            refreshCustomers();
        }

    private void updateHistoryTabs() {
        if (tabItemWise == null || tabCustomerWise == null) return;

        if (isItemWise) {
            tabItemWise.setBackgroundResource(R.drawable.bg_tab_selected);
            tabItemWise.setTextColor(getResources().getColor(R.color.revenue_text, getTheme()));
            tabItemWise.setTypeface(null, android.graphics.Typeface.BOLD);

            tabCustomerWise.setBackground(null);
            tabCustomerWise.setTextColor(getResources().getColor(R.color.text_grey, getTheme()));
            tabCustomerWise.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            tvListHeader.setText("TOP MOVING ITEMS");
        } else {
            tabCustomerWise.setBackgroundResource(R.drawable.bg_tab_selected);
            tabCustomerWise.setTextColor(getResources().getColor(R.color.revenue_text, getTheme()));
            tabCustomerWise.setTypeface(null, android.graphics.Typeface.BOLD);

            tabItemWise.setBackground(null);
            tabItemWise.setTextColor(getResources().getColor(R.color.text_grey, getTheme()));
            tabItemWise.setTypeface(null, android.graphics.Typeface.NORMAL);
            
            tvListHeader.setText("DELIVERED STORES");
        }
    }

    private void refreshHistory() {
        updateStats();
        llHistoryList.removeAllViews();
        if (isItemWise) {
            populateItemWiseList();
        } else {
            populateCustomerWiseList();
        }
    }

    private void updateStats() {
        Cursor cursor = dbHelper.getTodayStats();
        if (cursor != null && cursor.moveToFirst()) {
            double revenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total_revenue"));
            int bills = cursor.getInt(cursor.getColumnIndexOrThrow("total_bills"));
            double due = cursor.getDouble(cursor.getColumnIndexOrThrow("total_due"));
            int pendingStores = cursor.getInt(cursor.getColumnIndexOrThrow("pending_stores"));

            tvStatRevenue.setText(String.valueOf((int)revenue));
            tvStatBills.setText(String.valueOf(bills));
            tvStatDue.setText(String.valueOf((int)due));
            tvStatDueStores.setText(pendingStores + " stores pending");
            cursor.close();
        }
    }

    private void populateItemWiseList() {
        Cursor cursor = dbHelper.getItemWiseSales();
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OUR_PRICE));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_price"));

                View row = LayoutInflater.from(this).inflate(R.layout.item_history_item, llHistoryList, false);
                ((TextView)row.findViewById(R.id.tvItemName)).setText(name);
                ((TextView)row.findViewById(R.id.tvItemDetails)).setText((int)price + " / unit • " + qty + " units");
                ((TextView)row.findViewById(R.id.tvItemTotal)).setText(String.valueOf((int)total));
                
                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        tvListCount.setText(count + " items sold");
    }

    private void populateCustomerWiseList() {
        Cursor cursor = dbHelper.getCustomerWiseSales();
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT));
                double totalSales = cursor.getDouble(cursor.getColumnIndexOrThrow("total_sales"));
                double currentDue = cursor.getDouble(cursor.getColumnIndexOrThrow("current_due"));

                View row = LayoutInflater.from(this).inflate(R.layout.item_history_customer, llHistoryList, false);
                
                String initials = "";
                if (firstName.length() > 0) initials += firstName.charAt(0);
                if (lastName.length() > 0) initials += lastName.charAt(0);
                
                ((TextView)row.findViewById(R.id.tvCustomerAvatar)).setText(initials.toUpperCase());
                ((TextView)row.findViewById(R.id.tvCustomerName)).setText(firstName + " " + lastName);
                ((TextView)row.findViewById(R.id.tvStoreName)).setText(storeName);
                ((TextView)row.findViewById(R.id.tvCustomerPhone)).setText(contact);
                ((TextView)row.findViewById(R.id.tvCustomerTotal)).setText(String.valueOf((int)totalSales));
                
                TextView tvDue = row.findViewById(R.id.tvCustomerDue);
                if (currentDue > 0) {
                    tvDue.setText("Due " + (int)currentDue);
                    tvDue.setVisibility(View.VISIBLE);
                } else {
                    tvDue.setText("Settled");
                    tvDue.setBackgroundResource(R.drawable.bg_filter_unselected);
                    tvDue.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
                }

                int customerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                row.setOnClickListener(v -> showCustomerBillingHistory(customerId, firstName + " " + lastName, storeName, totalSales, currentDue));

                llHistoryList.addView(row);
                count++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        tvListCount.setText(count + " stores");
    }

    private void showCustomerBillingHistory(int customerId, String customerName, String storeName, double totalSales, double currentDue) {
        currentBillingCustomerId = customerId;
        tvBillingCustomerName.setText(customerName);
        tvBillingStoreName.setText(storeName);
        tvTotalCustomerSales.setText(String.valueOf((int)totalSales));
        tvTotalCustomerDue.setText(String.valueOf((int)currentDue));
        
        if (etSettleAmountHistory != null) {
            etSettleAmountHistory.setText(""); // Keep input field empty as requested
            etSettleAmountHistory.setSelectAllOnFocus(true);
        }

        llTransactionList.removeAllViews();
        Cursor cursor = dbHelper.getCustomerTransactions(customerId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_TOTAL));
                double paid = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_PAID));
                double due = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DUE));

                View row = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, llTransactionList, false);
                ((TextView)row.findViewById(R.id.tvTransactionDate)).setText(date);
                
                TextView tvTotal = row.findViewById(R.id.tvTransactionTotal);
                if (total == 0 && paid > 0) {
                    tvTotal.setText("Payment: " + (int)paid);
                    tvTotal.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
                } else {
                    tvTotal.setText(String.valueOf((int)total));
                    tvTotal.setTextColor(getResources().getColor(R.color.text_dark, getTheme()));
                }
                
                TextView tvStatus = row.findViewById(R.id.tvTransactionStatus);
                if (due > 0) {
                    tvStatus.setText("Due " + (int)due);
                    tvStatus.setTextColor(getResources().getColor(R.color.due_text, getTheme()));
                } else {
                    tvStatus.setText("Paid");
                    tvStatus.setTextColor(getResources().getColor(R.color.primary_green, getTheme()));
                }

                // Optionally count items
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

                row.setOnClickListener(v -> showBillDetails(orderId));

                llTransactionList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }

        findViewById(R.id.historyLayout).setVisibility(View.GONE);
        customerBillingHistoryLayout.setVisibility(View.VISIBLE);
    }

    private void showBillDetails(int orderId) {
        currentDetailOrderId = orderId;
        isEditingBill = false;
        btnEditBill.setColorFilter(getResources().getColor(R.color.primary_green, getTheme()));
        etBillDetailPaid.setEnabled(false);
        
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
            
            if (total == 0 && paid > 0) {
                // This is a payment-only record, don't show full bill details
                orderCursor.close();
                Toast.makeText(this, "Payment record: " + (int)paid, Toast.LENGTH_SHORT).show();
                return;
            }

            // Recalculate original arrears at the time of this bill
            currentBillArrears = due - total + paid;

            tvBillDetailStoreName.setText(tvBillingStoreName.getText()); // Use store name from context
            tvBillDetailDateTime.setText(date);
            tvBillDetailOrderId.setText("#" + orderId);
            tvBillDetailSubTotal.setText(String.valueOf((int)total));
            
            // Show the actual paid amount entered by the user
            etBillDetailPaid.setText(String.valueOf((int)paid));
            
            tvBillDetailDue.setText(String.valueOf((int)due));
            orderCursor.close();
        }

        refreshBillDetailsItems();

        customerBillingHistoryLayout.setVisibility(View.GONE);
        billDetailsLayout.setVisibility(View.VISIBLE);
        
        // Change print button text based on edit mode (initial state is NOT editing)
        Button btnBottomPrint = findViewById(R.id.btnBottomPrint);
        if (btnBottomPrint != null) {
            btnBottomPrint.setText("Print Bill");
        }
    }

    private void refreshBillDetailsItems() {
        if (llBillItemsList == null || currentDetailOrderId == -1) return;
        
        llBillItemsList.removeAllViews();
        editOrderQuantities.clear();
        editOrderPrices.clear();

        // Update Bottom Button Text
        Button btnBottomPrint = findViewById(R.id.btnBottomPrint);
        if (btnBottomPrint != null) {
            btnBottomPrint.setText(isEditingBill ? "Save Changes" : "Print Bill");
        }

        Cursor itemsCursor = dbHelper.getOrderItems(currentDetailOrderId);
        if (itemsCursor != null && itemsCursor.moveToFirst()) {
            do {
                int itemId = itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_ITEM_ID));
                String itemName = itemsCursor.getString(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int qty = itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_QUANTITY));
                double price = itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OI_PRICE));

                editOrderQuantities.put(itemId, qty);
                editOrderPrices.put(itemId, price);

                View itemView;
                if (isEditingBill) {
                    itemView = LayoutInflater.from(this).inflate(R.layout.item_delivery_row, llBillItemsList, false);
                    setupEditBillItemRow(itemView, itemId, itemName, price, qty);
                } else {
                    itemView = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, llBillItemsList, false);
                    TextView nameTv = itemView.findViewById(R.id.tvTransactionDate);
                    TextView detailTv = itemView.findViewById(R.id.tvTransactionItems);
                    TextView priceTv = itemView.findViewById(R.id.tvTransactionTotal);
                    TextView statusTv = itemView.findViewById(R.id.tvTransactionStatus);

                    nameTv.setText(itemName);
                    detailTv.setText(qty + " units");
                    priceTv.setText(String.valueOf((int)(qty * price)));
                    statusTv.setVisibility(View.GONE);
                }

                llBillItemsList.addView(itemView);
            } while (itemsCursor.moveToNext());
            itemsCursor.close();
        }
        
        if (isEditingBill) {
            updateEditBillSummary();
        }
    }

    private void setupEditBillItemRow(View row, int itemId, String name, double price, int initialQty) {
        TextView tvName = row.findViewById(R.id.itemName);
        TextView tvPrice = row.findViewById(R.id.itemPriceDisplay);
        TextView tvTotal = row.findViewById(R.id.itemTotal);
        EditText etQty = row.findViewById(R.id.quantity);
        TextView btnMinus = row.findViewById(R.id.btnMinus);
        TextView btnPlus = row.findViewById(R.id.btnPlus);

        tvName.setText(name);
        tvPrice.setText("Price: " + (int) price);
        etQty.setText(String.valueOf(initialQty));
        tvTotal.setText(String.valueOf((int) (initialQty * price)));

        btnMinus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQty.getText().toString());
            if (qty > 0) {
                qty--;
                etQty.setText(String.valueOf(qty));
                tvTotal.setText(String.valueOf((int) (qty * price)));
                editOrderQuantities.put(itemId, qty);
                updateEditBillSummary();
            }
        });

        btnPlus.setOnClickListener(v -> {
            int qty = Integer.parseInt(etQty.getText().toString());
            qty++;
            etQty.setText(String.valueOf(qty));
            tvTotal.setText(String.valueOf((int) (qty * price)));
            editOrderQuantities.put(itemId, qty);
            updateEditBillSummary();
        });

        etQty.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String val = s.toString();
                int qty = val.isEmpty() ? 0 : Integer.parseInt(val);
                tvTotal.setText(String.valueOf((int) (qty * price)));
                editOrderQuantities.put(itemId, qty);
                updateEditBillSummary();
            }
        });
    }

    private void updateEditBillSummary() {
        double newTotal = 0;
        for (Map.Entry<Integer, Integer> entry : editOrderQuantities.entrySet()) {
            newTotal += entry.getValue() * editOrderPrices.get(entry.getKey());
        }
        
        double paid;
        String paidText = etBillDetailPaid.getText().toString().trim();
        if (paidText.isEmpty()) {
            // Default logic: If no original arrears, assume fully paid. Otherwise 0.
            paid = (currentBillArrears > 0) ? 0 : newTotal;
        } else {
            try {
                paid = Double.parseDouble(paidText);
            } catch (NumberFormatException e) {
                paid = 0;
            }
        }

        double newDue = (newTotal - paid) + currentBillArrears;
        
        tvBillDetailSubTotal.setText(String.valueOf((int) newTotal));
        tvBillDetailDue.setText(String.valueOf((int) newDue));
    }

    private void printBill(int orderId) {
        Cursor orderCursor = dbHelper.getReadableDatabase().query("orders", null, "id=?", new String[]{String.valueOf(orderId)}, null, null, null);
        if (orderCursor != null && orderCursor.moveToFirst()) {
            int customerId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow("customer_id"));
            String date = orderCursor.getString(orderCursor.getColumnIndexOrThrow("order_date"));
            double total = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("total_amount"));
            double paid = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("paid_amount"));
            double due = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("due_amount"));
            orderCursor.close();

            Cursor customerCursor = dbHelper.getReadableDatabase().query("customers", null, "id=?", new String[]{String.valueOf(customerId)}, null, null, null);
            String storeName = "N/A";
            String customerName = "Customer";
            if (customerCursor != null && customerCursor.moveToFirst()) {
                storeName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("store_name"));
                customerName = customerCursor.getString(customerCursor.getColumnIndexOrThrow("first_name")) + " " +
                        customerCursor.getString(customerCursor.getColumnIndexOrThrow("last_name"));
                customerCursor.close();
            }

            StringBuilder html = new StringBuilder();
            html.append("<html><head><style>")
                .append("body { font-family: sans-serif; padding: 20px; }")
                .append("h1 { text-align: center; color: #2E7D32; margin-bottom: 5px; }")
                .append(".header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 10px; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
                .append("th { background-color: #f2f2f2; }")
                .append(".totals { margin-top: 30px; text-align: right; }")
                .append(".totals p { margin: 5px 0; font-size: 16px; }")
                .append(".footer { margin-top: 50px; text-align: center; font-size: 12px; color: #888; }")
                .append("</style></head><body>");

            html.append("<h1>BakeDash</h1>")
                .append("<div class='header'>")
                .append("<p><strong>Invoice #").append(orderId).append("</strong></p>")
                .append("<p>Date: ").append(date).append("</p>")
                .append("<p>Store: ").append(storeName).append("</p>")
                .append("<p>Customer: ").append(customerName).append("</p>")
                .append("</div>");

            html.append("<table><thead><tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr></thead><tbody>");

            Cursor itemsCursor = dbHelper.getOrderItems(orderId);
            if (itemsCursor != null) {
                while (itemsCursor.moveToNext()) {
                    String itemName = itemsCursor.getString(itemsCursor.getColumnIndexOrThrow("item_name"));
                    int qty = itemsCursor.getInt(itemsCursor.getColumnIndexOrThrow("quantity"));
                    double price = itemsCursor.getDouble(itemsCursor.getColumnIndexOrThrow("price_at_time"));
                    html.append("<tr>")
                        .append("<td>").append(itemName).append("</td>")
                        .append("<td>").append(qty).append("</td>")
                        .append("<td>").append((int)price).append("</td>")
                        .append("<td>").append((int)(qty * price)).append("</td>")
                        .append("</tr>");
                }
                itemsCursor.close();
            }

            html.append("</tbody></table>");

            html.append("<div class='totals'>")
                .append("<p><strong>Sub Total: </strong>").append((int)total).append("</p>")
                .append("<p><strong>Paid Amount: </strong>").append((int)paid).append("</p>")
                .append("<p><strong>Balance Due: </strong>").append((int)due).append("</p>")
                .append("</div>");

            html.append("<div class='footer'><p>Thank you for your business!</p></div>")
                .append("</body></html>");

            doWebViewPrint(html.toString(), "Bill_" + orderId);
        }
    }

    private void doWebViewPrint(String htmlContent, String jobNameSuffix) {
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob(view, jobNameSuffix);
                mWebView = null;
            }
        });

        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null);
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView, String jobNameSuffix) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " " + jobNameSuffix;
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        printJobs.add(printJob);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Optional: clean up mWebView if it was in the middle of a print job
        // though usually we want it to finish.
    }

    private void saveEditedBill() {
        try {
            boolean hasValidItems = false;
            for (Integer qty : editOrderQuantities.values()) {
                if (qty > 0) {
                    hasValidItems = true;
                    break;
                }
            }

            if (!hasValidItems) {
                Toast.makeText(this, "Bill must have at least one item with quantity > 0", Toast.LENGTH_SHORT).show();
                return;
            }

            double total = Double.parseDouble(tvBillDetailSubTotal.getText().toString());
            double due = Double.parseDouble(tvBillDetailDue.getText().toString());
            
            double paid;
            String paidText = etBillDetailPaid.getText().toString().trim();
            if (paidText.isEmpty()) {
                paid = 0;
            } else {
                paid = Double.parseDouble(paidText);
            }
            
            boolean success = dbHelper.updateOrder(currentDetailOrderId, total, paid, due, editOrderQuantities, editOrderPrices);
            if (success) {
                Toast.makeText(this, "Bill updated successfully!", Toast.LENGTH_SHORT).show();
                isEditingBill = false;
                btnEditBill.setColorFilter(getResources().getColor(R.color.primary_green, getTheme()));
                
                // Update the billing history view total/due display before going back or refreshing
                refreshHistory(); 
                
                // Fetch updated customer data to show correct totals
                Cursor customerCursor = dbHelper.getReadableDatabase().query(
                        DatabaseHelper.TABLE_CUSTOMERS,
                        new String[]{DatabaseHelper.COLUMN_FIRST_NAME, DatabaseHelper.COLUMN_LAST_NAME, DatabaseHelper.COLUMN_OLD_BILL},
                        DatabaseHelper.COLUMN_ID + "=?",
                        new String[]{String.valueOf(currentBillingCustomerId)},
                        null, null, null);
                
                double updatedDue = 0;
                String fullName = tvBillingCustomerName.getText().toString();
                if (customerCursor != null && customerCursor.moveToFirst()) {
                    updatedDue = customerCursor.getDouble(customerCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));
                    fullName = customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME)) + " " +
                               customerCursor.getString(customerCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                    customerCursor.close();
                }

                // Re-fetch customer transactions to show updated values in the list
                showCustomerBillingHistory(currentBillingCustomerId, 
                    fullName, 
                    tvBillingStoreName.getText().toString(), 
                    Double.parseDouble(tvTotalCustomerSales.getText().toString()), 
                    updatedDue);
                
                // Return to the bill details in read-only mode
                showBillDetails(currentDetailOrderId);
            } else {
                Toast.makeText(this, "Failed to update bill", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettlePaymentDialog() {
        if (currentBillingCustomerId == -1) return;

        double currentDue = 0;
        try {
            currentDue = Double.parseDouble(tvTotalCustomerDue.getText().toString());
        } catch (Exception e) {}

        if (currentDue <= 0) {
            Toast.makeText(this, "No due amount to settle", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etSettleAmountHistory.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount to pay", Toast.LENGTH_SHORT).show();
            return;
        }

        double finalCurrentDue = currentDue;
        try {
            double paymentAmount = Double.parseDouble(amountStr);
            if (paymentAmount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double newDue = finalCurrentDue - paymentAmount;

            // Confirm payment with a simple dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Payment")
                    .setMessage("Amount: " + (int)paymentAmount + "\nNew Due: " + (int)newDue)
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        long orderId = dbHelper.saveOrder(currentBillingCustomerId, date, 0, paymentAmount, newDue, new HashMap<>(), new HashMap<>());

                        if (orderId != -1) {
                            dbHelper.updateCustomerDue(currentBillingCustomerId, newDue);
                            Toast.makeText(this, "Payment recorded successfully", Toast.LENGTH_SHORT).show();

                            // Refresh the billing history view
                            showCustomerBillingHistory(currentBillingCustomerId,
                                    tvBillingCustomerName.getText().toString(),
                                    tvBillingStoreName.getText().toString(),
                                    Double.parseDouble(tvTotalCustomerSales.getText().toString()),
                                    newDue);
                        } else {
                            Toast.makeText(this, "Failed to record payment", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}