package com.example.bakedashtest;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class CustomerFormFragment extends Fragment {

    private static final String ARG_CUSTOMER_ID = "customer_id";
    private int customerId = -1;

    private DatabaseHelper dbHelper;
    private EditText etFirstName, etLastName, etStoreName, etContactNumber, etAddress, etOldBillAmount;
    private TextView tvTitle, tvPreviewInitials, tvPreviewName, tvPreviewStore;
    private ImageView btnBack;
    private MaterialButton btnSave;

    public static CustomerFormFragment newInstance(int customerId) {
        CustomerFormFragment fragment = new CustomerFormFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CUSTOMER_ID, customerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerId = getArguments().getInt(ARG_CUSTOMER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_customer, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        tvTitle = view.findViewById(R.id.tvAddCustomerTitle);
        btnBack = view.findViewById(R.id.backButton);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etStoreName = view.findViewById(R.id.etStoreName);
        etContactNumber = view.findViewById(R.id.etContactNumber);
        etAddress = view.findViewById(R.id.etAddress);
        etOldBillAmount = view.findViewById(R.id.etOldBillAmount);
        btnSave = view.findViewById(R.id.saveButton);

        tvPreviewInitials = view.findViewById(R.id.previewAvatar);
        tvPreviewName = view.findViewById(R.id.previewName);
        tvPreviewStore = view.findViewById(R.id.previewStore);

        setupListeners();

        if (customerId != -1) {
            loadCustomerData();
            tvTitle.setText("Edit Customer");
            btnSave.setText("Save Changes");
        } else {
            tvTitle.setText("Add Customer");
            btnSave.setText("Create Account");
        }

        return view;
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        TextWatcher previewWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updatePreview();
            }
        };

        etFirstName.addTextChangedListener(previewWatcher);
        etLastName.addTextChangedListener(previewWatcher);
        etStoreName.addTextChangedListener(previewWatcher);

        btnSave.setOnClickListener(v -> saveCustomer());
    }

    private void updatePreview() {
        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String store = etStoreName.getText().toString().trim();

        String initials = "";
        if (!first.isEmpty()) initials += first.charAt(0);
        if (!last.isEmpty()) initials += last.charAt(0);
        tvPreviewInitials.setText(initials.isEmpty() ? "NC" : initials.toUpperCase());

        String fullName = first + " " + last;
        tvPreviewName.setText(fullName.trim().isEmpty() ? "New Customer" : fullName);
        tvPreviewStore.setText(store.isEmpty() ? "Store name preview" : store);
    }

    private void loadCustomerData() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_CUSTOMERS,
                null,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(customerId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            etFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME)));
            etLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME)));
            etStoreName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STORE_NAME)));
            etContactNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTACT)));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)));
            etOldBillAmount.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OLD_BILL))));
            cursor.close();
        }
        updatePreview();
    }

    private void saveCustomer() {
        String first = etFirstName.getText().toString().trim();
        String last = etLastName.getText().toString().trim();
        String store = etStoreName.getText().toString().trim();
        String contact = etContactNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String billStr = etOldBillAmount.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || store.isEmpty() || contact.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double oldBill = billStr.isEmpty() ? 0 : Double.parseDouble(billStr);

        long result;
        if (customerId == -1) {
            result = dbHelper.addCustomer(first, last, store, contact, address, oldBill);
        } else {
            result = dbHelper.updateCustomer(customerId, first, last, store, contact, address, oldBill);
        }

        if (result != -1) {
            Toast.makeText(requireContext(), "Customer saved successfully", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Error saving customer", Toast.LENGTH_SHORT).show();
        }
    }
}
