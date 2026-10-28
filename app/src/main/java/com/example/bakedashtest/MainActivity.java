package com.example.bakedashtest;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

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

        setupDeliverySearch();

        updateDeliveryDate();

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
                return true;
            } else if (itemId == R.id.nav_customers) {
                customersLayout.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_history) {
                historyLayout.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_items) {
                itemsLayout.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

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
}