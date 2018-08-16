package com.example.pete.inventoryapp.data;

import android.provider.BaseColumns;

public final class BooksContract {

    public BooksContract() {
    }

    public static final class BookEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_NAME = "productName";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplierName";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "supplierPhoneNumber";


    }
}
