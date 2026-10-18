package com.example.bakedashtest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        View deliveryLayout = findViewById(R.id.deliveryLayout);
        View customersLayout = findViewById(R.id.customersLayout);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_delivery) {
                deliveryLayout.setVisibility(View.VISIBLE);
                customersLayout.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_customers) {
                deliveryLayout.setVisibility(View.GONE);
                customersLayout.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        setupCustomersList();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupCustomersList() {
        setupCustomerRow(findViewById(R.id.customer1), "AP", "Aarav Patel", "Sunrise Grocery", "+91 98765 43210", "₹450", true);
        setupCustomerRow(findViewById(R.id.customer2), "MS", "Meera Shah", "Green Valley Foods", "+91 87654 32109", "₹0", false);
        setupCustomerRow(findViewById(R.id.customer3), "RV", "Rohan Verma", "City Bakers", "+91 76543 21098", "₹1,200", false);
        setupCustomerRow(findViewById(R.id.customer4), "PN", "Priya Nair", "Daily Mart", "+91 65432 10987", "₹0", false);
        setupCustomerRow(findViewById(R.id.customer5), "IK", "Imran Khan", "Fresh Choice", "+91 54321 09876", "₹320", false);
    }

    private void setupCustomerRow(View row, String avatar, String name, String store, String phone, String amount, boolean isSelected) {
        if (row == null) return;
        
        android.widget.TextView tvAvatar = row.findViewById(R.id.customerAvatar);
        android.widget.TextView tvName = row.findViewById(R.id.customerName);
        android.widget.TextView tvStore = row.findViewById(R.id.storeName);
        android.widget.TextView tvPhone = row.findViewById(R.id.phoneNumber);
        android.widget.TextView tvAmount = row.findViewById(R.id.dueAmount);

        tvAvatar.setText(avatar);
        tvName.setText(name);
        tvStore.setText(store);
        tvPhone.setText(phone);
        tvAmount.setText(amount);

        if (isSelected) {
            row.setBackgroundResource(R.drawable.bg_customer_card);
        } else {
            row.setBackgroundResource(R.drawable.bg_customer_card_white);
        }
    }
}