package com.example.bakedashtest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.graphics.Rect;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private View navDrawerContainer;

    // Custom Nav Items
    private LinearLayout navDeliveryBilling, navCustomers, navHistoryReports, navProducts, navStockInventory, navSettings, navHelpSupport, btnLogout;
    private List<LinearLayout> navItemsList = new ArrayList<>();

    private WebView mWebView;
    private List<PrintJob> printJobs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(getResources().getColor(R.color.primary_green, getTheme())),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navDrawerContainer = findViewById(R.id.navDrawerContainer);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Initialize Custom Nav Items
        navDeliveryBilling = findViewById(R.id.navDeliveryBilling);
        navCustomers = findViewById(R.id.navCustomers);
        navHistoryReports = findViewById(R.id.navHistoryReports);
        navProducts = findViewById(R.id.navProducts);
        navStockInventory = findViewById(R.id.navStockInventory);
        navSettings = findViewById(R.id.navSettings);
        navHelpSupport = findViewById(R.id.navHelpSupport);
        btnLogout = findViewById(R.id.btnLogout);

        navItemsList.add(navDeliveryBilling);
        navItemsList.add(navCustomers);
        navItemsList.add(navHistoryReports);
        navItemsList.add(navProducts);
        navItemsList.add(navStockInventory);
        navItemsList.add(navSettings);
        navItemsList.add(navHelpSupport);

        setupNavigationClickListeners();

        // Apply Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        if (navDrawerContainer != null) {
            final View navHeader = findViewById(R.id.navHeader);
            ViewCompat.setOnApplyWindowInsetsListener(navDrawerContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (navHeader != null) {
                    float density = getResources().getDisplayMetrics().density;
                    int topPadding = systemBars.top + (int) (28 * density);
                    int sidePadding = (int) (18 * density);
                    int bottomPadding = (int) (16 * density);
                    navHeader.setPadding(sidePadding, topPadding, sidePadding, bottomPadding);
                }
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });
        }

        // Back stack management
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (!(currentFragment instanceof DeliveryFragment)) {
                        navigateToDelivery();
                    } else {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                        setEnabled(true);
                    }
                }
            }
        });

        // Default screen
        if (savedInstanceState == null) {
            navigateToDelivery();
        }
    }

    private void setupNavigationClickListeners() {
        navDeliveryBilling.setOnClickListener(v -> {
            navigateToDelivery();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navCustomers.setOnClickListener(v -> {
            selectNavItem(navCustomers);
            showFragment(new CustomersFragment(), "Customers");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navHistoryReports.setOnClickListener(v -> {
            selectNavItem(navHistoryReports);
            showFragment(new HistoryFragment(), "History & Reports");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navProducts.setOnClickListener(v -> {
            selectNavItem(navProducts);
            showFragment(new ProductsFragment(), "Manage Products");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navStockInventory.setOnClickListener(v -> {
            selectNavItem(navStockInventory);
            showFragment(new StockFragment(), "Stock & Inventory");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navSettings.setOnClickListener(v -> {
            selectNavItem(navSettings);
            showFragment(new SettingsFragment(), "Settings");
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        navHelpSupport.setOnClickListener(v -> {
            Toast.makeText(this, "Help & Support coming soon", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        btnLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("End Shift")
                    .setMessage("Are you sure you want to end your shift and logout?")
                    .setPositiveButton("End Shift", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void selectNavItem(LinearLayout selectedItem) {
        for (LinearLayout item : navItemsList) {
            boolean isActive = (item == selectedItem);
            item.setBackgroundResource(isActive ? R.drawable.bg_nav_item_active : R.drawable.bg_nav_item_normal);
            
            for (int i = 0; i < item.getChildCount(); i++) {
                View child = item.getChildAt(i);
                if (child instanceof FrameLayout) {
                    ImageView icon = (ImageView) ((FrameLayout) child).getChildAt(0);
                    if (icon != null) {
                        icon.setColorFilter(getResources().getColor(isActive ? R.color.emerald_header_top : R.color.drawer_icon_inactive, getTheme()));
                    }
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(getResources().getColor(isActive ? R.color.emerald_header_top : R.color.drawer_text_inactive, getTheme()));
                }
            }
        }
    }

    public void showFragment(Fragment fragment, String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void navigateToDelivery() {
        navigateToDelivery(-1);
    }

    public void navigateToDelivery(int customerId) {
        selectNavItem(navDeliveryBilling);
        DeliveryFragment fragment = new DeliveryFragment();
        if (customerId != -1) {
            Bundle args = new Bundle();
            args.putInt("selected_customer_id", customerId);
            fragment.setArguments(args);
        }
        showFragment(fragment, "Delivery & Billing");
    }

    public void showProductsFragment() {
        selectNavItem(navProducts);
        showFragment(new ProductsFragment(), "Manage Products");
    }

    public void showCustomerBillingHistoryFragment(int customerId, String customerName, String storeName, double totalSales, double currentDue) {
        CustomerBillingHistoryFragment fragment = CustomerBillingHistoryFragment.newInstance(customerId, customerName, storeName, totalSales, currentDue);
        showFragment(fragment, "Billing History");
    }

    public void showBillDetailsFragment(int orderId, String storeName) {
        BillDetailsFragment fragment = BillDetailsFragment.newInstance(orderId, storeName);
        showFragment(fragment, "Bill Details");
    }

    public void showAddGRNFragment() {
        showFragment(new AddGRNFragment(), "Add Stock (GRN)");
    }

    public void printBill(int orderId) {
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
                .append("<p><strong>Sub Total: </strong>Rs. ").append((int)total).append("</p>")
                .append("<p><strong>Paid Amount: </strong>Rs. ").append((int)paid).append("</p>");
            if (due < 0) {
                html.append("<p><strong>Advance Credit: </strong>Rs. ").append(Math.abs((int)due)).append("</p>");
            } else {
                html.append("<p><strong>Balance Due: </strong>Rs. ").append((int)due).append("</p>");
            }
            html.append("</div>");

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
