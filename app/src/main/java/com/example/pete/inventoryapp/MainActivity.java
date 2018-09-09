package com.example.pete.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.example.pete.inventoryapp.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //region constants, instance variables, and View references

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BOOK_LOADER = 0;
    BookCursorAdapter bookCursorAdapter;

    // View references
    ListView listView;
    RelativeLayout emptyView;
    ImageView emptyImage;
    TextView emptyTitle;
    TextView emptySubtitle;
    FloatingActionButton fab;

    //endregion constants, instance variables, and View references

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the Views in activity_main.xml
        getViewReferences();

        // Set the Activity title
        setTitle(getString(R.string.title_main_activity));

        // Add onClickListener for the FloatingActionButton
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditorActivity.class)));

        // Set the ListView's empty view
        listView.setEmptyView(emptyView);

        // Create an adapter for the ListView
        // (there is no data until the loader finishes, so pass in null for the Cursor)
        bookCursorAdapter = new BookCursorAdapter(this, null);
        listView.setAdapter(bookCursorAdapter);

        // Set the ListView's item click listener
        // (open the clicked item with the ViewerActivity)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Create new intent to go to {@link ViewerActivity}
            Intent intent = new Intent(MainActivity.this, ViewerActivity.class);

            // Form the content URI that represents the specific book that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // {@link BookEntry#CONTENT_URI}.
            // For example, the URI would be "content://com.example.pete.inventoryapp/books/2"
            // if the pet with ID 2 was clicked on.
            Uri uri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

            // Set the URI on the data field of the intent
            intent.setData(uri);

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent);
        });

        // Add a footer view to the listView so that the FloatingActionButton
        // doesn't block the Sale button on the last listView item
        Space space = new Space(listView.getContext());
        space.setMinimumHeight((int)(75 * Resources.getSystem().getDisplayMetrics().density));
        listView.addFooterView(space);

        // Initialize the Loader
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    // Get references to the Views in activity_main.xml
    private void getViewReferences() {
        listView = findViewById(R.id.listView);
        emptyView = findViewById(R.id.empty_view);
        emptyImage = findViewById(R.id.empty_image);
        emptyTitle = findViewById(R.id.empty_title_text);
        emptySubtitle = findViewById(R.id.empty_subtitle_text);
        fab = findViewById(R.id.fab);
    }

    //region overrides for LoaderManager.LoaderCallbacks<Cursor>

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        // (I'm excluding supplier name and phone number since they're not displayed in the
        //  MainActivity)
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                BookEntry.CONTENT_URI,          // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated data
        bookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        bookCursorAdapter.swapCursor(null);
    }

    //endregion overrides for LoaderManager.LoaderCallbacks<Cursor>

    //region options menu overrides and methods

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllEntries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_options.xmlions.xml file.
        getMenuInflater().inflate(R.menu.menu_main_options, menu);
        return true;
    }

    /*
    Insert some hardcoded data for test purposes
    */
    private void insertDummyData() {
        insertDummyData_item("Harry Potter and The Philosopher's Stone", 8.87,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Chamber of Secrets", 10.58,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Prisoner of Azkaban", 9.74,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Goblet of Fire", 12.51,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Order of the Phoenix", 11.19,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Half Blood Prince", 9.94,
                10, "Barnes and Noble", "+1 (919) 555-1234");
        insertDummyData_item("Harry Potter and The Deathly Hallows", 14.43,
                10, "Barnes and Noble", "+1 (919) 555-1234");
    }

    /*
     * Insert one hardcoded item
     */
    private void insertDummyData_item(String name, Double price, int quantity,
                                      String supplierName, String supplierPhone) {
        // Create the content values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_NAME, name);
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhone);

        // Insert a new row into the provider using the ContentResolver.
        // Use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        // into the books database table.
        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    /*
    Delete all database entries
    */
    private void deleteAllEntries() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from the database by deleteAllEntries");
    }

    //endregion options menu overrides and methods
}
