package com.example.bakedashtest;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class CustomersFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout llCustomerList;
    private TextView tvCustomerCount;
    private TextView tvTotalClientsKpi, tvWithDueKpi, tvTotalReceivableKpi;
    private TextView filterAll, filterDue, filterSettled, tvCustomerListHeader;
    private EditText etCustomerListSearch;
    private LinearLayout llSearchContainer;
    private ImageView ivSearchIcon;
    private MaterialButton btnCreateCustomer;
    private int customerFilterMode = 0; // 0: All, 1: Due, 2: Settled

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customers, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        llCustomerList = view.findViewById(R.id.llCustomerList);
        tvCustomerCount = view.findViewById(R.id.tvCustomerCount);
        tvTotalClientsKpi = view.findViewById(R.id.tvTotalClientsKpi);
        tvWithDueKpi = view.findViewById(R.id.tvWithDueKpi);
        tvTotalReceivableKpi = view.findViewById(R.id.tvTotalReceivableKpi);
        filterAll = view.findViewById(R.id.filterAll);
        filterDue = view.findViewById(R.id.filterDue);
        filterSettled = view.findViewById(R.id.filterSettled);
        tvCustomerListHeader = view.findViewById(R.id.tvCustomerListHeader);
        etCustomerListSearch = view.findViewById(R.id.etCustomerListSearch);
        llSearchContainer = view.findViewById(R.id.llSearchContainer);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        btnCreateCustomer = view.findViewById(R.id.btnCreateCustomer);

        setupListeners();
        refreshCustomers();
        updateCustomerFilterStyles();

        return view;
    }

    private void setupListeners() {
        if (etCustomerListSearch != null) {
            etCustomerListSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    refreshCustomers();
                }
            });

            etCustomerListSearch.setOnFocusChangeListener((v, hasFocus) -> {
                if (llSearchContainer != null) {
                    llSearchContainer.setBackgroundResource(hasFocus ? R.drawable.bg_search_focused : R.drawable.bg_search_floating);
                }
                if (ivSearchIcon != null) {
                    ivSearchIcon.setColorFilter(hasFocus ? getResources().getColor(R.color.emerald_bright, requireContext().getTheme()) : getResources().getColor(R.color.emerald_primary, requireContext().getTheme()));
                }
            });
        }

        filterAll.setOnClickListener(v -> { customerFilterMode = 0; updateCustomerFilterStyles(); refreshCustomers(); });
        filterDue.setOnClickListener(v -> { customerFilterMode = 1; updateCustomerFilterStyles(); refreshCustomers(); });
        filterSettled.setOnClickListener(v -> { customerFilterMode = 2; updateCustomerFilterStyles(); refreshCustomers(); });

        btnCreateCustomer.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, CustomerFormFragment.newInstance(-1))
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void updateCustomerFilterStyles() {
        filterAll.setBackgroundResource(customerFilterMode == 0 ? R.drawable.bg_filter_active : R.drawable.bg_filter_inactive);
        filterAll.setTextColor(customerFilterMode == 0 ? getResources().getColor(R.color.emerald_header_top, requireContext().getTheme()) : getResources().getColor(R.color.text_secondary, requireContext().getTheme()));
        filterAll.setTypeface(null, customerFilterMode == 0 ? Typeface.BOLD : Typeface.NORMAL);

        filterDue.setBackgroundResource(customerFilterMode == 1 ? R.drawable.bg_filter_active : R.drawable.bg_filter_inactive);
        filterDue.setTextColor(customerFilterMode == 1 ? getResources().getColor(R.color.emerald_header_top, requireContext().getTheme()) : getResources().getColor(R.color.text_secondary, requireContext().getTheme()));
        filterDue.setTypeface(null, customerFilterMode == 1 ? Typeface.BOLD : Typeface.NORMAL);

        filterSettled.setBackgroundResource(customerFilterMode == 2 ? R.drawable.bg_filter_active : R.drawable.bg_filter_inactive);
        filterSettled.setTextColor(customerFilterMode == 2 ? getResources().getColor(R.color.emerald_header_top, requireContext().getTheme()) : getResources().getColor(R.color.text_secondary, requireContext().getTheme()));
        filterSettled.setTypeface(null, customerFilterMode == 2 ? Typeface.BOLD : Typeface.NORMAL);

        if (tvCustomerListHeader != null) {
            if (customerFilterMode == 0) tvCustomerListHeader.setText("All customers");
            else if (customerFilterMode == 1) tvCustomerListHeader.setText("Customers with due");
            else if (customerFilterMode == 2) tvCustomerListHeader.setText("Settled customers");
        }
    }

    private void refreshCustomers() {
        if (llCustomerList == null) return;
        llCustomerList.removeAllViews();

        String query = etCustomerListSearch.getText().toString().trim();
        Cursor cursor;
        if (query.isEmpty()) {
            cursor = dbHelper.getAllCustomers();
        } else {
            cursor = dbHelper.searchCustomers(query);
        }

        int totalClients = 0;
        int withDueCount = 0;
        double totalReceivable = 0;
        int displayedCount = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
                double oldBill = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL));

                totalClients++;
                if (oldBill > 0) {
                    withDueCount++;
                    totalReceivable += oldBill;
                }

                // Filter logic
                boolean shouldAdd = true;
                if (customerFilterMode == 1 && oldBill <= 0) shouldAdd = false;
                if (customerFilterMode == 2 && oldBill > 0) shouldAdd = false;

                if (shouldAdd) {
                    displayedCount++;
                    View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_customer_row, llCustomerList, false);
                    setupCustomerRow(row, id, firstName, lastName, storeName, contact, address, oldBill);
                    llCustomerList.addView(row);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        tvTotalClientsKpi.setText(String.valueOf(totalClients));
        tvWithDueKpi.setText(String.valueOf(withDueCount));
        tvTotalReceivableKpi.setText("Rs. " + String.format(Locale.getDefault(), "%,.0f", totalReceivable));
        tvCustomerCount.setText(displayedCount + (displayedCount == 1 ? " customer" : " customers"));
    }

    private void setupCustomerRow(View row, int id, String firstName, String lastName, String store, String phone, String address, double amount) {
        TextView tvAvatar = row.findViewById(R.id.customerAvatar);
        TextView tvName = row.findViewById(R.id.customerName);
        TextView tvStore = row.findViewById(R.id.storeName);
        TextView tvPhone = row.findViewById(R.id.phoneNumber);
        TextView tvStatusPill = row.findViewById(R.id.statusPill);
        View customerCard = row.findViewById(R.id.customerCard);

        String initials = "";
        if (firstName.length() > 0) initials += firstName.charAt(0);
        if (lastName.length() > 0) initials += lastName.charAt(0);
        tvAvatar.setText(initials.toUpperCase());

        tvName.setText(firstName + " " + lastName);
        tvStore.setText(store);
        tvPhone.setText(phone);
        
        if (amount > 0) {
            tvStatusPill.setText("Due Rs. " + String.format(Locale.getDefault(), "%,.0f", amount));
            tvStatusPill.setBackgroundResource(R.drawable.bg_status_pill_due);
            tvStatusPill.setTextColor(Color.parseColor("#92400E"));
        } else {
            tvStatusPill.setText("Settled");
            tvStatusPill.setBackgroundResource(R.drawable.bg_status_pill_settled);
            tvStatusPill.setTextColor(Color.parseColor("#065F46"));
        }

        customerCard.setOnClickListener(v -> {
            // Start a delivery bill for this customer
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToDelivery(id);
            }
        });

        View arrowIcon = row.findViewById(R.id.arrowIcon);
        if (arrowIcon != null) {
            arrowIcon.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, CustomerFormFragment.newInstance(id))
                        .addToBackStack(null)
                        .commit();
            });
        }
    }
}