package com.example.pete.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pete.inventoryapp.data.BookContract.BookEntry;

import java.text.NumberFormat;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    // View references
    TextView textViewName;
    TextView textViewPrice;
    TextView textViewQuantity;

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get references to the Views of list_item.xml
        textViewName = view.findViewById(R.id.textViewName);
        textViewPrice = view.findViewById(R.id.textViewPrice);
        textViewQuantity = view.findViewById(R.id.textViewQuantity);

        // Find the column indices of the attributes
        int columnIndexName = cursor.getColumnIndex(BookEntry.COLUMN_NAME);
        int columnIndexPrice = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int columnIndexQuantity = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Read the attributes from the Cursor for the current item
        String name = cursor.getString(columnIndexName);
        Double price = cursor.getDouble(columnIndexPrice);
        Integer quantity = cursor.getInt(columnIndexQuantity);

        // Create a formatter for the price
        NumberFormat format = NumberFormat.getCurrencyInstance();

        // Set the TextView Texts
        textViewName.setText(name);
        textViewPrice.setText(format.format(price));
        textViewQuantity.setText(context.getResources().getString(R.string.quantityString, quantity));

        // Save the cursor position so that the Sale button knows which item was clicked
        final int position = cursor.getPosition();

        // Get the Sale button and set its onClickListener
        Button saleButton = view.findViewById(R.id.buttonSale);

        saleButton.setOnClickListener(v -> {
            // Restore the cursor position relative to the clicked item
            cursor.moveToPosition(position);

            // Get the current quantity (for this item, referred to by cursor)
            int thisQuantity = cursor.getInt(columnIndexQuantity);

            // Decrement the quantity value, with a floor of zero
            if (thisQuantity > 0) {
                thisQuantity--;
            } else {
                Toast.makeText(context, "Quantity is already at 0.", LENGTH_SHORT).show();
                return;
            }

            // Build a content values object
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_QUANTITY, thisQuantity);

            // Get the column index for id
            int columnIndexID = cursor.getColumnIndex(BookEntry._ID);

            // Get the current book's id
            int id = cursor.getInt(columnIndexID);

            // Build a URI for this book entry
            Uri uri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

            // Update the entry
            int rowsAffected = context.getContentResolver().update(uri, values, null, null);

            // Show a toast message reporting the result of this operation
            if (rowsAffected == 0) {
                // There was an error if no rows were affected
                Toast.makeText(context, "Error reducing quantity.", LENGTH_SHORT).show();
            }
        });
    }

}