package com.example.android.inventoryappstage2;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryappstage2.data.InventoryContract;

/**
 * Created by ChandaB on 10/4/2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super( context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from( context ).inflate( R.layout.list_item, parent, false );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Set textviews for the list item
        TextView productName = view.findViewById( R.id.productName );
        TextView price = view.findViewById( R.id.price );
        TextView quantity = view.findViewById( R.id.quantity );

        //Find the columns to display in the listview
        int productNameColumnIndex = cursor.getColumnIndex( InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME );
        int priceColumnIndex = cursor.getColumnIndex( InventoryContract.InventoryEntry.COLUMN_PRICE );
        int quantityColumnIndex = cursor.getColumnIndex( InventoryContract.InventoryEntry.COLUMN_QUANTITY );

        //Extract the cursor values
        String cursorProductName = cursor.getString( productNameColumnIndex );
        String cursorPrice = cursor.getString( priceColumnIndex );
        String cursorQuantity = cursor.getString( quantityColumnIndex );

        // If the price is empty or null, then show "price not found"
        // so the TextView isn't blank.
        if (TextUtils.isEmpty(cursorPrice)) {
            cursorPrice = context.getString(R.string.unknown_price);
        }

        //set text at current cursor
        productName.setText( cursorProductName );
        price.setText( cursorPrice );
        quantity.setText( cursorQuantity );

    }
}
