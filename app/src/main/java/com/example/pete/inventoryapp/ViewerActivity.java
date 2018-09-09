package com.example.pete.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pete.inventoryapp.data.BookContract;

import java.text.NumberFormat;

import static android.widget.Toast.LENGTH_SHORT;

public class ViewerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //region constants, instance variables, and View references

    // Identifier for the data loader
    private static final int EXISTING_BOOK_LOADER = 0;

    // Content URI for the existing book (null if it's a new book)
    private Uri currentBookUri;

    // View references: TextViews
    private TextView viewerTextViewName;
    private TextView viewerTextViewPrice;
    private TextView viewerTextViewQuantity;
    private TextView viewerTextViewSupplierName;
    private TextView viewerTextViewSupplierPhone;

    // View references: Buttons
    private FloatingActionButton viewer_fab;
    private Button buttonPlus;
    private Button buttonMinus;
    private Button buttonContact;

    //endregion constants, instance variables, and View references

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        // Set the Activity title
        setTitle(getString(R.string.title_viewer_activity));

        // Get references to the Views in activity_editor.xml
        getViewReferences();

        // Receive the intent
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        // Set button Click Listeners
        setButtonListeners();

        // Initialize a loader to read the current data from the database
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    // Get references to the Views in activity_editor.xml
    private void getViewReferences() {
        viewerTextViewName = findViewById(R.id.viewerTextViewName);
        viewerTextViewPrice = findViewById(R.id.viewerTextViewPrice);
        viewerTextViewQuantity = findViewById(R.id.viewerTextViewQuantity);
        viewerTextViewSupplierName = findViewById(R.id.viewerTextViewSupplierName);
        viewerTextViewSupplierPhone = findViewById(R.id.viewerTextViewSupplierPhone);

        viewer_fab = findViewById(R.id.viewer_fab);
        buttonPlus = findViewById(R.id.buttonPlus);
        buttonMinus = findViewById(R.id.buttonMinus);
        buttonContact = findViewById(R.id.buttonContact);
    }

    // Set button click listeners for Plus, Minus, and Contact in the Viewer Activity
    private void setButtonListeners() {
        // FloatingActionButton
        viewer_fab.setOnClickListener(v -> {
            // Create new intent to go to {@link ViewerActivity}
            Intent intent = new Intent(ViewerActivity.this, EditorActivity.class);

            // Set the URI on the data field of the intent
            intent.setData(currentBookUri);

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent);
        });

        // Plus Quantity
        buttonPlus.setOnClickListener(v -> {
            // Add 1 to the current quantity
            modifyQuantity(1);
        });

        // Minus Quantity
        buttonMinus.setOnClickListener(v -> {
            // Subtract 1 from the current quantity
            modifyQuantity(-1);
        });

        // Contact
        buttonContact.setOnClickListener(v -> {
            // Create a projection to get the phone number
            String[] projection = {
                    BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

            // Get a cursor for the current book
            Cursor cursor = getContentResolver().query(currentBookUri,
                    projection,
                    null,
                    null,
                    null);

            // This is needed before accessing the cursor's data, similar to its use in onLoadFinished()
            cursor.moveToFirst();

            // Get the column index for the supplier phone number
            int columnIndexSupplierPhone = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Create a URI from the phone number
            Uri uri = Uri.parse("tel:" + cursor.getString(columnIndexSupplierPhone));

            // Create an intent to call the phone number
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);

            // Start the intent
            startActivity(intent);

            // Close the cursor we just created
            cursor.close();
        });
    }

    /**
     * Helper method to modify quantity.
     *
     * @param amount The value to add to the current quantity (can be +/- 1)
     */
    private void modifyQuantity(int amount) {
        // Create a projection to get the quantity of this book
        String[] projection = {
                BookContract.BookEntry.COLUMN_QUANTITY};

        // Get a cursor for the current book
        Cursor cursor = getContentResolver().query(currentBookUri,
                projection,
                null,
                null,
                null);

        // This is needed before accessing the cursor's data, similar to its use in onLoadFinished()
        cursor.moveToFirst();

        // Get the column index of the quantity
        int columnIndexQuantity = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);

        // Retrieve the quantity amount
        int quantity = cursor.getInt(columnIndexQuantity);

        // Close the cursor we just created
        // (We're done with it after this point, and below are chances for this method to return)
        cursor.close();

        // Do not let the quantity go below zero
        if (quantity + amount >= 0) {
            quantity += amount;
        } else {
            Toast.makeText(this, "Quantity is already 0.", LENGTH_SHORT).show();
            return;
        }

        // Build a content values object
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, quantity);

        // Update the entry
        int rowsAffected = this.getContentResolver().update(currentBookUri, values, null, null);

        // Show a toast message reporting the result of this operation
        if (rowsAffected == 0) {
            // There was an error if no rows were affected
            Toast.makeText(this, "Error reducing quantity.", LENGTH_SHORT).show();
        }
    }

    /**
     * Delete this entry.
     */
    private void deleteEntry() {
        // Only perform the delete if this is an existing book.
        if (currentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the currentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error deleting book", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Book deleted", Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    //region options menu overrides and methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor_options.xml file.
        getMenuInflater().inflate(R.menu.menu_view_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_entry:
                // Delete button (options menu)
                showDeleteConfirmationDialog();
                break;
            case android.R.id.home:
                // Navigating back
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion options menu overrides and methods

    //region confirmation dialogs (discard unsaved changes, delete entry)

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message_prompt);
        builder.setPositiveButton(R.string.delete_dialog_delete, (dialog, id) -> {
            // User clicked the "Delete" button, so delete the entry.
            deleteEntry();
        });
        builder.setNegativeButton(R.string.delete_dialog_cancel, (dialog, id) -> {
            // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the entry.
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //endregion confirmation dialogs (discard unsaved changes, delete entry)

    //region method overrides for LoaderManager.LoaderCallbacks<Cursor>

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_NAME,
                BookContract.BookEntry.COLUMN_PRICE,
                BookContract.BookEntry.COLUMN_QUANTITY,
                BookContract.BookEntry.COLUMN_SUPPLIER_NAME,
                BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentBookUri,          // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (There should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Get column indices of book attributes
            int columnIndexName = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_NAME);
            int columnIndexPrice = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
            int columnIndexQuantity = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
            int columnIndexSupplierName = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_NAME);
            int columnIndexSupplierPhone = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract values from the Cursor
            String name = cursor.getString(columnIndexName);
            Double price = cursor.getDouble(columnIndexPrice);
            int quantity = cursor.getInt(columnIndexQuantity);
            String supplierName = cursor.getString(columnIndexSupplierName);
            String supplierPhone = cursor.getString(columnIndexSupplierPhone);

            // Create a formatter for the price
            NumberFormat format = NumberFormat.getCurrencyInstance();

            // Hide the Contact supplier button if there is no supplier phone number
            if (supplierPhone.equals("")) {
                buttonContact.setVisibility(View.GONE);
            }

            // For optional fields, display no data text instead of nothing
            if (supplierName.equals("")) {
                supplierName = getString(R.string.viewer_no_data_text);
            }

            if (supplierPhone.equals("")) {
                supplierPhone = getString(R.string.viewer_no_data_text);
            }

            // Bind data to the Views
            viewerTextViewName.setText(name);
            viewerTextViewPrice.setText(format.format(price));
            viewerTextViewQuantity.setText(String.valueOf(quantity));
            viewerTextViewSupplierName.setText(supplierName);
            viewerTextViewSupplierPhone.setText(supplierPhone);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear text from all the EditTexts
        viewerTextViewName.setText("");
        viewerTextViewPrice.setText("");
        viewerTextViewQuantity.setText("");
        viewerTextViewSupplierName.setText("");
        viewerTextViewSupplierPhone.setText("");
    }

    //endregion method overrides for LoaderManager.LoaderCallbacks<Cursor>

}
