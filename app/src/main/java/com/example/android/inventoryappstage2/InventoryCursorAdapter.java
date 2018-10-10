package com.example.android.inventoryappstage2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

/**
 * Created by ChandaB on 10/4/2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    //Global context variable
    Context thisContext;

    //Constructor
    public InventoryCursorAdapter(Context context, Cursor c) {
        super( context, c, 0 );
    }

    /**
     * Inflate the view
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from( context ).inflate( R.layout.list_item, parent, false );
    }

    /**
     * Bind the view with the data
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        thisContext = context;

        //Set textviews for the list item
        TextView productName = view.findViewById( R.id.productName );
        TextView mediaType = view.findViewById( R.id.mediaType );
        TextView price = view.findViewById( R.id.price );
        TextView price_label = view.findViewById( R.id.price_label );
        final TextView quantity = view.findViewById( R.id.quantity );
        Button saleButton = view.findViewById( R.id.sale_button );

        //Make sure the view is visible on load of new item
        price_label.setVisibility( View.VISIBLE );

        //Find the columns to display in the listview
        int idColumnIndex = cursor.getColumnIndex( InventoryEntry._ID );
        int productNameColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_NAME );
        int mediaTypeColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_MEDIA_TYPE );
        int priceColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRICE );
        int quantityColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_QUANTITY );

        //Extract the cursor values
        final String id = cursor.getString( idColumnIndex );
        String cursorProductName = cursor.getString( productNameColumnIndex );
        int cursorMediaType = cursor.getInt( mediaTypeColumnIndex );
        String cursorPrice = cursor.getString( priceColumnIndex );
        double listViewPrice = cursor.getDouble( priceColumnIndex );
        final int cursorQuantity = cursor.getInt( quantityColumnIndex );

        //Determine media type string to display
        switch (cursorMediaType) {
            case 1:
                mediaType.setText( "Hardcover" );
                break;
            case 2:
                mediaType.setText( "Paperback" );
                break;
            case 3:
                mediaType.setText( "Audiobook" );
                break;
            case 4:
                mediaType.setText( "Kindle" );
                break;
            case 5:
                mediaType.setText( "Epub" );
                break;
            case 6:
                mediaType.setText( "MP3" );
                break;
        }

        // If the price is empty or null, then show "price not found"
        // so the TextView isn't blank.
        if (TextUtils.isEmpty( cursorPrice )) {
            price_label.setVisibility( View.GONE );
            cursorPrice = context.getString( R.string.unknown_price );
        } else if (listViewPrice == 0.0) {
            price_label.setVisibility( View.GONE );
            cursorPrice = "FREE";
        }

        //set the textvalues for the list item
        productName.setText( cursorProductName );
        price.setText( cursorPrice );
        quantity.setText( Integer.toString( cursorQuantity ) );


        //set and onclick listener to trigger quantity update when clicked
        saleButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuantity( cursorQuantity, id, context );
            }
        } );
    }

    /**
     * Method to calculate and update the quantity in the database when the report sale button is clicked.
     * @param quantityForCalc
     * @param id
     * @param context
     */
    private void updateQuantity(int quantityForCalc, String id, Context context) {
        if (quantityForCalc == 0) {
            Toast.makeText( context, R.string.editor_update_inventory_less_than_zero, Toast.LENGTH_SHORT ).show();
            return;
        } else {
            Log.d( "BeforeQtyForCalc", "Before Calculation " + quantityForCalc );
            //Deducting one from inventory (quantity)
            quantityForCalc = quantityForCalc - 1;
            Log.d( "AfterQtyForCalc", "After Calculation " + quantityForCalc );

            //Setting up DB stuff to update the new quantity in the database
            ContentValues values = new ContentValues();
            values.put( InventoryEntry.COLUMN_QUANTITY, quantityForCalc );
            Uri currentInventoryItemUri = Uri.withAppendedPath( InventoryEntry.CONTENT_URI, id );
            Log.d( "CURRENTURI", "URI : " + currentInventoryItemUri );
            thisContext.getContentResolver().update( currentInventoryItemUri, values, null, null );
        }
    }
}