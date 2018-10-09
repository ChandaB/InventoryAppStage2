package com.example.android.inventoryappstage2;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

/**
 * Created by ChandaB on 10/7/2018.
 */


public class ViewerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * TextView field to enter the product name
     */
    private TextView productNameTextView;
    /**
     * Spinner to enter the media type
     */
    private TextView mediaTypeTextView;
    /**
     * TextView field to enter the price
     */
    private TextView priceTextView;
    /**
     * TextView field to enter the quantity
     */
    private TextView quantityTextView;
    /**
     * TextView field to enter the supplier name
     */
    private TextView supplierNameTextView;
    /**
     * TextView field to enter the supplier phone number
     */
    private TextView supplierPhoneTextView;
    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri currentInventoryItemUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_viewer );

        //Instantiate the new intent for this pet
        Intent intent = getIntent();
        currentInventoryItemUri = intent.getData();
        Log.d( "VIEWERACTCURRENTINVURI", "Current URI = " + currentInventoryItemUri );

        // Find all relevant views that we will need to read user input from
        productNameTextView = findViewById( R.id.view_product_name );
        mediaTypeTextView = findViewById( R.id.view_media_type );
        priceTextView = findViewById( R.id.view_price );
        quantityTextView = findViewById( R.id.view_quantity );
        supplierNameTextView = findViewById( R.id.view_supplier_name );
        supplierPhoneTextView = findViewById( R.id.view_phone );

        getLoaderManager().initLoader(0, null, this            );

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewerActivity.this, EditorActivity.class);

                //Form the URL by appending the selected pet's ID to the base URI
                Uri currentInventoryUri = currentInventoryItemUri;

                Log.d("CAPTURINGURIONITEMCLICK" , "URI: " + currentInventoryUri );

                //Set the URI on the data field of the intent
                intent.setData( currentInventoryUri );

                //Launce the editor activity
                startActivity( intent );

                startActivity(intent);
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_MEDIA_TYPE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE
        };

        //Log the current URL being passed to the cursorloader
        Log.d( "CurrentInventoryItemUri", "OnCreateLoader Method currentInventoryItemURI " + currentInventoryItemUri );

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader( this,   // Parent activity context
                currentInventoryItemUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null );                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int productNameColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_NAME );
            int mediaTypeColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_MEDIA_TYPE );
            int priceColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_QUANTITY );
            int supplierNameColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_SUPPLIER_NAME );
            int supplierPhoneColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_SUPPLIER_PHONE );

            // Extract out the value from the Cursor for the given column index

            String productName = cursor.getString( productNameColumnIndex );
            int mediaType = cursor.getInt( mediaTypeColumnIndex );
            double price = cursor.getDouble( priceColumnIndex );
            String priceString = "Free";
            int quantity = cursor.getInt( quantityColumnIndex );
            String supplierName = cursor.getString( supplierNameColumnIndex );
            String supplierPhone = cursor.getString( supplierPhoneColumnIndex );

            //Determine media type string to display
            switch (mediaType) {
                case 1:
                    mediaTypeTextView.setText( "Hardcover" );
                    break;
                case 2:
                    mediaTypeTextView.setText( "Paperback" );
                    break;
                case 3:
                    mediaTypeTextView.setText( "Audiobook" );
                    break;
                case 4:
                    mediaTypeTextView.setText( "Kindle" );
                    break;
                case 5:
                    mediaTypeTextView.setText( "Epub" );
                    break;
                case 6:
                    mediaTypeTextView.setText( "MP3" );
                    break;
            }

                // Update the views on the screen with the values from the database
                productNameTextView.setText( productName );

            if(price == 0.0){
                priceTextView.setText(priceString);
            } else {
                priceTextView.setText( Double.toString( price ) );
            }

                quantityTextView.setText( Integer.toString( quantity ) );
                supplierNameTextView.setText( supplierName );
                supplierPhoneTextView.setText( supplierPhone );

            }
        }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    }