package com.example.bakedashtest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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
    private LinearLayout llCustomerList;
    private TextView tvCustomerCount;
    private int editingCustomerId = -1;
    private TextView tvAddCustomerTitle;
    private BottomNavigationView bottomNav;

    // Form fields
    private EditText etFirstName, etLastName, etStoreName, etContactNumber, etAddress, etOldBillAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        llCustomerList = findViewById(R.id.llCustomerList);
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
            // Logic to save item would go here
            addItemLayout.setVisibility(View.GONE);
            itemsLayout.setVisibility(View.VISIBLE);
            bottomNav.setVisibility(View.VISIBLE);
        });

        setupItemsList();
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

    private void setupItemsList() {
        setupProductRow(findViewById(R.id.item1), "Bread", "35", "40");
        setupProductRow(findViewById(R.id.item2), "Butter Cookies", "100", "120");
        setupProductRow(findViewById(R.id.item3), "Chocolate Cake", "400", "450");
        setupProductRow(findViewById(R.id.item4), "Milk Bread", "38", "45");
        setupProductRow(findViewById(R.id.item5), "Fruit Jam", "75", "85");
    }

    private void setupProductRow(View row, String name, String ourPrice, String shopPrice) {
        if (row == null) return;
        android.widget.TextView tvName = row.findViewById(R.id.productName);
        android.widget.TextView tvOurPrice = row.findViewById(R.id.ourPrice);
        android.widget.TextView tvShopPrice = row.findViewById(R.id.shopPrice);
        if (tvName != null) tvName.setText(name);
        if (tvOurPrice != null) tvOurPrice.setText(ourPrice);
        if (tvShopPrice != null) tvShopPrice.setText(shopPrice);
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