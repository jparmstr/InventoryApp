package com.example.pete.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pete.inventoryapp.data.BookContract.BookEntry;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //region constants, instance variables, and View references

    // Do not allow any null values at all, which makes the app more annoying to use,
    // but strictly conforms to the project rubric
    private static final boolean STRICTLY_FOLLOW_THE_RUBRIC = true;

    // Identifier for the data loader
    private static final int EXISTING_BOOK_LOADER = 0;

    // Content URI for the existing book (null if it's a new book)
    private Uri currentBookUri;

    // Keep track of whether this entry has been changed or not
    private boolean entryHasChanged = false;

    // View references
    private EditText editTextName;
    private EditText editTextPrice;
    private EditText editTextQuantity;
    private EditText editTextSupplierName;
    private EditText editTextSupplierPhone;
    private BottomNavigationView editorBottomNavigationView;

    //endregion constants, instance variables, and View references

    //region Listener objects

    // Listener that sets entryHasChanged = true when a user touches an EditText View
    private final View.OnTouchListener onTouchListener = (v, event) -> {
        // Call the performClick method if necessary
        // (addresses a warning from Android Studio)
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.performClick();
                break;
            case MotionEvent.ACTION_UP:
                v.performClick();
                break;
        }

        entryHasChanged = true;
        return false;
    };

    private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save button (bottom navigation bar)
                saveEntry();
                break;
            case R.id.action_cancel:
                // Cancel button (bottom navigation bar)
                if (!entryHasChanged) {
                    // If the entry hasn't changed, navigate up to the parent activity
//                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    finish();
                    return true;
                }

                cancel();
                return true;
        }

        return true;
    };

    //endregion Listener objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Get references to the Views in activity_editor.xml
        getViewReferences();

        // Receive the intent
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        if (currentBookUri == null) {
            // Creating a new entry
            setTitle(getString(R.string.title_editor_activity_new));

            // Hide the Delete MenuItem
            // This is achieved by forcing onPrepareOptionsMenu to be called
            //   which in turn hides the action_delete_entry MenuItem specifically
            invalidateOptionsMenu();
        } else {
            // Editing an existing entry
            setTitle(getString(R.string.title_editor_activity_edit));

            // Initialize a loader to read the current data from the database
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Set on touch listeners on all the EditText views
        setOnTouchListeners();

        // Set navigation item selected listener on the bottom navigation view
        editorBottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // No fields are optional if strictly following the project rubric
        if (STRICTLY_FOLLOW_THE_RUBRIC) {
            editTextSupplierName.setHint("");
            editTextSupplierPhone.setHint("");
        }
    }

    // Get references to the Views in activity_editor.xml
    private void getViewReferences() {
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        editTextSupplierName = findViewById(R.id.editTextSupplierName);
        editTextSupplierPhone = findViewById(R.id.editTextSupplierPhone);

        editorBottomNavigationView = findViewById(R.id.editorBottomNavigationView);
    }

    // Set on touch listeners on all the EditText views
    private void setOnTouchListeners() {
        editTextName.setOnTouchListener(onTouchListener);
        editTextPrice.setOnTouchListener(onTouchListener);
        editTextQuantity.setOnTouchListener(onTouchListener);
        editTextSupplierName.setOnTouchListener(onTouchListener);
        editTextSupplierPhone.setOnTouchListener(onTouchListener);
    }

    // Save this entry into the database (create or update)
    private void saveEntry() {
        // Read from the EditTexts
        // Use trim to eliminate leading or trailing white space
        // Only convert numeric values if the EditText fields are not empty
        String name = editTextName.getText().toString().trim();
        Double price = 0.0;
        if (!TextUtils.isEmpty(editTextPrice.getText().toString().trim())) {
            price = Double.parseDouble(editTextPrice.getText().toString().trim());
        }
        Integer quantity = 0;
        if (!TextUtils.isEmpty(editTextQuantity.getText().toString().trim())) {
            quantity = Integer.parseInt(editTextQuantity.getText().toString().trim());
        }
        String supplierName = editTextSupplierName.getText().toString().trim();
        String supplierPhone = editTextSupplierPhone.getText().toString().trim();

        // Exit if this is a new entry and no data was provided
        if (currentBookUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone) && price == 0.0 &&
                quantity == 0) {
            return;
        }

        // Validate information:
        // Validate the name
        if (name.equals("")) {
            Toast.makeText(this, "Book name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the price
        if (price == 0.0) {
            Toast.makeText(this, "Please enter a price.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the quantity
        if (quantity == 0) {
            Toast.makeText(this, "Please enter a quantity.", Toast.LENGTH_SHORT).show();
            return;
        }

        // No fields are optional if strictly following the project rubric
        if (STRICTLY_FOLLOW_THE_RUBRIC) {
            // Validate supplier name
            if (supplierName.equals("")) {
                Toast.makeText(this, "Supplier name required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate supplier phone
            if (supplierPhone.equals("")) {
                Toast.makeText(this, "Supplier phone number required.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create a ContentValues object
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_NAME, name);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhone);

        // Determine if this is a new or existing entry
        if (currentBookUri == null) {
            // This is a new entry

            // Insert the new entry into the database
            Uri uri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message reporting the result of this operation
            if (uri == null) {
                // There was an error inserting this entry
                Toast.makeText(this, "Error saving new book.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // The insert operation was successful
                Toast.makeText(this, "New book saved.",
                        Toast.LENGTH_SHORT).show();

                // Exit this activity
                finish();
            }
        } else {
            // This is an existing entry

            // Update the entry
            int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);

            // Show a toast message reporting the result of this operation
            if (rowsAffected == 0) {
                // There was an error if no rows were affected
                Toast.makeText(this, "Error updating the book.", Toast.LENGTH_SHORT).show();
            } else {
                // The update operation was successful
                Toast.makeText(this, "Book updated.", Toast.LENGTH_SHORT).show();

                // Exit this activity
                finish();
            }
        }
    }

    // Delete this entry
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

        // Go back to the MainActivity
        Intent showMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(showMainActivity);
    }

    //region options menu overrides and methods

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor_options.xml file.
        getMenuInflater().inflate(R.menu.menu_editor_options, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new entry, hide the "Delete" item
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_entry);
            menuItem.setVisible(false);
        }

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
                if (!entryHasChanged) {
                    // If the entry hasn't changed, navigate up to the parent activity
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                cancel();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!entryHasChanged) {
            // If the entry hasn't changed, navigate up to the parent activity
            super.onBackPressed();
            return;
        }

        cancel();
    }

    // Used for back navigation and Cancel button
    // Creates a click listener for the Discard button
    //  and shows the unsaved changes dialog
    private void cancel() {
        // If there are unsaved changes, present a dialog to warn the user
        // about saving changes
        DialogInterface.OnClickListener discardButtonClickListener =
                (dialogInterface, i) -> {
                    // User clicked "Discard" button, navigate to parent activity.
//                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    finish();
                };


        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //endregion options menu overrides and methods

    //region confirmation dialogs (discard unsaved changes, delete entry)

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_message_prompt);
        builder.setPositiveButton(R.string.unsaved_changes_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.unsaved_changes_keep_editing, (dialog, id) -> {
            // User clicked the "Keep editing" button, so dismiss the dialog
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

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
                BookEntry._ID,
                BookEntry.COLUMN_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

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
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Get column indices of book attributes
            int columnIndexName = cursor.getColumnIndex(BookEntry.COLUMN_NAME);
            int columnIndexPrice = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int columnIndexQuantity = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int columnIndexSupplierName = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int columnIndexSupplierPhone = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            // Extract values from the Cursor
            String name = cursor.getString(columnIndexName);
            Double price = cursor.getDouble(columnIndexPrice);
            int quantity = cursor.getInt(columnIndexQuantity);
            String supplierName = cursor.getString(columnIndexSupplierName);
            String supplierPhone = cursor.getString(columnIndexSupplierPhone);

            // Bind data to the Views
            editTextName.setText(name);
            editTextPrice.setText(String.valueOf(price));
            editTextQuantity.setText(String.valueOf(quantity));
            editTextSupplierName.setText(supplierName);
            editTextSupplierPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear text from all the EditTexts
        editTextName.setText("");
        editTextPrice.setText("");
        editTextQuantity.setText("");
        editTextSupplierName.setText("");
        editTextSupplierPhone.setText("");
    }

    //endregion method overrides for LoaderManager.LoaderCallbacks<Cursor>

}
