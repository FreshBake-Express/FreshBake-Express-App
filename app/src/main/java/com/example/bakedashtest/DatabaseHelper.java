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
        int result = db.update(TABLE_CUSTOMERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return result;
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

    public boolean updateOrder(int orderId, double total, double paid, double due, Map<Integer, Integer> quantities, Map<Integer, Double> prices) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Get old due and customer_id to update customer's overall balance
            Cursor cursor = db.query(TABLE_ORDERS, new String[]{COLUMN_ORDER_CUSTOMER_ID, COLUMN_ORDER_DUE}, 
                    COLUMN_ID + " = ?", new String[]{String.valueOf(orderId)}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int customerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_CUSTOMER_ID));
                double oldDue = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_ORDER_DUE));
                cursor.close();

                // Update order
                ContentValues orderValues = new ContentValues();
                orderValues.put(COLUMN_ORDER_TOTAL, total);
                orderValues.put(COLUMN_ORDER_PAID, paid);
                orderValues.put(COLUMN_ORDER_DUE, due);
                db.update(TABLE_ORDERS, orderValues, COLUMN_ID + " = ?", new String[]{String.valueOf(orderId)});

                // Update customer's cumulative due
                double dueDiff = due - oldDue;
                db.execSQL("UPDATE " + TABLE_CUSTOMERS + " SET " + COLUMN_OLD_BILL + " = " + COLUMN_OLD_BILL + " + ? WHERE " + COLUMN_ID + " = ?",
                        new Object[]{dueDiff, customerId});
            }

            // Update items
            db.delete(TABLE_ORDER_ITEMS, COLUMN_OI_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
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
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Cursor getTodayStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String query = "SELECT " +
                "(SELECT IFNULL(SUM(" + COLUMN_ORDER_TOTAL + "), 0) FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " LIKE ?) as total_revenue, " +
                "(SELECT COUNT(" + COLUMN_ID + ") FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " LIKE ?) as total_bills, " +
                "(SELECT IFNULL(SUM(" + COLUMN_OLD_BILL + "), 0) FROM " + TABLE_CUSTOMERS + ") as total_due, " +
                "(SELECT COUNT(DISTINCT " + COLUMN_ORDER_CUSTOMER_ID + ") FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " LIKE ? AND " + COLUMN_ORDER_CUSTOMER_ID + " IN (SELECT " + COLUMN_ID + " FROM " + TABLE_CUSTOMERS + " WHERE " + COLUMN_OLD_BILL + " > 0)) as pending_stores";
        return db.rawQuery(query, new String[]{today + "%", today + "%", today + "%"});
    }

    public Cursor getItemWiseSales() {
        return getItemWiseSales("");
    }

    public Cursor getItemWiseSales(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT i." + COLUMN_ITEM_NAME + ", i." + COLUMN_OUR_PRICE + ", SUM(oi." + COLUMN_OI_QUANTITY + ") as total_qty, SUM(oi." + COLUMN_OI_QUANTITY + " * oi." + COLUMN_OI_PRICE + ") as total_price " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "JOIN " + TABLE_ITEMS + " i ON oi." + COLUMN_OI_ITEM_ID + " = i." + COLUMN_ID + " ";
        
        if (query != null && !query.isEmpty()) {
            sql += "WHERE i." + COLUMN_ITEM_NAME + " LIKE ? ";
        }
        
        sql += "GROUP BY oi." + COLUMN_OI_ITEM_ID + " " +
               "ORDER BY total_qty DESC";
               
        if (query != null && !query.isEmpty()) {
            return db.rawQuery(sql, new String[]{"%" + query + "%"});
        }
        return db.rawQuery(sql, null);
    }

    public Cursor getCustomerWiseSales() {
        return getCustomerWiseSales("");
    }

    public Cursor getCustomerWiseSales(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT c." + COLUMN_ID + ", c." + COLUMN_FIRST_NAME + ", c." + COLUMN_LAST_NAME + ", c." + COLUMN_STORE_NAME + ", c." + COLUMN_CONTACT + ", c." + COLUMN_OLD_BILL + " as current_due, IFNULL(SUM(o." + COLUMN_ORDER_TOTAL + "), 0) as total_sales, MAX(o." + COLUMN_ORDER_DATE + ") as last_order_date " +
                "FROM " + TABLE_CUSTOMERS + " c " +
                "LEFT JOIN " + TABLE_ORDERS + " o ON o." + COLUMN_ORDER_CUSTOMER_ID + " = c." + COLUMN_ID + " ";
        
        if (query != null && !query.isEmpty()) {
            sql += "WHERE c." + COLUMN_FIRST_NAME + " LIKE ? OR c." + COLUMN_LAST_NAME + " LIKE ? OR c." + COLUMN_STORE_NAME + " LIKE ? ";
        }
        
        sql += "GROUP BY c." + COLUMN_ID + " " +
               "HAVING total_sales > 0 OR current_due > 0 " +
               "ORDER BY last_order_date DESC, current_due DESC";

        if (query != null && !query.isEmpty()) {
            String q = "%" + query + "%";
            return db.rawQuery(sql, new String[]{q, q, q});
        }
        return db.rawQuery(sql, null);
    }

    public Cursor getCustomerTransactions(int customerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_CUSTOMER_ID + " = ? ORDER BY " + COLUMN_ORDER_DATE + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(customerId)});
    }

    public Cursor getOrderItems(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.*, i." + COLUMN_ITEM_NAME + " FROM " + TABLE_ORDER_ITEMS + " oi " +
                "JOIN " + TABLE_ITEMS + " i ON oi." + COLUMN_OI_ITEM_ID + " = i." + COLUMN_ID + " " +
                "WHERE oi." + COLUMN_OI_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }
}