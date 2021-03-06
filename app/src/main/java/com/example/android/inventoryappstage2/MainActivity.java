package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryappstage2.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Logging variable
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Gloable variables for Main Activity
     */
    private static final int inventory_loader_indicator = 0;
    //InventoryCursorAdapter
    InventoryCursorAdapter listViewCursorAdapter;
    // Database helper that will provide access to the database
    private InventoryDbHelper inventoryDbHelper;
    //DrawerLayout for nagivation drawer
    private DrawerLayout mDrawerLayout;

    /**
     * Used to instantiate the nagigation drawer
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer( GravityCompat.START );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        //invoke toolbar
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        //Invoke an actionbar
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled( true );
        actionbar.setHomeAsUpIndicator( R.drawable.ic_menu );

        //set the layout for the navigation drawer
        mDrawerLayout = findViewById( R.id.drawer_layout );

        //Create a navigation drawer
        final NavigationView navigationView = findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        int selectedOption = menuItem.getItemId();
                        switch (selectedOption) {
                            case R.id.add_item:
                                Intent i = new Intent( MainActivity.this, EditorActivity.class );
                                startActivity( i );
                                return true;
                            case R.id.insert_dummy_data:
                                //Close the navigation drawer
                                mDrawerLayout.closeDrawers();
                                //Method call to insert a dummy data into the table
                                insertData();
                                //Create a handler to delay refreshing the main screen until the drawer closes
                                Handler handler = new Handler();
                                //Delay refreshing main screen for 1 second
                                handler.postDelayed( new Runnable() {
                                    public void run() {
                                        finish();
                                        startActivity( getIntent() );
                                    }
                                }, 1000 );
                                return true;
                            case R.id.delete_all_data:
                                showDeleteConfirmationDialog();
                                return true;
                            // Add code here to update the UI based on the item selected
                            // For example, swap UI fragments here
                        }
                        return true;
                    }
                }
        );

        // Instantiate a new InventoryDbHelper
        // passing the context (this), which is the current activity.
        inventoryDbHelper = new InventoryDbHelper( this );

        // Find the ListView which will be populated with the inventory data
        ListView inventoryListView = findViewById( R.id.list_view_inventory );

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById( R.id.empty_view );
        inventoryListView.setEmptyView( emptyView );

        //Inveoke the cursorAdapter
        listViewCursorAdapter = new InventoryCursorAdapter( this, null );
        inventoryListView.setAdapter( listViewCursorAdapter );

        //Set onItemClickListener to open viewer details when a list item is clicked
        inventoryListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Create a new Intent to open the editor activity
                Intent intent = new Intent( MainActivity.this, ViewerActivity.class );

                //Form the URL by appending the selected inventory item's ID to the base URI
                Uri currentInventoryUri = ContentUris.withAppendedId( InventoryEntry.CONTENT_URI, id );

                Log.d( "CAPTURINGURIONITEMCLICK", "URI: " + currentInventoryUri );

                //Set the URI on the data field of the intent
                intent.setData( currentInventoryUri );

                //Launce the editor activity
                startActivity( intent );
            }
        } );

        //start the loadermanager
        getLoaderManager().initLoader( inventory_loader_indicator, null, this );
    }

    //Method to insert dummy data into database.
    private void insertData() {

        // Calling the getWritableDatabase method, puts the db in write mode
        SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();

        // Create a ContentValues object
        ContentValues sampleValues = new ContentValues();

        // Data for HARDCOVER book with null price to display price not found on listview,
        // the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Happy Doomsday" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_HARDCOVER );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 1502 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "47 North" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(317) 444-5555" );

        //insert samplevalues in the table
        long newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );

        // Data for KINDLE book, the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Happy Doomsday" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_KINDLE );
        sampleValues.put( InventoryEntry.COLUMN_PRICE, 4.99 );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 15452 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "47 North" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(317) 444-5555" );

        //insert samplevalues in the table
        newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );

        // Data for EPUB book, the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Dang Near Dead" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_EPUB );
        sampleValues.put( InventoryEntry.COLUMN_PRICE, 2.99 );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 148 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "Henery Press" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(515) 444-5555" );

        //insert samplevalues in the table
        newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );

        // Data for MP3 book, the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Little Fires Everywhere" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_MP3 );
        sampleValues.put( InventoryEntry.COLUMN_PRICE, 24.99 );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 39 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "Penguin Press" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(515) 555-5555" );

        //insert samplevalues in the table
        newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );

        // Data for PAPERBACK book also set price to 0.0 so that the listview displays the price as FREE,
        // the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Fantastic Beasts" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_PAPERBACK );
        sampleValues.put( InventoryEntry.COLUMN_PRICE, 0.0 );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 4520 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "Arthur A. Levine Books" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(515) 555-9999" );

        //insert samplevalues in the table
        newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );

        // Data for AUDIOBOOK, the column names are the keys and applicable values are identified for entry in the db.
        sampleValues.put( InventoryEntry.COLUMN_PRODUCT_NAME, "Far From The Tree" );
        sampleValues.put( InventoryEntry.COLUMN_MEDIA_TYPE, InventoryEntry.MEDIA_TYPE_AUDIOBOOK );
        sampleValues.put( InventoryEntry.COLUMN_PRICE, 28.95 );
        sampleValues.put( InventoryEntry.COLUMN_QUANTITY, 15 );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_NAME, "Harper Collins" );
        sampleValues.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, "(515) 888-1111" );

        //insert samplevalues in the table
        newRowId = db.insert( InventoryEntry.TABLE_NAME, null, sampleValues );
        //Log message shows row number that was inserted
        Log.d( LOG_TAG, "Row number inserted: " + newRowId );
    }

    /**
     * select inventory item
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //The _ID is always needed for any cursor you create
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_MEDIA_TYPE,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY};

        return new CursorLoader( this, InventoryEntry.CONTENT_URI, projection, null, null, null );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listViewCursorAdapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listViewCursorAdapter.swapCursor( null );
    }

    /**
     * Method used to generate alertdialog asking user whether or not they want to delete all
     * the data from the inventory table
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.delete_all_inventory_dialog_msg );
        builder.setPositiveButton( R.string.action_delete_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete ALL data from the table.
                deleteAllInventory();
            }
        } );
        builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method used to actually delete the data once the user confirms they want to delete all the data in the table
     */
    private void deleteAllInventory() {
        Log.d( "URI", InventoryEntry.CONTENT_URI.toString() );
        int rowsDeleted = getContentResolver().delete( InventoryEntry.CONTENT_URI, null, null );
        Log.v( "MainActivity", rowsDeleted + " rows deleted from inventory database" );
    }
}