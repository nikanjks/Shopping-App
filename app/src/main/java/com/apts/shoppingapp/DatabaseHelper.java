package com.apts.shoppingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shopping_app.db";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String TABLE_USERS = "users";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);

        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT, " +
                        "price REAL NOT NULL, " +
                        "image_name TEXT)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_CART + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "product_id INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL CHECK(quantity > 0), " +
                        "UNIQUE(username, product_id), " +
                        "FOREIGN KEY(product_id) REFERENCES products(id))"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_ORDERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "order_date TEXT NOT NULL, " +
                        "total REAL NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "order_id INTEGER NOT NULL, " +
                        "product_id INTEGER NOT NULL, " +
                        "product_name TEXT NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "FOREIGN KEY(order_id) REFERENCES orders(id))"
        );

        insertProduct(db, "SOLMUX Advance",
                "Carbocisteine + Zinc 4 Film-Coated Tablets Pack",
                58.00, "prod_solmux");

        insertProduct(db, "CELETEQUE Facial Wash",
                "DermoScience Hydration Gentle Exfoliating Facial Wash 60ml",
                133.00, "prod_celeteque");

        insertProduct(db, "Nizoral Dandruff Shampoo",
                "ONE (1) SACHET ONLY (Contains 2% Ketoconazole) 6ml",
                80.00, "prod_nizoral");

        insertProduct(db, "Efficascent Oil",
                "Extra Strength 50ml (1 Bottle) Body Massage Oil",
                78.00, "prod_efficascent");
    }

    private void insertProduct(
            SQLiteDatabase db,
            String name,
            String description,
            double price,
            String imageName
    ) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("image_name", imageName);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // PRODUCTS

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                null,
                null,
                null,
                null,
                "id ASC",
                "4"
        )) {
            while (cursor.moveToNext()) {
                products.add(new Product(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
                ));
            }
        }

        return products;
    }

    // CART

    public boolean addToCart(String username, long productId, int quantity) {
        if (quantity < 1 || username == null || username.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        try (Cursor cursor = db.query(
                TABLE_PRODUCTS,
                new String[]{"id"},
                "id = ?",
                new String[]{String.valueOf(productId)},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) {
                return false;
            }
        }

        try (Cursor cursor = db.query(
                TABLE_CART,
                new String[]{"quantity"},
                "username = ? AND product_id = ?",
                new String[]{username, String.valueOf(productId)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                int currentQuantity = cursor.getInt(0);

                ContentValues values = new ContentValues();
                values.put("quantity", currentQuantity + quantity);

                return db.update(
                        TABLE_CART,
                        values,
                        "username = ? AND product_id = ?",
                        new String[]{username, String.valueOf(productId)}
                ) > 0;
            }
        }

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("product_id", productId);
        values.put("quantity", quantity);

        return db.insert(TABLE_CART, null, values) != -1;
    }

    public ArrayList<CartItem> getCartItems(String username) {
        ArrayList<CartItem> items = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return items;
        }
        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT p.id, p.name, p.description, p.price, " +
                        "p.image_name, c.quantity " +
                        "FROM cart c " +
                        "INNER JOIN products p ON c.product_id = p.id " +
                        "WHERE c.username = ? " +
                        "ORDER BY p.id ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            while (cursor.moveToNext()) {
                items.add(new CartItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
                ));
            }
        }

        return items;
    }

    public boolean updateCartQuantity(String username, long productId, int quantity) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (quantity <= 0) {
            return removeFromCart(username, productId);
        }

        ContentValues values = new ContentValues();
        values.put("quantity", quantity);

        return getWritableDatabase().update(
                TABLE_CART,
                values,
                "username = ? AND product_id = ?",
                new String[]{username, String.valueOf(productId)}
        ) > 0;
    }

    public boolean removeFromCart(String username, long productId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return getWritableDatabase().delete(
                TABLE_CART,
                "username = ? AND product_id = ?",
                new String[]{username, String.valueOf(productId)}
        ) > 0;
    }

    public double getCartTotal(String username) {
        double total = 0;
        if (username == null || username.trim().isEmpty()) {
            return total;
        }
        SQLiteDatabase db = getReadableDatabase();

        String query =
                "SELECT COALESCE(SUM(p.price * c.quantity), 0) AS total " +
                        "FROM cart c " +
                        "INNER JOIN products p ON c.product_id = p.id " +
                        "WHERE c.username = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        }

        return total;
    }

    // ORDERS

    public long checkout(String username) {
        if (username == null || username.trim().isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        long orderId = -1;

        db.beginTransaction();

        try {
            ArrayList<CartItem> items = getCartItems(username);

            if (items.isEmpty()) {
                return -1;
            }

            double total = 0;

            for (CartItem item : items) {
                total += item.getSubtotal();
            }

            String orderDate = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date());

            ContentValues orderValues = new ContentValues();
            orderValues.put("username", username);
            orderValues.put("order_date", orderDate);
            orderValues.put("total", total);

            orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId == -1) {
                return -1;
            }

            for (CartItem item : items) {
                ContentValues itemValues = new ContentValues();
                itemValues.put("order_id", orderId);
                itemValues.put("product_id", item.getProductId());
                itemValues.put("product_name", item.getName());
                itemValues.put("price", item.getPrice());
                itemValues.put("quantity", item.getQuantity());

                long result = db.insert(
                        TABLE_ORDER_ITEMS,
                        null,
                        itemValues
                );

                if (result == -1) {
                    return -1;
                }
            }

            db.delete(TABLE_CART, "username = ?", new String[]{username});
            db.setTransactionSuccessful();

        } catch (Exception e) {
            orderId = -1;
        } finally {
            db.endTransaction();
        }

        return orderId;
    }

    public ArrayList<OrderSummary> getOrders(String username) {
        ArrayList<OrderSummary> orders = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return orders;
        }
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_ORDERS,
                null,
                "username = ?",
                new String[]{username},
                null,
                null,
                "id ASC"
        )) {
            int orderNumber = 1;
            while (cursor.moveToNext()) {
                orders.add(new OrderSummary(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        orderNumber++,
                        cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
                ));
            }
        }

        Collections.reverse(orders);

        return orders;
    }

    public ArrayList<CartItem> getOrderItems(long orderId) {
        ArrayList<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_ORDER_ITEMS,
                null,
                "order_id = ?",
                new String[]{String.valueOf(orderId)},
                null,
                null,
                "id ASC"
        )) {
            while (cursor.moveToNext()) {
                items.add(new CartItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                        "",
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        ""
                ));
            }
        }

        return items;
    }

    // USERS (Login / Signup)

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT NOT NULL, " +
                        "address TEXT NOT NULL, " +
                        "phone TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "username TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                        "password_hash TEXT NOT NULL, " +
                        "salt TEXT NOT NULL)"
        );
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{"id"},
                "username = ? COLLATE NOCASE",
                new String[]{username},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    public boolean registerUser(String fullName, String address, String phone,
                                String email, String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);

        ContentValues values = new ContentValues();
        values.put("full_name", fullName);
        values.put("address", address);
        values.put("phone", phone);
        values.put("email", email);
        values.put("username", username);
        values.put("password_hash", passwordHash);
        values.put("salt", salt);

        return db.insert(TABLE_USERS, null, values) != -1;
    }

    /** Returns the user's full name if the login is correct, otherwise null. */
    public String loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{"full_name", "password_hash", "salt"},
                "username = ? COLLATE NOCASE",
                new String[]{username},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                String storedHash = cursor.getString(1);
                String salt = cursor.getString(2);

                if (storedHash.equals(hashPassword(password, salt))) {
                    return cursor.getString(0);
                }
            }
        }

        return null;
    }

    private static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    (salt + password).getBytes(StandardCharsets.UTF_8)
            );
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // UPGRADE

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        if (oldVersion < 2) {
            createUsersTable(db);
        }
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL(
                    "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "description TEXT, " +
                            "price REAL NOT NULL, " +
                            "image_name TEXT)"
            );
            insertProduct(db, "SOLMUX Advance",
                    "Carbocisteine + Zinc 4 Film-Coated Tablets Pack",
                    58.00, "prod_solmux");
            insertProduct(db, "CELETEQUE Facial Wash",
                    "DermoScience Hydration Gentle Exfoliating Facial Wash 60ml",
                    133.00, "prod_celeteque");
            insertProduct(db, "Nizoral Dandruff Shampoo",
                    "ONE (1) SACHET ONLY (Contains 2% Ketoconazole) 6ml",
                    80.00, "prod_nizoral");
            insertProduct(db, "Efficascent Oil",
                    "Extra Strength 50ml (1 Bottle) Body Massage Oil",
                    78.00, "prod_efficascent");
        }
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);

            db.execSQL(
                    "CREATE TABLE " + TABLE_CART + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT NOT NULL, " +
                            "product_id INTEGER NOT NULL, " +
                            "quantity INTEGER NOT NULL CHECK(quantity > 0), " +
                            "UNIQUE(username, product_id), " +
                            "FOREIGN KEY(product_id) REFERENCES products(id))"
            );

            db.execSQL(
                    "CREATE TABLE " + TABLE_ORDERS + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT NOT NULL, " +
                            "order_date TEXT NOT NULL, " +
                            "total REAL NOT NULL)"
            );

            db.execSQL(
                    "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "order_id INTEGER NOT NULL, " +
                            "product_id INTEGER NOT NULL, " +
                            "product_name TEXT NOT NULL, " +
                            "price REAL NOT NULL, " +
                            "quantity INTEGER NOT NULL, " +
                            "FOREIGN KEY(order_id) REFERENCES orders(id))"
            );
        }
    }
}
