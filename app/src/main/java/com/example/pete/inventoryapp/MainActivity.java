package com.example.pete.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pete.inventoryapp.data.BooksContract.BookEntry;
import com.example.pete.inventoryapp.data.BooksDbHelper;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final String spacer = " - ";

    // View references
    private TextView displayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get View references
        displayView = findViewById(R.id.textView1);

        // Insert dummy data into the database
        insertDummyData();

        // Display database info
        displayDatabaseInfo();
    }

    // Display information from the database
    private void displayDatabaseInfo() {
        // Instantiate BooksDbHelper, thereby executing the SQL_CREATE_TABLE command
        BooksDbHelper booksDbHelper = new BooksDbHelper(this);

        // "Create and/or open a database to read from it"
        SQLiteDatabase db = booksDbHelper.getReadableDatabase();

        // Build arguments for a db.query call
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // Create a cursor with db.query
        Cursor cursor = db.query(
                BookEntry.TABLE_NAME,
                projection,
                null, null, null, null, null);

        // Display info about the database in a TextView
        try {
            // Display the number of rows in the Cursor
            displayView.setText("The " + BookEntry.TABLE_NAME + " table contains " + cursor.getCount() + " books.\n\n");

            // Display column headers
            displayView.append(BookEntry._ID + spacer +
                    BookEntry.COLUMN_NAME + spacer +
                    BookEntry.COLUMN_PRICE + spacer +
                    BookEntry.COLUMN_QUANTITY + spacer +
                    BookEntry.COLUMN_SUPPLIER_NAME + spacer +
                    BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // .getString automatically converts the columns that aren't stored as Strings,
                // so I use it for every column
                displayView.append("\n" +
                        cursor.getString(cursor.getColumnIndex(BookEntry._ID)) + spacer +
                        cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_NAME)) + spacer +
                        "$" + cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRICE)) + spacer +
                        cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY)) + spacer +
                        cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME)) + spacer +
                        cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER)));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    /*
    Insert dummy data into the database.
    This creates 5 rows at a time which persist across app sessions
    Running the app multiple times will keep generating more data */
    private void insertDummyData() {
        // Get the dbHelper and the SQLiteDatabase objects
        BooksDbHelper booksDbHelper = new BooksDbHelper(this);
        SQLiteDatabase db = booksDbHelper.getWritableDatabase();

        // Get the index of the last row in the database
        int lastRowNumber = getLastRowNumber();

        // Create a range of data
        for (int i = 1; i < 6; i++) {
            // Build a ContentValues object
            ContentValues values = new ContentValues();
            // String
            values.put(BookEntry.COLUMN_NAME, "Book " + String.valueOf(lastRowNumber + i));
            // double
            values.put(BookEntry.COLUMN_PRICE, lastRowNumber + i * 10);
            // int
            values.put(BookEntry.COLUMN_QUANTITY, lastRowNumber + i);
            // String
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Supplier " + String.valueOf(lastRowNumber + i));
            // String
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                    String.valueOf(100 + lastRowNumber + i) + "-" + String.valueOf(1000 + lastRowNumber + i));

            // Insert row into database
            db.insert(BookEntry.TABLE_NAME, null, values);
        }
    }

    // Return the highest row number (ID), aka the number of rows in the database
    private int getLastRowNumber() {
        // Get the dbHelper and the SQLiteDatabase objects
        BooksDbHelper booksDbHelper = new BooksDbHelper(this);
        SQLiteDatabase db = booksDbHelper.getWritableDatabase();

        // Create a cursor with db.query
        Cursor cursor = db.query(
                BookEntry.TABLE_NAME,
                null, null, null, null, null, null);

        int totalRows = cursor.getCount();

        cursor.close();

        return totalRows;
    }

    /*
    Delete the inventory table.
    This can be used to get rid of rows created in earlier sessions. */
    private void deleteTable() {
        // Get the dbHelper and the SQLiteDatabase objects
        BooksDbHelper booksDbHelper = new BooksDbHelper(this);
        SQLiteDatabase db = booksDbHelper.getWritableDatabase();

        db.delete(BookEntry.TABLE_NAME, null, null);
    }

}
