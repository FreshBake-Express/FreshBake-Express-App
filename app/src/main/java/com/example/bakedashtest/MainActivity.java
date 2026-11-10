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
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.activity.OnBackPressedCallback;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Rect;
import android.content.Context;
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
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Form fields
    private EditText etFirstName, etLastName, etStoreName, etContactNumber, etAddress, etOldBillAmount;
    private EditText etItemName, etCost, etSKU, etCategory, etReorderLevel;
    private EditText etCustomerListSearch, etHistorySearch, etStockSearch;

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
    private View deliveryLayout, customersLayout, addCustomerLayout, historyLayout, itemsLayout, addItemLayout, settingsLayout, stockManagementLayout, addGRNLayout;
    private LinearLayout llStockList, llGRNItemsList;
    private EditText etSupplier, etGRNDate, etDeliveryNote;
    private TextView tvGRNNumber, tvGRNItemsCount, tvTotalItemsBottom, tvTotalQtyBottom, tvGRNTotalAmount;
    private boolean isItemWise = false;
    private int stockFilterMode = 0; // 0: All, 1: In Stock, 2: Low Stock, 3: Out of Stock
    private String stockCategoryFilter = "All categories";

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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        // Header Menu Buttons
        View headerView = navigationView.getHeaderView(0);
        headerView.findViewById(R.id.btnCloseDrawer).setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        deliveryLayout = findViewById(R.id.deliveryLayout);
        customersLayout = findViewById(R.id.customersLayout);
        addCustomerLayout = findViewById(R.id.addCustomerLayout);
        historyLayout = findViewById(R.id.historyLayout);
        customerBillingHistoryLayout = findViewById(R.id.customerBillingHistoryLayout);
        itemsLayout = findViewById(R.id.itemsLayout);
        stockManagementLayout = findViewById(R.id.stockManagementLayout);
        addItemLayout = findViewById(R.id.addItemLayout);
        settingsLayout = findViewById(R.id.settingsLayout);
        billDetailsLayout = findViewById(R.id.billDetailsLayout);
        addGRNLayout = findViewById(R.id.addGRNLayout);

        llStockList = findViewById(R.id.llStockList);

        if (stockManagementLayout != null) {
            stockManagementLayout.findViewById(R.id.btnAddGRN).setOnClickListener(v -> showAddGRNScreen());
        }

        if (addGRNLayout != null) {
            etSupplier = addGRNLayout.findViewById(R.id.etSupplier);
            etGRNDate = addGRNLayout.findViewById(R.id.etGRNDate);
            etDeliveryNote = addGRNLayout.findViewById(R.id.etDeliveryNote);
            tvGRNNumber = addGRNLayout.findViewById(R.id.tvGRNNumber);
            tvGRNItemsCount = addGRNLayout.findViewById(R.id.tvGRNItemsCount);
            tvTotalItemsBottom = addGRNLayout.findViewById(R.id.tvTotalItemsBottom);
            tvTotalQtyBottom = addGRNLayout.findViewById(R.id.tvTotalQtyBottom);
            tvGRNTotalAmount = addGRNLayout.findViewById(R.id.tvGRNTotalAmount);
            llGRNItemsList = addGRNLayout.findViewById(R.id.llGRNItemsList);

            addGRNLayout.findViewById(R.id.btnBackToStock).setOnClickListener(v -> showStockManagement());
            addGRNLayout.findViewById(R.id.btnCancelGRN).setOnClickListener(v -> showStockManagement());
            addGRNLayout.findViewById(R.id.btnSaveDraftGRN).setOnClickListener(v -> showStockManagement());
            addGRNLayout.findViewById(R.id.btnSaveGRN).setOnClickListener(v -> saveGRN());

            etGRNDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));
        }

        // Add Customer Form Fields
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etStoreName = findViewById(R.id.etStoreName);
        etContactNumber = findViewById(R.id.etContactNumber);
        etAddress = findViewById(R.id.etAddress);
        etOldBillAmount = findViewById(R.id.etOldBillAmount);

        // Add Item Form Fields
        etItemName = findViewById(R.id.etItemName);
        etCost = findViewById(R.id.etCost);
        etSKU = findViewById(R.id.etSKU);
        etCategory = findViewById(R.id.etCategory);
        etReorderLevel = findViewById(R.id.etReorderLevel);

        etCustomerListSearch = findViewById(R.id.etCustomerListSearch);
        etHistorySearch = findViewById(R.id.etHistorySearch);
        etStockSearch = findViewById(R.id.etStockSearch);

        if (etCustomerListSearch != null) {
            etCustomerListSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    refreshCustomers();
                }
            });
        }

        if (etStockSearch != null) {
            etStockSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    refreshStockManagement();
                }
            });
        }

        if (stockManagementLayout != null) {
            stockManagementLayout.findViewById(R.id.tvFilterAll).setOnClickListener(v -> { stockFilterMode = 0; refreshStockManagement(); });
            stockManagementLayout.findViewById(R.id.tvFilterInStock).setOnClickListener(v -> { stockFilterMode = 1; refreshStockManagement(); });
            stockManagementLayout.findViewById(R.id.tvFilterLowStock).setOnClickListener(v -> { stockFilterMode = 2; refreshStockManagement(); });
            stockManagementLayout.findViewById(R.id.tvFilterOutOfStock).setOnClickListener(v -> { stockFilterMode = 3; refreshStockManagement(); });
            stockManagementLayout.findViewById(R.id.tvCategoryFilter).setOnClickListener(v -> showCategoryFilterDialog());
        }

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
            if (isEditingBill) {
                // If we were editing and click edit again, it means cancel/save? 
                // In current logic it toggles. If toggling OFF, we should probably save or just revert.
                // The user said "Print Bill" should save and print. 
                // If they click "Save Changes" (which is btnBottomPrint when editing), it should just save.
                saveEditedBill(false);
            } else {
                isEditingBill = true;
                btnEditBill.setColorFilter(getResources().getColor(R.color.revenue_text, getTheme()));
                etBillDetailPaid.setEnabled(true);
                refreshBillDetailsItems();
            }
        });

        findViewById(R.id.btnBackFromBillDetails).setOnClickListener(v -> navigateToDelivery());

        View.OnClickListener printAction = v -> {
            if (isEditingBill) {
                saveEditedBill(true);
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
            saveCurrentBill(true);
        });

        findViewById(R.id.btnBackFromBillingHistory).setOnClickListener(v -> navigateToDelivery());

        // Settings Section Listeners
        findViewById(R.id.btnSettingManageItems).setOnClickListener(v -> {
            settingsLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.btnSettingLanguage).setOnClickListener(v -> {
            Toast.makeText(this, "Language settings coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBackFromItems).setOnClickListener(v -> navigateToDelivery());

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

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            hideAllLayouts();

            if (itemId == R.id.nav_delivery) {
                deliveryLayout.setVisibility(View.VISIBLE);
                refreshDeliveryItems();
            } else if (itemId == R.id.nav_customers) {
                customersLayout.setVisibility(View.VISIBLE);
                refreshCustomers();
            } else if (itemId == R.id.nav_history) {
                historyLayout.setVisibility(View.VISIBLE);
                updateHistoryTabs();
                refreshHistory();
            } else if (itemId == R.id.nav_items) {
                itemsLayout.setVisibility(View.VISIBLE);
                refreshItems();
            } else if (itemId == R.id.nav_product) {
                showStockManagement();
            } else if (itemId == R.id.nav_settings) {
                settingsLayout.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.nav_logout) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> finish())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Set default selection
        navigationView.setCheckedItem(R.id.nav_delivery);

        findViewById(R.id.btnSaveBill).setOnClickListener(v -> saveCurrentBill(false));

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
        });

        findViewById(R.id.backButton).setOnClickListener(v -> navigateToDelivery());

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
                // bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error saving customer", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Item Flow
        findViewById(R.id.btnAddItem).setOnClickListener(v -> {
            editingItemId = -1;
            etItemName.setText("");
            etCost.setText("");
            etSKU.setText("");
            etCategory.setText("");
            etReorderLevel.setText("");
            
            itemsLayout.setVisibility(View.GONE);
            addItemLayout.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.backButtonItems).setOnClickListener(v -> navigateToDelivery());

        findViewById(R.id.saveItemButton).setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String costStr = etCost.getText().toString().trim();
            String sku = etSKU.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String reorderStr = etReorderLevel.getText().toString().trim();

            if (itemName.isEmpty() || costStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double cost;
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid cost", Toast.LENGTH_SHORT).show();
                return;
            }

            int reorderLevel = 15;
            if (!reorderStr.isEmpty()) {
                try {
                    reorderLevel = Integer.parseInt(reorderStr);
                } catch (NumberFormatException e) {}
            }

            long result;
            if (editingItemId == -1) {
                result = dbHelper.addItem(itemName, cost, sku, category, reorderLevel);
            } else {
                result = dbHelper.updateItem(editingItemId, itemName, cost, sku, category, reorderLevel);
            }

            if (result != -1) {
                Toast.makeText(this, editingItemId == -1 ? "Item Saved Successfully" : "Item Updated Successfully", Toast.LENGTH_SHORT).show();
                
                // Clear the form
                etItemName.setText("");
                etCost.setText("");
                etSKU.setText("");
                etCategory.setText("");
                etReorderLevel.setText("");
                editingItemId = -1;

                // Refresh the items list
                refreshItems();

                // Dismiss screen and open Manage Items view
                addItemLayout.setVisibility(View.GONE);
                itemsLayout.setVisibility(View.VISIBLE);
                // bottomNav.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
            }
        });

        refreshItems();
        refreshCustomers();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (deliveryLayout.getVisibility() != View.VISIBLE) {
                    navigateToDelivery();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
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
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));

                View row = LayoutInflater.from(this).inflate(R.layout.item_product_row, llItemsList, false);
                setupProductRow(row, id, name, String.valueOf((int)cost));
                llItemsList.addView(row);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void setupProductRow(View row, int id, String name, String cost) {
        if (row == null) return;
        android.widget.TextView tvName = row.findViewById(R.id.productName);
        android.widget.TextView tvCost = row.findViewById(R.id.itemCost);
        View editIcon = row.findViewById(R.id.editIcon);

        if (tvName != null) tvName.setText(name);
        if (tvCost != null) tvCost.setText(cost);

        if (editIcon != null) {
            editIcon.setOnClickListener(v -> {
                editingItemId = id;
                Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_ITEMS, null, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    etItemName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME)));
                    etCost.setText(String.valueOf((int)cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST))));
                    etSKU.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU)));
                    etCategory.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
                    etReorderLevel.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL))));
                    cursor.close();
                }

                findViewById(R.id.itemsLayout).setVisibility(View.GONE);
                findViewById(R.id.addItemLayout).setVisibility(View.VISIBLE);
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
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));

                itemPrices.put(id, cost);
                deliveryQuantities.put(id, 0);

                View row = LayoutInflater.from(this).inflate(R.layout.item_delivery_row, llDeliveryItemsList, false);
                setupDeliveryItemRow(row, id, name, cost);
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

        String query = "";
        if (etCustomerListSearch != null) {
            query = etCustomerListSearch.getText().toString().trim();
        }

        int count = dbHelper.getCustomersCount();
        tvCustomerCount.setText(getString(R.string.customer_count, count));

        Cursor cursor;
        if (query.isEmpty()) {
            cursor = dbHelper.getAllCustomers();
        } else {
            cursor = dbHelper.searchCustomers(query);
        }

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

    private void saveCurrentBill(boolean autoPrint) {
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

                    if (autoPrint) {
                        printBill((int) orderId);
                        resetDeliveryScreen();
                    } else {
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
                    }
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
        
        String query = "";
        if (etHistorySearch != null) {
            query = etHistorySearch.getText().toString().trim();
        }
        
        if (isItemWise) {
            populateItemWiseList(query);
        } else {
            populateCustomerWiseList(query);
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

    private void populateItemWiseList(String query) {
        Cursor cursor = dbHelper.getItemWiseSales(query);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));
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

    private void populateCustomerWiseList(String query) {
        Cursor cursor = dbHelper.getCustomerWiseSales(query);
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

    private void saveEditedBill(boolean andPrint) {
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

                if (andPrint) {
                    printBill(currentDetailOrderId);
                }
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
            etSettleAmountHistory.requestFocus();
            return;
        }

        try {
            double paymentAmount = Double.parseDouble(amountStr);
            if (paymentAmount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double newDue = currentDue - paymentAmount;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Payment")
                    .setMessage("Amount: " + (int)paymentAmount + "\nNew Due: " + (int)newDue)
                    .setPositiveButton("Settle & Print", (dialog, which) -> {
                        performSettlement(paymentAmount, newDue, true);
                    })
                    .setNeutralButton("Settle Only", (dialog, which) -> {
                        performSettlement(paymentAmount, newDue, false);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSettlement(double paymentAmount, double newDue, boolean shouldPrint) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        long orderId = dbHelper.saveOrder(currentBillingCustomerId, date, 0, paymentAmount, newDue, new HashMap<>(), new HashMap<>());

        if (orderId != -1) {
            dbHelper.updateCustomerDue(currentBillingCustomerId, newDue);
            Toast.makeText(this, "Payment recorded successfully", Toast.LENGTH_SHORT).show();

            if (shouldPrint) {
                printBill((int) orderId);
            }

            // Refresh the billing history view
            showCustomerBillingHistory(currentBillingCustomerId,
                    tvBillingCustomerName.getText().toString(),
                    tvBillingStoreName.getText().toString(),
                    Double.parseDouble(tvTotalCustomerSales.getText().toString()),
                    newDue);

            refreshHistory(); // Refresh the main history list too
        } else {
            Toast.makeText(this, "Failed to record payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToDelivery() {
        isEditingBill = false;
        editingCustomerId = -1;
        editingItemId = -1;
        
        if (btnEditBill != null) {
            btnEditBill.setColorFilter(getResources().getColor(R.color.primary_green, getTheme()));
        }

        hideAllLayouts();
        deliveryLayout.setVisibility(View.VISIBLE);
        
        navigationView.setCheckedItem(R.id.nav_delivery);
        
        refreshDeliveryItems();
    }

    private void hideAllLayouts() {
        if (addCustomerLayout != null) addCustomerLayout.setVisibility(View.GONE);
        if (addItemLayout != null) addItemLayout.setVisibility(View.GONE);
        if (deliveryLayout != null) deliveryLayout.setVisibility(View.GONE);
        if (customersLayout != null) customersLayout.setVisibility(View.GONE);
        if (historyLayout != null) historyLayout.setVisibility(View.GONE);
        if (itemsLayout != null) itemsLayout.setVisibility(View.GONE);
        if (stockManagementLayout != null) stockManagementLayout.setVisibility(View.GONE);
        if (addGRNLayout != null) addGRNLayout.setVisibility(View.GONE);
        if (customerBillingHistoryLayout != null) customerBillingHistoryLayout.setVisibility(View.GONE);
        if (billDetailsLayout != null) billDetailsLayout.setVisibility(View.GONE);
        if (settingsLayout != null) settingsLayout.setVisibility(View.GONE);
    }

    private void showStockManagement() {
        hideAllLayouts();
        stockManagementLayout.setVisibility(View.VISIBLE);
        refreshStockManagement();
    }

    private void showAddGRNScreen() {
        hideAllLayouts();
        addGRNLayout.setVisibility(View.VISIBLE);
        
        // Initialize GRN screen
        tvGRNNumber.setText("GRN-" + new SimpleDateFormat("yyyy-MMdd", Locale.getDefault()).format(new Date()) + " · New receipt");
        llGRNItemsList.removeAllViews();
        
        Cursor cursor = dbHelper.getAllItems();
        int itemCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST));

                View row = LayoutInflater.from(this).inflate(R.layout.item_grn_row, llGRNItemsList, false);
                ((TextView)row.findViewById(R.id.tvItemName)).setText(name);
                ((TextView)row.findViewById(R.id.tvItemSKU)).setText((sku != null && !sku.isEmpty() ? sku : "") + " · Each");
                
                EditText etQty = row.findViewById(R.id.etQty);
                EditText etPrice = row.findViewById(R.id.etPrice);
                TextView tvRowTotal = row.findViewById(R.id.tvRowTotal);

                etPrice.setText(String.format(Locale.US, "%.2f", cost));
                
                TextWatcher watcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        calculateGRNTotal();
                        try {
                            String qs = etQty.getText().toString();
                            String ps = etPrice.getText().toString();
                            int q = qs.isEmpty() ? 0 : Integer.parseInt(qs);
                            double p = ps.isEmpty() ? 0 : Double.parseDouble(ps);
                            tvRowTotal.setText(String.format(Locale.US, "%.2f", q * p));
                        } catch (Exception e) {
                            tvRowTotal.setText("0.00");
                        }
                    }
                };
                etQty.addTextChangedListener(watcher);
                etPrice.addTextChangedListener(watcher);

                row.setTag(itemId);
                llGRNItemsList.addView(row);
                itemCount++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        tvGRNItemsCount.setText(itemCount + " items");
        tvTotalItemsBottom.setText(itemCount + " items");
        calculateGRNTotal();
    }

    private void calculateGRNTotal() {
        int totalQty = 0;
        double totalAmount = 0;
        for (int i = 0; i < llGRNItemsList.getChildCount(); i++) {
            View row = llGRNItemsList.getChildAt(i);
            EditText etQty = row.findViewById(R.id.etQty);
            EditText etPrice = row.findViewById(R.id.etPrice);
            try {
                String qs = etQty.getText().toString();
                String ps = etPrice.getText().toString();
                int q = qs.isEmpty() ? 0 : Integer.parseInt(qs);
                double p = ps.isEmpty() ? 0 : Double.parseDouble(ps);
                totalQty += q;
                totalAmount += (q * p);
            } catch (Exception ignored) {}
        }
        tvTotalQtyBottom.setText("Total qty: " + totalQty);
        tvGRNTotalAmount.setText(String.format(Locale.US, "%.2f", totalAmount));
    }

    private void saveGRN() {
        boolean anyUpdated = false;
        for (int i = 0; i < llGRNItemsList.getChildCount(); i++) {
            View row = llGRNItemsList.getChildAt(i);
            int itemId = (int) row.getTag();
            EditText etQty = row.findViewById(R.id.etQty);
            String qtyStr = etQty.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                try {
                    int receivedQty = Integer.parseInt(qtyStr);
                    if (receivedQty > 0) {
                        dbHelper.addStock(itemId, receivedQty);
                        dbHelper.addGRNLog(itemId, receivedQty);
                        anyUpdated = true;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (anyUpdated) {
            Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show();
            showStockManagement();
        } else {
            Toast.makeText(this, "Please enter quantities to receive", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCategoryFilterDialog() {
        final String[] categories = {"All categories", "Bread", "Pastry", "Cakes", "Biscuits", "Others"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setItems(categories, (dialog, which) -> {
            stockCategoryFilter = categories[which];
            refreshStockManagement();
        });
        builder.show();
    }

    private void refreshStockManagement() {
        if (llStockList == null) return;
        llStockList.removeAllViews();

        int totalItems = 0;
        int availableUnitsTotal = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;
        int inStockCount = 0;

        String query = "";
        if (etStockSearch != null) {
            query = etStockSearch.getText().toString().trim();
        }

        Cursor cursor = dbHelper.getAllItems();
        if (cursor != null && cursor.moveToFirst()) {
            totalItems = cursor.getCount();
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int onHand = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ON_HAND));
                int reserved = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESERVED));
                int reorderLevel = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL));
                String sku = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                
                int available = onHand - reserved;
                availableUnitsTotal += available;
                
                boolean isLow = (available > 0 && available < reorderLevel);
                boolean isOut = (available <= 0);
                boolean isIn = (available >= reorderLevel);

                if (isOut) outOfStockCount++;
                else if (isLow) lowStockCount++;
                else inStockCount++;

                // Apply Search Filter
                boolean matchesSearch = query.isEmpty() || 
                                       name.toLowerCase().contains(query.toLowerCase()) || 
                                       (sku != null && sku.toLowerCase().contains(query.toLowerCase()));
                
                // Apply Tab Filter
                boolean matchesFilter = (stockFilterMode == 0) || 
                                        (stockFilterMode == 1 && isIn) || 
                                        (stockFilterMode == 2 && isLow) || 
                                        (stockFilterMode == 3 && isOut);

                // Apply Category Filter
                boolean matchesCategory = stockCategoryFilter.equals("All categories") || 
                                         (category != null && category.equals(stockCategoryFilter));

                if (matchesSearch && matchesFilter && matchesCategory) {
                    String skuDisplay = (sku != null ? sku : "") + (sku != null && !sku.isEmpty() ? " • " : "") + (category != null ? category : "Bakery");
                    
                    String status = "In stock";
                    int statusBg = R.color.stock_in_bg;
                    int statusText = R.color.stock_in_text;

                    if (isOut) {
                        status = "Out of stock";
                        statusBg = R.color.stock_out_bg;
                        statusText = R.color.stock_out_text;
                    } else if (isLow) {
                        status = "Low stock";
                        statusBg = R.color.stock_low_bg;
                        statusText = R.color.stock_low_text;
                    }

                    View row = LayoutInflater.from(this).inflate(R.layout.item_stock_row, llStockList, false);
                    ((TextView)row.findViewById(R.id.tvStockItemName)).setText(name);
                    ((TextView)row.findViewById(R.id.tvStockSKU)).setText(skuDisplay);
                    ((TextView)row.findViewById(R.id.tvAvailableUnits)).setText(String.valueOf(available));
                    ((TextView)row.findViewById(R.id.tvOnHandUnits)).setText(String.valueOf(onHand));
                    ((TextView)row.findViewById(R.id.tvReservedUnits)).setText(String.valueOf(reserved));
                    ((TextView)row.findViewById(R.id.tvReorderLevel)).setText("Reorder at " + reorderLevel + " units");
                    
                    TextView tvStatus = row.findViewById(R.id.tvStockStatus);
                    tvStatus.setText(status);
                    tvStatus.setBackgroundResource(statusBg);
                    tvStatus.setTextColor(getResources().getColor(statusText, getTheme()));

                    row.setOnClickListener(v -> {
                        editingItemId = id;
                        Cursor itemCursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_ITEMS, null, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
                        if (itemCursor != null && itemCursor.moveToFirst()) {
                            etItemName.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME)));
                            etCost.setText(String.valueOf((int)itemCursor.getDouble(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COST))));
                            etSKU.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SKU)));
                            etCategory.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
                            etReorderLevel.setText(String.valueOf(itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REORDER_LEVEL))));
                            itemCursor.close();
                        }

                        stockManagementLayout.setVisibility(View.GONE);
                        addItemLayout.setVisibility(View.VISIBLE);
                    });
                    
                    llStockList.addView(row);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Update Stats and Filter Labels
        ((TextView)stockManagementLayout.findViewById(R.id.tvTotalItems)).setText(String.valueOf(totalItems));
        ((TextView)stockManagementLayout.findViewById(R.id.tvAvailableStock)).setText(String.valueOf(availableUnitsTotal));
        ((TextView)stockManagementLayout.findViewById(R.id.tvLowStock)).setText(String.valueOf(lowStockCount));
        ((TextView)stockManagementLayout.findViewById(R.id.tvOutOfStock)).setText(String.valueOf(outOfStockCount));
        ((TextView)stockManagementLayout.findViewById(R.id.tvStockItemsCount)).setText(totalItems + " items");

        TextView tvFilterAll = stockManagementLayout.findViewById(R.id.tvFilterAll);
        TextView tvFilterIn = stockManagementLayout.findViewById(R.id.tvFilterInStock);
        TextView tvFilterLow = stockManagementLayout.findViewById(R.id.tvFilterLowStock);
        TextView tvFilterOut = stockManagementLayout.findViewById(R.id.tvFilterOutOfStock);

        tvFilterAll.setText("All " + totalItems);
        tvFilterIn.setText("In stock " + inStockCount);
        tvFilterLow.setText("Low stock " + lowStockCount);
        tvFilterOut.setText("Out of stock " + outOfStockCount);

        // Update Category Label
        ((TextView)stockManagementLayout.findViewById(R.id.tvCategoryFilter)).setText("Bakery - " + stockCategoryFilter + " ▾");

        // Update Filter Styles
        updateStockFilterStyles(tvFilterAll, tvFilterIn, tvFilterLow, tvFilterOut);
        
        TextView tvAlertTitle = stockManagementLayout.findViewById(R.id.tvAlertTitle);
        TextView tvAlertDesc = stockManagementLayout.findViewById(R.id.tvAlertDesc);
        View alertBox = stockManagementLayout.findViewById(R.id.llAlertBox);
        int needReplenishment = lowStockCount + outOfStockCount;
        
        if (needReplenishment > 0 && alertBox != null) {
            tvAlertTitle.setText(needReplenishment + " items need replenishment");
            tvAlertDesc.setText(lowStockCount + " low stock - " + outOfStockCount + " out of stock. Use Add GRN to update.");
            alertBox.setVisibility(View.VISIBLE);
        } else if (alertBox != null) {
            alertBox.setVisibility(View.GONE);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
        ((TextView)stockManagementLayout.findViewById(R.id.tvInventoryDate)).setText(sdf.format(new Date()));

        stockManagementLayout.findViewById(R.id.btnViewGRNHistory).setOnClickListener(v -> showGRNHistoryDialog());
    }

    private void showGRNHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GRN Transaction History");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        Cursor cursor = dbHelper.getGRNHistory();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRN_DATE));
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRN_QUANTITY));

                View row = LayoutInflater.from(this).inflate(R.layout.item_transaction_row, content, false);
                ((TextView)row.findViewById(R.id.tvTransactionDate)).setText(date);
                ((TextView)row.findViewById(R.id.tvTransactionItems)).setText("Received " + qty + " units of " + itemName);
                ((TextView)row.findViewById(R.id.tvTransactionTotal)).setText("+" + qty);
                ((TextView)row.findViewById(R.id.tvTransactionTotal)).setTextColor(getResources().getColor(R.color.revenue_text, getTheme()));
                ((TextView)row.findViewById(R.id.tvTransactionStatus)).setText("RESTOCK");
                ((TextView)row.findViewById(R.id.tvTransactionStatus)).setTextColor(getResources().getColor(R.color.stockroom_teal, getTheme()));
                
                content.addView(row);
                
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(Color.LTGRAY);
                content.addView(divider);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No GRN history found.");
            tvEmpty.setPadding(0, 20, 0, 20);
            content.addView(tvEmpty);
        }

        scrollView.addView(content);
        layout.addView(scrollView);
        builder.setView(layout);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void updateStockFilterStyles(TextView all, TextView in, TextView low, TextView out) {
        all.setBackgroundResource(stockFilterMode == 0 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        all.setTextColor(getResources().getColor(stockFilterMode == 0 ? R.color.stockroom_teal : R.color.text_grey, getTheme()));

        in.setBackgroundResource(stockFilterMode == 1 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        in.setTextColor(getResources().getColor(stockFilterMode == 1 ? R.color.stockroom_teal : R.color.text_grey, getTheme()));

        low.setBackgroundResource(stockFilterMode == 2 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        low.setTextColor(getResources().getColor(stockFilterMode == 2 ? R.color.stockroom_teal : R.color.text_grey, getTheme()));

        out.setBackgroundResource(stockFilterMode == 3 ? R.drawable.bg_filter_selected_teal : R.drawable.bg_filter_unselected);
        out.setTextColor(getResources().getColor(stockFilterMode == 3 ? R.color.stockroom_teal : R.color.text_grey, getTheme()));
    }

    private void showAddGRNDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add GRN (Goods Received Note)");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_grn, null);
        LinearLayout llGRNItems = view.findViewById(R.id.llGRNItems);

        Cursor cursor = dbHelper.getAllItems();
        Map<Integer, EditText> inputMap = new HashMap<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_NAME));

                View itemRow = LayoutInflater.from(this).inflate(R.layout.item_grn_input, llGRNItems, false);
                ((TextView)itemRow.findViewById(R.id.tvItemName)).setText(name);
                EditText etQty = itemRow.findViewById(R.id.etReceivedQty);
                inputMap.put(id, etQty);
                llGRNItems.addView(itemRow);
            } while (cursor.moveToNext());
            cursor.close();
        }

        builder.setView(view);
        builder.setPositiveButton("Receive All", (dialog, which) -> {
            boolean anyUpdated = false;
            for (Map.Entry<Integer, EditText> entry : inputMap.entrySet()) {
                String qtyStr = entry.getValue().getText().toString().trim();
                if (!qtyStr.isEmpty()) {
                    int qty = Integer.parseInt(qtyStr);
                    if (qty > 0) {
                        dbHelper.updateStock(entry.getKey(), qty);
                        anyUpdated = true;
                    }
                }
            }
            if (anyUpdated) {
                Toast.makeText(this, "Stock updated successfully", Toast.LENGTH_SHORT).show();
                refreshStockManagement();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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