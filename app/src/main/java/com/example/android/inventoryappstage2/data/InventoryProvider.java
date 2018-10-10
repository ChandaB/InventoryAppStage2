package com.example.android.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

/**
 * Created by ChandaB on 10/4/2018.
 */

public class InventoryProvider extends ContentProvider {
    /**
     * Logging variable
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single innventory item in the inventory table
     */
    private static final int INVENTORY_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the roo t URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI( InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY );
        sUriMatcher.addURI( InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID );
    }

    /**
     * Global InventoryDbHelper invocation;
     */
    private InventoryDbHelper inventoryDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper( getContext() );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        {
            Log.d( LOG_TAG, "QUERY URI " + uri + ", selection " + selection + ", selectionArgs " + selectionArgs );
            // Get readable database
            SQLiteDatabase database = inventoryDbHelper.getReadableDatabase();
            // This cursor will hold the result of the query
            Cursor cursor;
            // Figure out if the URI matcher can match the URI to a specific code
            int match = sUriMatcher.match( uri );
            switch (match) {
                case INVENTORY:
                    // Query the inventory table directly with the given
                    // projection, selection, selection arguments, and sort order. The cursor
                    // could contain multiple rows of the inventory table.
                    cursor = database.query( InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                    break;
                case INVENTORY_ID:
                    //Construct query to select a specific inventory item in the inventory table
                    selection = InventoryEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                    //Set the cursor data to the data retrieved from the table
                    cursor = database.query( InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder );
                    break;
                default:
                    throw new IllegalArgumentException( "Cannot query unknown URI " + uri );
            }
            cursor.setNotificationUri( getContext().getContentResolver(), uri );
            return cursor;
        }

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d( LOG_TAG, "Insert URI " + uri + ", values " + values.toString() );
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case INVENTORY:
                return insertInventory( uri, values );
            default:
                throw new IllegalArgumentException( "Insertion is not supported for " + uri );
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString( InventoryEntry.COLUMN_PRODUCT_NAME );
        if (name == null) {
            throw new IllegalArgumentException( "You must provide the product name" );
        }
        // Check that the media type is valid
        Integer mediaType = values.getAsInteger( InventoryEntry.COLUMN_MEDIA_TYPE );
        if (mediaType == null || !InventoryEntry.isValidMediaType( mediaType )) {
            throw new IllegalArgumentException( "You must select a valid media type" );
        }
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer quantity = values.getAsInteger( InventoryEntry.COLUMN_QUANTITY );
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException( "Quantity must be 0 or greater" );
        }
        //Instantiate writable database object
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        //Insert a new inventory item
        long id = database.insert( InventoryEntry.TABLE_NAME, null, values );
        //Check to see if insert was successful, display error if it wasn't
        if (id == -1) {
            Log.e( LOG_TAG, "Failed to insert record for " + uri + " in database." );
            return null;
        }
        //notify listeners that data has changes for the content URI
        getContext().getContentResolver().notifyChange( uri, null );
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId( uri, id );

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        //Create variable to keep track of number of rows deleted
        int rowsDeleted;
        final int match = sUriMatcher.match( uri );
        //Determine which URI is being called for the delete (whether to delete all data in table or single row
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete( InventoryEntry.TABLE_NAME, selection, selectionArgs );
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete( InventoryEntry.TABLE_NAME, selection, selectionArgs );
                break;
            default:
                throw new IllegalArgumentException( "Deletion is not supported for " + uri );
        }
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }
        //Number of rows deleted by this action
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case INVENTORY:
                return updateInventory( uri, contentValues, selection, selectionArgs );
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                return updateInventory( uri, contentValues, selection, selectionArgs );
            default:
                throw new IllegalArgumentException( "Update is not supported for " + uri );
        }
    }

    /**
     *
     * @param uri  Uri for current entry
     * @param values values to update in database
     * @param selection selection values
     * @param selectionArgs and the selection args
     * @return
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Throw exception if product name is null.
        if (values.containsKey( InventoryEntry.COLUMN_PRODUCT_NAME )) {
            String name = values.getAsString( InventoryEntry.COLUMN_PRODUCT_NAME );
            if (name == null) {
                throw new IllegalArgumentException( "You must specify a Product name" );
            }
        }
        // Throw exception if the mediatype is null
        if (values.containsKey( InventoryEntry.COLUMN_MEDIA_TYPE )) {
            Integer mediaType = values.getAsInteger( InventoryEntry.COLUMN_MEDIA_TYPE );
            if (mediaType == null || !InventoryEntry.isValidMediaType( mediaType )) {
                throw new IllegalArgumentException( "This product requires a valid mediaType" );
            }
        }
        // Throw exception if the quantity is null
        if (values.containsKey( InventoryEntry.COLUMN_QUANTITY )) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger( InventoryEntry.COLUMN_QUANTITY );
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException( "Quantity must be 0 or greater" );
            }
        }
        // Throw exception if the price is null
        if (values.containsKey( InventoryEntry.COLUMN_PRICE )) {
            // Check that the weight is greater than or equal to 0 kg
            Double price = values.getAsDouble( InventoryEntry.COLUMN_PRICE );
            if (price != null && price < 0) {
                throw new IllegalArgumentException( "Quantity must be 0 or greater" );
            }
        }
        // Throw exception if supplier name is null.
        if (values.containsKey( InventoryEntry.COLUMN_SUPPLIER_NAME )) {
            String name = values.getAsString( InventoryEntry.COLUMN_SUPPLIER_NAME );
            if (name == null) {
                throw new IllegalArgumentException( "You must specify a Supplier name" );
            }
        }
        // Throw exception if Supplier phone is null.
        if (values.containsKey( InventoryEntry.COLUMN_SUPPLIER_PHONE )) {
            String name = values.getAsString( InventoryEntry.COLUMN_SUPPLIER_PHONE );
            if (name == null) {
                throw new IllegalArgumentException( "You must specify a Supplier phone number" );
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update( InventoryEntry.TABLE_NAME, values, selection, selectionArgs );
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }
        // Returns the number of database rows affected by the update statement
        return rowsUpdated;

    }
}
