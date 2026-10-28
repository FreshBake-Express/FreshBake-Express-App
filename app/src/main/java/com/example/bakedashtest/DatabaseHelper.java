package com.example.bakedashtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BakeryDB";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_STORE_NAME = "store_name";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_OLD_BILL = "old_bill";

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ITEM_NAME = "item_name";
    public static final String COLUMN_OUR_PRICE = "our_price";
    public static final String COLUMN_SHOP_PRICE = "shop_price";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_ORDER_TOTAL = "total_amount";
    public static final String COLUMN_ORDER_PAID = "paid_amount";
    public static final String COLUMN_ORDER_DUE = "due_amount";

    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_OI_ORDER_ID = "order_id";
    public static final String COLUMN_OI_ITEM_ID = "item_id";
    public static final String COLUMN_OI_QUANTITY = "quantity";
    public static final String COLUMN_OI_PRICE = "price_at_time";

    private static final String CREATE_TABLE_CUSTOMERS = "CREATE TABLE " + TABLE_CUSTOMERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
            + COLUMN_STORE_NAME + " TEXT,"
            + COLUMN_CONTACT + " TEXT,"
            + COLUMN_ADDRESS + " TEXT,"
            + COLUMN_OLD_BILL + " REAL"
            + ")";

    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ITEM_NAME + " TEXT,"
            + COLUMN_OUR_PRICE + " REAL,"
            + COLUMN_SHOP_PRICE + " REAL"
            + ")";

    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_CUSTOMER_ID + " INTEGER,"
            + COLUMN_ORDER_DATE + " TEXT,"
            + COLUMN_ORDER_TOTAL + " REAL,"
            + COLUMN_ORDER_PAID + " REAL,"
            + COLUMN_ORDER_DUE + " REAL"
            + ")";

    private static final String CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OI_ORDER_ID + " INTEGER,"
            + COLUMN_OI_ITEM_ID + " INTEGER,"
            + COLUMN_OI_QUANTITY + " INTEGER,"
            + COLUMN_OI_PRICE + " REAL"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_ITEMS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_ITEMS);
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_ORDERS);
            db.execSQL(CREATE_TABLE_ORDER_ITEMS);
        }
    }

    public long addCustomer(String firstName, String lastName, String storeName, String contact, String address, double oldBill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_STORE_NAME, storeName);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_OLD_BILL, oldBill);

        long id = db.insert(TABLE_CUSTOMERS, null, values);
        db.close();
        return id;
    }

    public int getCustomersCount() {
        String countQuery = "SELECT * FROM " + TABLE_CUSTOMERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public Cursor getAllCustomers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null, null, null, null, null, COLUMN_FIRST_NAME + " ASC");
    }

    public int updateCustomer(int id, String firstName, String lastName, String storeName, String contact, String address, double oldBill) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_STORE_NAME, storeName);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_OLD_BILL, oldBill);

        return db.update(TABLE_CUSTOMERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateCustomerDue(int id, double newDue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OLD_BILL, newDue);
        return db.update(TABLE_CUSTOMERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long addItem(String itemName, double ourPrice, double shopPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_OUR_PRICE, ourPrice);
        values.put(COLUMN_SHOP_PRICE, shopPrice);

        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ITEMS, null, null, null, null, null, COLUMN_ITEM_NAME + " ASC");
    }

    public int getItemsCount() {
        String countQuery = "SELECT * FROM " + TABLE_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateItem(int id, String itemName, double ourPrice, double shopPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_OUR_PRICE, ourPrice);
        values.put(COLUMN_SHOP_PRICE, shopPrice);

        return db.update(TABLE_ITEMS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor searchCustomers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FIRST_NAME + " LIKE ? OR " + COLUMN_LAST_NAME + " LIKE ? OR " + COLUMN_STORE_NAME + " LIKE ? OR " + COLUMN_CONTACT + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"};
        return db.query(TABLE_CUSTOMERS, null, selection, selectionArgs, null, null, COLUMN_FIRST_NAME + " ASC");
    }

    public long saveOrder(int customerId, String date, double total, double paid, double due, Map<Integer, Integer> quantities, Map<Integer, Double> prices) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_CUSTOMER_ID, customerId);
            orderValues.put(COLUMN_ORDER_DATE, date);
            orderValues.put(COLUMN_ORDER_TOTAL, total);
            orderValues.put(COLUMN_ORDER_PAID, paid);
            orderValues.put(COLUMN_ORDER_DUE, due);

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId != -1) {
                for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                    if (entry.getValue() > 0) {
                        ContentValues itemValues = new ContentValues();
                        itemValues.put(COLUMN_OI_ORDER_ID, orderId);
                        itemValues.put(COLUMN_OI_ITEM_ID, entry.getKey());
                        itemValues.put(COLUMN_OI_QUANTITY, entry.getValue());
                        itemValues.put(COLUMN_OI_PRICE, prices.get(entry.getKey()));
                        db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                    }
                }
            }
            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Cursor getTodayStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String query = "SELECT SUM(" + COLUMN_ORDER_TOTAL + ") as total_revenue, " +
                "COUNT(" + COLUMN_ID + ") as total_bills, " +
                "SUM(" + COLUMN_ORDER_DUE + ") as total_due, " +
                "(SELECT COUNT(DISTINCT " + COLUMN_ORDER_CUSTOMER_ID + ") FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " LIKE ? AND " + COLUMN_ORDER_DUE + " > 0) as pending_stores " +
                "FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " LIKE ?";
        return db.rawQuery(query, new String[]{today + "%", today + "%"});
    }

    public Cursor getItemWiseSales() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT i." + COLUMN_ITEM_NAME + ", i." + COLUMN_OUR_PRICE + ", SUM(oi." + COLUMN_OI_QUANTITY + ") as total_qty, SUM(oi." + COLUMN_OI_QUANTITY + " * oi." + COLUMN_OI_PRICE + ") as total_price " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "JOIN " + TABLE_ITEMS + " i ON oi." + COLUMN_OI_ITEM_ID + " = i." + COLUMN_ID + " " +
                "GROUP BY oi." + COLUMN_OI_ITEM_ID + " " +
                "ORDER BY total_qty DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getCustomerWiseSales() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c." + COLUMN_FIRST_NAME + ", c." + COLUMN_LAST_NAME + ", c." + COLUMN_STORE_NAME + ", c." + COLUMN_CONTACT + ", SUM(o." + COLUMN_ORDER_TOTAL + ") as total_sales, SUM(o." + COLUMN_ORDER_DUE + ") as current_due " +
                "FROM " + TABLE_ORDERS + " o " +
                "JOIN " + TABLE_CUSTOMERS + " c ON o." + COLUMN_ORDER_CUSTOMER_ID + " = c." + COLUMN_ID + " " +
                "GROUP BY o." + COLUMN_ORDER_CUSTOMER_ID + " " +
                "ORDER BY total_sales DESC";
        return db.rawQuery(query, null);
    }
}