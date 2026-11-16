package com.example.bakedashtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BakeryDB";
    private static final int DATABASE_VERSION = 8;

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
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_ON_HAND = "on_hand";
    public static final String COLUMN_RESERVED = "reserved";
    public static final String COLUMN_REORDER_LEVEL = "reorder_level";
    public static final String COLUMN_SKU = "sku";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_RETURN_STOCK = "return_stock";
    public static final String COLUMN_IS_ACTIVE = "is_active";

    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_ORDER_GROSS = "gross_amount";
    public static final String COLUMN_ORDER_RETURN = "return_amount";
    public static final String COLUMN_ORDER_TOTAL = "total_amount";
    public static final String COLUMN_ORDER_PAID = "paid_amount";
    public static final String COLUMN_ORDER_DUE = "due_amount";

    public static final String TABLE_ORDER_RETURNS = "order_returns";
    public static final String COLUMN_RET_ORDER_ID = "order_id";
    public static final String COLUMN_RET_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_RET_ITEM_ID = "item_id";
    public static final String COLUMN_RET_QUANTITY = "quantity";
    public static final String COLUMN_RET_PRICE = "price_at_time";
    public static final String COLUMN_RET_REASON = "reason";
    public static final String COLUMN_RET_DATE = "return_date";

    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COLUMN_OI_ORDER_ID = "order_id";
    public static final String COLUMN_OI_ITEM_ID = "item_id";
    public static final String COLUMN_OI_QUANTITY = "quantity";
    public static final String COLUMN_OI_PRICE = "price_at_time";

    public static final String TABLE_GRN_LOG = "grn_log";
    public static final String COLUMN_GRN_DATE = "grn_date";
    public static final String COLUMN_GRN_ITEM_ID = "item_id";
    public static final String COLUMN_GRN_QUANTITY = "quantity";

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
            + COLUMN_COST + " REAL,"
            + COLUMN_ON_HAND + " INTEGER DEFAULT 0,"
            + COLUMN_RESERVED + " INTEGER DEFAULT 0,"
            + COLUMN_REORDER_LEVEL + " INTEGER DEFAULT 15,"
            + COLUMN_SKU + " TEXT,"
            + COLUMN_CATEGORY + " TEXT,"
            + COLUMN_RETURN_STOCK + " INTEGER DEFAULT 0,"
            + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1"
            + ")";

    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ORDER_CUSTOMER_ID + " INTEGER,"
            + COLUMN_ORDER_DATE + " TEXT,"
            + COLUMN_ORDER_GROSS + " REAL DEFAULT 0,"
            + COLUMN_ORDER_RETURN + " REAL DEFAULT 0,"
            + COLUMN_ORDER_TOTAL + " REAL,"
            + COLUMN_ORDER_PAID + " REAL,"
            + COLUMN_ORDER_DUE + " REAL"
            + ")";

    private static final String CREATE_TABLE_ORDER_RETURNS = "CREATE TABLE " + TABLE_ORDER_RETURNS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_RET_ORDER_ID + " INTEGER,"
            + COLUMN_RET_CUSTOMER_ID + " INTEGER,"
            + COLUMN_RET_ITEM_ID + " INTEGER,"
            + COLUMN_RET_QUANTITY + " INTEGER,"
            + COLUMN_RET_PRICE + " REAL,"
            + COLUMN_RET_REASON + " TEXT,"
            + COLUMN_RET_DATE + " TEXT"
            + ")";

    private static final String CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OI_ORDER_ID + " INTEGER,"
            + COLUMN_OI_ITEM_ID + " INTEGER,"
            + COLUMN_OI_QUANTITY + " INTEGER,"
            + COLUMN_OI_PRICE + " REAL"
            + ")";

    private static final String CREATE_TABLE_GRN_LOG = "CREATE TABLE " + TABLE_GRN_LOG + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_GRN_DATE + " TEXT,"
            + COLUMN_GRN_ITEM_ID + " INTEGER,"
            + COLUMN_GRN_QUANTITY + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CUSTOMERS);
        db.execSQL(CREATE_TABLE_ITEMS);
        db.execSQL(CREATE_TABLE_ORDERS);
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);
        db.execSQL(CREATE_TABLE_ORDER_RETURNS);
        db.execSQL(CREATE_TABLE_GRN_LOG);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureReturnTables(db);
    }

    private void ensureReturnTables(SQLiteDatabase db) {
        try {
            // Check if gross_amount exists in orders
            Cursor c = db.rawQuery("PRAGMA table_info(" + TABLE_ORDERS + ")", null);
            boolean hasGross = false;
            while (c.moveToNext()) {
                if ("gross_amount".equals(c.getString(1))) hasGross = true;
            }
            c.close();
            if (!hasGross) {
                db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COLUMN_ORDER_GROSS + " REAL DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COLUMN_ORDER_RETURN + " REAL DEFAULT 0");
            }

            // Check if return_stock exists in items
            Cursor ci = db.rawQuery("PRAGMA table_info(" + TABLE_ITEMS + ")", null);
            boolean hasReturnStock = false;
            while (ci.moveToNext()) {
                if ("return_stock".equals(ci.getString(1))) hasReturnStock = true;
            }
            ci.close();
            if (!hasReturnStock) {
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_RETURN_STOCK + " INTEGER DEFAULT 0");
            }

            db.execSQL(CREATE_TABLE_ORDER_RETURNS);
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE items_new (id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT, cost REAL)");
            db.execSQL("INSERT INTO items_new (id, item_name, cost) SELECT id, item_name, our_price FROM items");
            db.execSQL("DROP TABLE items");
            db.execSQL("ALTER TABLE items_new RENAME TO items");
        } 
        
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_ORDERS);
            db.execSQL(CREATE_TABLE_ORDER_ITEMS);
        }

        if (oldVersion < 4) {
            // Check if cost already exists to avoid errors
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_ITEMS + ")", null);
            boolean hasCost = false;
            while(cursor.moveToNext()){
                if("cost".equals(cursor.getString(1))) hasCost = true;
            }
            cursor.close();
            if(!hasCost) {
                db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN cost REAL DEFAULT 0");
            }
        }

        if (oldVersion < 6) {
            // Add missing inventory columns to items table
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_ON_HAND + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_RESERVED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_REORDER_LEVEL + " INTEGER DEFAULT 15");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_SKU + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT");

            // Create GRN log table if it doesn't exist
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GRN_LOG + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_GRN_DATE + " TEXT,"
                    + COLUMN_GRN_ITEM_ID + " INTEGER,"
                    + COLUMN_GRN_QUANTITY + " INTEGER"
                    + ")");
        }

        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1");
        }

        if (oldVersion < 8) {
            ensureReturnTables(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Safe downgrade handling to prevent app crashes when rolling back
        ensureReturnTables(db);
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

    public long addItem(String itemName, double cost, String sku, String category, int reorderLevel, int isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_COST, cost);
        values.put(COLUMN_SKU, sku);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_REORDER_LEVEL, reorderLevel);
        values.put(COLUMN_IS_ACTIVE, isActive);

        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    public long addItem(String itemName, double cost, String sku, String category, int reorderLevel) {
        return addItem(itemName, cost, sku, category, reorderLevel, 1);
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);
    }

    public Cursor searchItems(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ITEM_NAME + " LIKE ? OR " + COLUMN_SKU + " LIKE ? OR " + COLUMN_CATEGORY + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};
        return db.query(TABLE_ITEMS, null, selection, selectionArgs, null, null, COLUMN_ITEM_NAME + " ASC");
    }

    public int updateItem(int id, String itemName, double cost, String sku, String category, int reorderLevel, int isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, itemName);
        values.put(COLUMN_COST, cost);
        values.put(COLUMN_SKU, sku);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_REORDER_LEVEL, reorderLevel);
        values.put(COLUMN_IS_ACTIVE, isActive);

        return db.update(TABLE_ITEMS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateItem(int id, String itemName, double cost, String sku, String category, int reorderLevel) {
        return updateItem(id, itemName, cost, sku, category, reorderLevel, 1);
    }

    public Cursor getActiveItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ITEMS, null, COLUMN_IS_ACTIVE + " = 1", null, null, null, COLUMN_ITEM_NAME + " ASC");
    }

    public boolean toggleItemStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ACTIVE, status);
        return db.update(TABLE_ITEMS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean addStock(int id, int addedQty) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " + ? WHERE " + COLUMN_ID + " = ?",
                new Object[]{addedQty, id});
        return true;
    }

    public boolean setOnHandStock(int id, int newOnHand) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ON_HAND, Math.max(0, newOnHand));
        return db.update(TABLE_ITEMS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public long addGRNLog(int itemId, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GRN_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_GRN_ITEM_ID, itemId);
        values.put(COLUMN_GRN_QUANTITY, qty);
        return db.insert(TABLE_GRN_LOG, null, values);
    }

    public boolean updateStock(int id, int addedQty) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " + ? WHERE " + COLUMN_ID + " = ?",
                    new Object[]{addedQty, id});

            ContentValues grnValues = new ContentValues();
            grnValues.put(COLUMN_GRN_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            grnValues.put(COLUMN_GRN_ITEM_ID, id);
            grnValues.put(COLUMN_GRN_QUANTITY, addedQty);
            db.insert(TABLE_GRN_LOG, null, grnValues);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean reserveStock(int id, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_RESERVED + " = " + COLUMN_RESERVED + " + ? WHERE " + COLUMN_ID + " = ?",
                new Object[]{qty, id});
        return true;
    }

    public boolean completeReservation(int id, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Decrement reserved and decrement on_hand
            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_RESERVED + " = " + COLUMN_RESERVED + " - ?, " +
                            COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " - ? WHERE " + COLUMN_ID + " = ?",
                    new Object[]{qty, qty, id});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public Cursor searchCustomers(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FIRST_NAME + " LIKE ? OR " + COLUMN_LAST_NAME + " LIKE ? OR " + COLUMN_STORE_NAME + " LIKE ? OR " + COLUMN_CONTACT + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%", "%" + query + "%"};
        return db.query(TABLE_CUSTOMERS, null, selection, selectionArgs, null, null, COLUMN_FIRST_NAME + " ASC");
    }

    public Cursor getCustomerById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CUSTOMERS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public long saveOrder(int customerId, String date, double grossTotal, double returnTotal, double netTotal,
                          double paid, double due, Map<Integer, Integer> quantities,
                          Map<Integer, Integer> returnQuantities, Map<Integer, String> returnReasons,
                          Map<Integer, Double> prices) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_ORDER_CUSTOMER_ID, customerId);
            orderValues.put(COLUMN_ORDER_DATE, date);
            orderValues.put(COLUMN_ORDER_GROSS, grossTotal);
            orderValues.put(COLUMN_ORDER_RETURN, returnTotal);
            orderValues.put(COLUMN_ORDER_TOTAL, netTotal);
            orderValues.put(COLUMN_ORDER_PAID, paid);
            orderValues.put(COLUMN_ORDER_DUE, due);

            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId != -1) {
                // 1. Insert delivered items & decrement on_hand
                if (quantities != null) {
                    for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                        if (entry.getValue() != null && entry.getValue() > 0) {
                            ContentValues itemValues = new ContentValues();
                            itemValues.put(COLUMN_OI_ORDER_ID, orderId);
                            itemValues.put(COLUMN_OI_ITEM_ID, entry.getKey());
                            itemValues.put(COLUMN_OI_QUANTITY, entry.getValue());
                            itemValues.put(COLUMN_OI_PRICE, prices != null ? prices.get(entry.getKey()) : 0.0);
                            db.insert(TABLE_ORDER_ITEMS, null, itemValues);

                            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " - ? WHERE " + COLUMN_ID + " = ?",
                                    new Object[]{entry.getValue(), entry.getKey()});
                            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_RESERVED + " = CASE WHEN " + COLUMN_RESERVED + " >= ? THEN " + COLUMN_RESERVED + " - ? ELSE 0 END WHERE " + COLUMN_ID + " = ?",
                                    new Object[]{entry.getValue(), entry.getValue(), entry.getKey()});
                        }
                    }
                }

                // 2. Insert returned items (logged to order_returns table & reflected in stock inventory)
                if (returnQuantities != null) {
                    for (Map.Entry<Integer, Integer> retEntry : returnQuantities.entrySet()) {
                        if (retEntry.getValue() != null && retEntry.getValue() > 0) {
                            ContentValues retValues = new ContentValues();
                            retValues.put(COLUMN_RET_ORDER_ID, orderId);
                            retValues.put(COLUMN_RET_CUSTOMER_ID, customerId);
                            retValues.put(COLUMN_RET_ITEM_ID, retEntry.getKey());
                            retValues.put(COLUMN_RET_QUANTITY, retEntry.getValue());
                            retValues.put(COLUMN_RET_PRICE, prices != null ? prices.get(retEntry.getKey()) : 0.0);
                            String reason = (returnReasons != null && returnReasons.containsKey(retEntry.getKey()))
                                    ? returnReasons.get(retEntry.getKey()) : "Stale";
                            retValues.put(COLUMN_RET_REASON, reason);
                            retValues.put(COLUMN_RET_DATE, date);
                            db.insert(TABLE_ORDER_RETURNS, null, retValues);

                            // Update return_stock on the item
                            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_RETURN_STOCK + " = " + COLUMN_RETURN_STOCK + " + ? WHERE " + COLUMN_ID + " = ?",
                                    new Object[]{retEntry.getValue(), retEntry.getKey()});

                            // Adjust on_hand so net stock reflects returns taken back
                            db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " + ? WHERE " + COLUMN_ID + " = ?",
                                    new Object[]{retEntry.getValue(), retEntry.getKey()});
                        }
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

    public long saveOrder(int customerId, String date, double total, double paid, double due, Map<Integer, Integer> quantities, Map<Integer, Double> prices) {
        return saveOrder(customerId, date, total, 0.0, total, paid, due, quantities, null, null, prices);
    }

    public Cursor getOrderReturns(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT r.*, i." + COLUMN_ITEM_NAME + " " +
                "FROM " + TABLE_ORDER_RETURNS + " r " +
                "JOIN " + TABLE_ITEMS + " i ON r." + COLUMN_RET_ITEM_ID + " = i." + COLUMN_ID + " " +
                "WHERE r." + COLUMN_RET_ORDER_ID + " = ?";
        return db.rawQuery(sql, new String[]{String.valueOf(orderId)});
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
            // First, restore old quantities to on_hand before deleting
            Cursor itemCursor = db.query(TABLE_ORDER_ITEMS, new String[]{COLUMN_OI_ITEM_ID, COLUMN_OI_QUANTITY},
                    COLUMN_OI_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)}, null, null, null);
            if (itemCursor != null) {
                while (itemCursor.moveToNext()) {
                    int itemId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(COLUMN_OI_ITEM_ID));
                    int oldQty = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(COLUMN_OI_QUANTITY));
                    db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " + ? WHERE " + COLUMN_ID + " = ?",
                            new Object[]{oldQty, itemId});
                }
                itemCursor.close();
            }

            db.delete(TABLE_ORDER_ITEMS, COLUMN_OI_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                if (entry.getValue() > 0) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(COLUMN_OI_ORDER_ID, orderId);
                    itemValues.put(COLUMN_OI_ITEM_ID, entry.getKey());
                    itemValues.put(COLUMN_OI_QUANTITY, entry.getValue());
                    itemValues.put(COLUMN_OI_PRICE, prices.get(entry.getKey()));
                    db.insert(TABLE_ORDER_ITEMS, null, itemValues);

                    // Deduct new quantities from on_hand
                    db.execSQL("UPDATE " + TABLE_ITEMS + " SET " + COLUMN_ON_HAND + " = " + COLUMN_ON_HAND + " - ? WHERE " + COLUMN_ID + " = ?",
                            new Object[]{entry.getValue(), entry.getKey()});
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
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        return getSalesStatsByDate(today, today);
    }

    public Cursor getSalesStatsByDate(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";
        String query = "SELECT " +
                "IFNULL(SUM(" + COLUMN_ORDER_TOTAL + "), 0) as total_revenue, " +
                "IFNULL(SUM(" + COLUMN_ORDER_GROSS + "), 0) as total_gross, " +
                "IFNULL(SUM(" + COLUMN_ORDER_RETURN + "), 0) as total_returns, " +
                "COUNT(" + COLUMN_ID + ") as total_bills, " +
                "IFNULL(SUM(" + COLUMN_ORDER_PAID + "), 0) as total_collected, " +
                "IFNULL(SUM(" + COLUMN_ORDER_DUE + "), 0) as total_due, " +
                "(SELECT COUNT(DISTINCT " + COLUMN_ORDER_CUSTOMER_ID + ") FROM " + TABLE_ORDERS + " WHERE " + COLUMN_ORDER_DATE + " >= ? AND " + COLUMN_ORDER_DATE + " <= ? AND " + COLUMN_ORDER_DUE + " > 0) as pending_stores, " +
                "(SELECT IFNULL(SUM(" + COLUMN_RET_QUANTITY + "), 0) FROM " + TABLE_ORDER_RETURNS + " WHERE " + COLUMN_RET_DATE + " >= ? AND " + COLUMN_RET_DATE + " <= ?) as total_return_units " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE " + COLUMN_ORDER_DATE + " >= ? AND " + COLUMN_ORDER_DATE + " <= ?";
        return db.rawQuery(query, new String[]{start, end, start, end, start, end});
    }

    public Cursor getDailySalesTrend(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";
        String query = "SELECT substr(" + COLUMN_ORDER_DATE + ", 1, 10) as sale_date, " +
                "IFNULL(SUM(" + COLUMN_ORDER_TOTAL + "), 0) as day_revenue, " +
                "COUNT(" + COLUMN_ID + ") as day_bills " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE " + COLUMN_ORDER_DATE + " >= ? AND " + COLUMN_ORDER_DATE + " <= ? " +
                "GROUP BY sale_date " +
                "ORDER BY sale_date ASC";
        return db.rawQuery(query, new String[]{start, end});
    }

    public Cursor getHourlySalesIntervals(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String start = date + " 00:00:00";
        String end = date + " 23:59:59";
        String query = "SELECT " +
                "CAST(substr(" + COLUMN_ORDER_DATE + ", 12, 2) AS INTEGER) as order_hour, " +
                "IFNULL(SUM(" + COLUMN_ORDER_TOTAL + "), 0) as slot_revenue, " +
                "COUNT(" + COLUMN_ID + ") as slot_bills " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE " + COLUMN_ORDER_DATE + " >= ? AND " + COLUMN_ORDER_DATE + " <= ? " +
                "GROUP BY order_hour " +
                "ORDER BY order_hour ASC";
        return db.rawQuery(query, new String[]{start, end});
    }

    public Cursor getItemWiseSalesByDate(String startDate, String endDate, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";

        String sql = "SELECT " +
                "i." + COLUMN_ID + ", " +
                "i." + COLUMN_ITEM_NAME + ", " +
                "i." + COLUMN_COST + ", " +
                "IFNULL(del.delivered_qty, 0) as delivered_qty, " +
                "IFNULL(ret.returned_qty, 0) as returned_qty, " +
                "(IFNULL(del.delivered_qty, 0) - IFNULL(ret.returned_qty, 0)) as total_qty, " +
                "(IFNULL(del.delivered_price, 0) - IFNULL(ret.returned_price, 0)) as total_price " +
                "FROM " + TABLE_ITEMS + " i " +
                "LEFT JOIN (" +
                "    SELECT oi." + COLUMN_OI_ITEM_ID + " as item_id, " +
                "           SUM(oi." + COLUMN_OI_QUANTITY + ") as delivered_qty, " +
                "           SUM(oi." + COLUMN_OI_QUANTITY + " * oi." + COLUMN_OI_PRICE + ") as delivered_price " +
                "    FROM " + TABLE_ORDER_ITEMS + " oi " +
                "    JOIN " + TABLE_ORDERS + " o ON oi." + COLUMN_OI_ORDER_ID + " = o." + COLUMN_ID + " " +
                "    WHERE o." + COLUMN_ORDER_DATE + " >= ? AND o." + COLUMN_ORDER_DATE + " <= ? " +
                "    GROUP BY oi." + COLUMN_OI_ITEM_ID + "" +
                ") del ON i." + COLUMN_ID + " = del.item_id " +
                "LEFT JOIN (" +
                "    SELECT r." + COLUMN_RET_ITEM_ID + " as item_id, " +
                "           SUM(r." + COLUMN_RET_QUANTITY + ") as returned_qty, " +
                "           SUM(r." + COLUMN_RET_QUANTITY + " * r." + COLUMN_RET_PRICE + ") as returned_price " +
                "    FROM " + TABLE_ORDER_RETURNS + " r " +
                "    WHERE r." + COLUMN_RET_DATE + " >= ? AND r." + COLUMN_RET_DATE + " <= ? " +
                "    GROUP BY r." + COLUMN_RET_ITEM_ID + "" +
                ") ret ON i." + COLUMN_ID + " = ret.item_id " +
                "WHERE (IFNULL(del.delivered_qty, 0) > 0 OR IFNULL(ret.returned_qty, 0) > 0) ";

        if (query != null && !query.trim().isEmpty()) {
            sql += "AND i." + COLUMN_ITEM_NAME + " LIKE ? ";
        }

        sql += "ORDER BY total_qty DESC";

        if (query != null && !query.trim().isEmpty()) {
            return db.rawQuery(sql, new String[]{start, end, start, end, "%" + query.trim() + "%"});
        }
        return db.rawQuery(sql, new String[]{start, end, start, end});
    }

    public Cursor getCustomerWiseSalesByDate(String startDate, String endDate, String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String start = startDate + " 00:00:00";
        String end = endDate + " 23:59:59";
        String sql = "SELECT o." + COLUMN_ID + " as order_id, o." + COLUMN_ORDER_DATE + ", " +
                "o." + COLUMN_ORDER_GROSS + ", o." + COLUMN_ORDER_RETURN + ", o." + COLUMN_ORDER_TOTAL + ", " +
                "o." + COLUMN_ORDER_PAID + ", o." + COLUMN_ORDER_DUE + ", " +
                "c." + COLUMN_ID + " as customer_id, c." + COLUMN_FIRST_NAME + ", c." + COLUMN_LAST_NAME + ", c." + COLUMN_STORE_NAME + ", c." + COLUMN_CONTACT + " " +
                "FROM " + TABLE_ORDERS + " o " +
                "JOIN " + TABLE_CUSTOMERS + " c ON o." + COLUMN_ORDER_CUSTOMER_ID + " = c." + COLUMN_ID + " " +
                "WHERE o." + COLUMN_ORDER_DATE + " >= ? AND o." + COLUMN_ORDER_DATE + " <= ? ";

        if (query != null && !query.trim().isEmpty()) {
            sql += "AND (c." + COLUMN_FIRST_NAME + " LIKE ? OR c." + COLUMN_LAST_NAME + " LIKE ? OR c." + COLUMN_STORE_NAME + " LIKE ?) ";
        }

        sql += "ORDER BY o." + COLUMN_ORDER_DATE + " DESC";

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim() + "%";
            return db.rawQuery(sql, new String[]{start, end, q, q, q});
        }
        return db.rawQuery(sql, new String[]{start, end});
    }

    public Cursor getItemWiseSales() {
        return getItemWiseSales("");
    }

    public Cursor getItemWiseSales(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT i." + COLUMN_ITEM_NAME + ", i." + COLUMN_COST + ", SUM(oi." + COLUMN_OI_QUANTITY + ") as total_qty, SUM(oi." + COLUMN_OI_QUANTITY + " * oi." + COLUMN_OI_PRICE + ") as total_price " +
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
        String query = "SELECT oi.*, i." + COLUMN_ITEM_NAME + ", i." + COLUMN_ON_HAND + " FROM " + TABLE_ORDER_ITEMS + " oi " +
                "JOIN " + TABLE_ITEMS + " i ON oi." + COLUMN_OI_ITEM_ID + " = i." + COLUMN_ID + " " +
                "WHERE oi." + COLUMN_OI_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    public Cursor getGRNHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT g.*, i." + COLUMN_ITEM_NAME + " FROM " + TABLE_GRN_LOG + " g " +
                "JOIN " + TABLE_ITEMS + " i ON g." + COLUMN_GRN_ITEM_ID + " = i." + COLUMN_ID + " " +
                "ORDER BY g." + COLUMN_ID + " DESC";
        return db.rawQuery(query, null);
    }
}