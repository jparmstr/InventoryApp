package com.example.pete.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.pete.inventoryapp.data.BookContract.BookEntry;

class BookDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = BookDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This String contains the SQL command to create the inventory table
        String SQL_CREATE_TABLE =
                "CREATE TABLE " + BookEntry.TABLE_NAME + "("
                        + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + BookEntry.COLUMN_NAME + " TEXT NOT NULL, "
                        + BookEntry.COLUMN_PRICE + " REAL NOT NULL, "
                        + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, "
                        + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT DEFAULT '', "
                        + BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT DEFAULT '');";

        // Log the CREATE TABLE command for debug purposes
        Log.d(LOG_TAG, SQL_CREATE_TABLE);

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            Log.d("onUpgrade", "Database versions do not match");
        }
    }
}
