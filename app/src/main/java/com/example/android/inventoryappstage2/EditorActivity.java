package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.InventoryContract.InventoryEntry;

/**
 * Created by ChandaB on 10/6/2018.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;
    /**
     * EditText field to enter the product name
     */
    private EditText productNameEditText;
    /**
     * Spinner to enter the media type
     */
    private Spinner mediaTypeSpinner;
    /**
     * EditText field to enter the price
     */
    private EditText priceEditText;
    /**
     * EditText field to enter the quantity
     */
    private EditText quantityEditText;
    /**
     * EditText field to enter the supplier name
     */
    private EditText supplierNameEditText;
    /**
     * EditText field to enter the supplier phone number
     */
    private EditText supplierPhoneEditText;

    /**
     * Global variable to calculate quantity
     */
    private int quantity;

    /**
     * Content URI for the existing inventory item, null if it's a new inventory item
     */
    private Uri currentInventoryItemUri;

    /**
     * Variable to store indicator for whether or not the editor activity has had changes
     */
    private boolean itemHasChanged = false;

    /**
     * default mediaType indicator
     */
    private int mediaType = InventoryEntry.MEDIA_TYPE_UNKNOWN;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_editor );

        //Instantiate the new intent for this inventory item
        Intent intent = getIntent();
        currentInventoryItemUri = intent.getData();
        Log.d( "EDITORACTCURRENTINVURI", "Current URI = " + currentInventoryItemUri );

        //If the intent does not contain a inventory ID, then create a new inventory item
        if (currentInventoryItemUri != null) {
            setTitle( getString( R.string.editor_activity_edit_inventory_item ) );
            getLoaderManager().initLoader( EXISTING_INVENTORY_LOADER, null, this );
        } else {
            setTitle( getString( R.string.editor_activity_title_new_inventory_item ) );
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read user input from
        productNameEditText = findViewById( R.id.edit_product_name );
        mediaTypeSpinner = findViewById( R.id.spinner_media_type );
        priceEditText = findViewById( R.id.edit_price );
        quantityEditText = findViewById( R.id.edit_quantity );
        supplierNameEditText = findViewById( R.id.edit_supplier_name );
        supplierPhoneEditText = findViewById( R.id.edit_phone );

        //format phone number
        supplierPhoneEditText.addTextChangedListener( new PhoneNumberFormattingTextWatcher() );

        //Set onTouchListeners for editing data
        productNameEditText.setOnTouchListener( thisTouchListener );
        mediaTypeSpinner.setOnTouchListener( thisTouchListener );
        priceEditText.setOnTouchListener( thisTouchListener );
        quantityEditText.setOnTouchListener( thisTouchListener );
        supplierNameEditText.setOnTouchListener( thisTouchListener );
        supplierPhoneEditText.setOnTouchListener( thisTouchListener );

        //Adding the TextChangedListener to immediately update the quantity value so that the
        //addition and subtraction buttons will work properly
        quantityChangeChecker();

        //Setup the spinner
        setupSpinner();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_MEDIA_TYPE,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE
        };

        //Log the current URI being passed to the cursorloader
        Log.d( "CurrentInventoryItemUri", "OnCreateLoader Method currentInventoryItemURI " + currentInventoryItemUri );

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader( this,   // Parent activity context
                currentInventoryItemUri,         // Query the content URI for the current inventory item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null );                  // Default sort order
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor cursor) {

        // Return if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of inventory attributes that we're interested in
            int productNameColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_NAME );
            int mediaTypeColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_MEDIA_TYPE );
            int priceColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_QUANTITY );
            int supplierNameColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_SUPPLIER_NAME );
            int supplierPhoneColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_SUPPLIER_PHONE );

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString( productNameColumnIndex );
            int spinnerMediaType = cursor.getInt( mediaTypeColumnIndex );
            double price = cursor.getDouble( priceColumnIndex );
            quantity = cursor.getInt( quantityColumnIndex );
            String supplierName = cursor.getString( supplierNameColumnIndex );
            String supplierPhone = cursor.getString( supplierPhoneColumnIndex );

            // Update the views on the screen with the values from the database
            productNameEditText.setText( productName );
            priceEditText.setText( Double.toString( price ) );
            quantityEditText.setText( Integer.toString( quantity ) );
            supplierNameEditText.setText( supplierName );
            supplierPhoneEditText.setText( supplierPhone );

            //mediaType spinner mapping
            switch (spinnerMediaType) {
                case InventoryEntry.MEDIA_TYPE_HARDCOVER:
                    mediaTypeSpinner.setSelection( 1 );
                    break;
                case InventoryEntry.MEDIA_TYPE_PAPERBACK:
                    mediaTypeSpinner.setSelection( 2 );
                    break;
                case InventoryEntry.MEDIA_TYPE_AUDIOBOOK:
                    mediaTypeSpinner.setSelection( 3 );
                    break;
                case InventoryEntry.MEDIA_TYPE_KINDLE:
                    mediaTypeSpinner.setSelection( 4 );
                    break;
                case InventoryEntry.MEDIA_TYPE_EPUB:
                    mediaTypeSpinner.setSelection( 5 );
                    break;
                case InventoryEntry.MEDIA_TYPE_MP3:
                    mediaTypeSpinner.setSelection( 6 );
                    break;
                default:
                    mediaTypeSpinner.setSelection( 0 );
                    break;

            }
        }
    }

    private void quantityChangeChecker() {
        //OnChangeListener that will change the quantity to 0 if the user tries to delete the value
        //in other words, if they tried to null out the value, default to 0
        quantityEditText.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (quantityEditText == null || quantityEditText.getText().toString().length() == 0) {
                    quantityEditText.setText( "0" );
                }
                quantity = Integer.parseInt( quantityEditText.getText().toString() );
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        } );
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //When the loader resets, reset the fields
        productNameEditText.setText( "" );
        mediaTypeSpinner.setSelection( 0 );
        priceEditText.setText( "" );
        quantityEditText.setText( "" );
        supplierNameEditText.setText( "" );
        supplierPhoneEditText.setText( "" );
    }

    /**
     * Setup the dropdown spinner that allows the user to select the media type for this inventory item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use.
        ArrayAdapter mediaTypeSpinnerAdapter = ArrayAdapter.createFromResource( this,
                R.array.array_media_type_options, android.R.layout.simple_spinner_item );

        // Specify dropdown layout style - simple list view with 1 item per line
        mediaTypeSpinnerAdapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );

        // Apply the adapter to the spinner
        mediaTypeSpinner.setAdapter( mediaTypeSpinnerAdapter );

        // Set the integer mediaType to the constant values
        mediaTypeSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                            String selection = (String) parent.getItemAtPosition( position );
                                                            if (!TextUtils.isEmpty( selection )) {
                                                                if (selection.equals( getString( R.string.hardcover ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_HARDCOVER;
                                                                } else if (selection.equals( getString( R.string.paperback ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_PAPERBACK;
                                                                } else if (selection.equals( getString( R.string.audiobook ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_AUDIOBOOK;
                                                                } else if (selection.equals( getString( R.string.kindle ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_KINDLE;
                                                                } else if (selection.equals( getString( R.string.epub ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_EPUB;
                                                                } else if (selection.equals( getString( R.string.mp3 ) )) {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_MP3;
                                                                } else {
                                                                    mediaType = InventoryEntry.MEDIA_TYPE_UNKNOWN;
                                                                }
                                                            }
                                                        }

                                                        // Because AdapterView is an abstract class, onNothingSelected must be defined
                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {
                                                            mediaType = InventoryEntry.MEDIA_TYPE_UNKNOWN;
                                                            Log.d( "onNothingSelected", "This method: onNothingSelected just finished " );
                                                        }
                                                    }

        );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu( menu );
        // If this is a new inventory entry, hide the "Delete" menu item.
        if (currentInventoryItemUri == null) {
            MenuItem menuItem = menu.findItem( R.id.action_delete );
            menuItem.setVisible( false );
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate( R.menu.menu_editor, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save entry to database
                saveItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask( EditorActivity.this );
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask( EditorActivity.this );
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }
        return super.onOptionsItemSelected( item );

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.delete_dialog_msg );
        builder.setPositiveButton( R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete this inventory item.
                deleteInventoryItem();
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
     * This method is called when the + button is clicked and updates the quantity accordingly.
     */
    public void increment(View view) {
        quantity = quantity + 1;
        // Update the textview to display the calculated value
        quantityEditText.setText( Integer.toString( quantity ) );

    }

    /**
     * This method is called when the - button is clicked and updates the quantity acoordingly.
     */
    public void decrement(View view) {
        if (quantity == 0) {
            //show an error message
            Toast.makeText( this, getString( R.string.editor_update_inventory_less_than_zero ),
                    Toast.LENGTH_SHORT ).show();
            //break because we don't want ot do anything else if they try to decrease qty further.
            return;
        } else {
            //Update the quantity and update the textview to display the calculated value
            quantity = quantity - 1;
            quantityEditText.setText( Integer.toString( quantity ) );
        }
    }

    private void deleteInventoryItem() {
        // Only perform the delete if this is an existing inventory item.
        if (currentInventoryItemUri != null) {
            // Call the ContentResolver to delete the inventory item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the inventory item that we want.
            int rowsDeleted = getContentResolver().delete( currentInventoryItemUri, null, null );

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText( this, getString( R.string.editor_delete_inventory_item_failed ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText( this, getString( R.string.editor_delete_invenotry_item_successful ),
                        Toast.LENGTH_SHORT ).show();
            }

            NavUtils.navigateUpFromSameTask( EditorActivity.this );
            // Close the activity
            finish();
        }
    }


    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = productNameEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierPhoneString = supplierPhoneEditText.getText().toString();

        //format phone number
        supplierPhoneEditText.addTextChangedListener( new PhoneNumberFormattingTextWatcher() );

        //Check to see if all fields are empty
        if (currentInventoryItemUri == null && TextUtils.isEmpty( productNameString )
                && TextUtils.isEmpty( priceString )
                && TextUtils.isEmpty( quantityString )
                && TextUtils.isEmpty( supplierNameString )
                && TextUtils.isEmpty( supplierPhoneString )
                && mediaType == InventoryEntry.MEDIA_TYPE_UNKNOWN) {
            // Nothing entered so identify that nothing was saved via toast message.
            Toast.makeText( this, getString( R.string.editor_insert_nothing_entered_not_saving ),
                    Toast.LENGTH_SHORT ).show();
            return;
        }

        //Validation to confirm that all fields have been entered before saving
        if (TextUtils.isEmpty( productNameString )) {
            Toast.makeText( this, getString( R.string.editor_insert_product_name_required ),
                    Toast.LENGTH_SHORT ).show();
        } else if (TextUtils.isEmpty( priceString )) {
            Toast.makeText( this, getString( R.string.editor_insert_price_required ),
                    Toast.LENGTH_SHORT ).show();
        } else if (TextUtils.isEmpty( quantityString )) {
            Toast.makeText( this, getString( R.string.editor_insert_quantity_required ),
                    Toast.LENGTH_SHORT ).show();
        } else if (TextUtils.isEmpty( supplierNameString )) {
            Toast.makeText( this, getString( R.string.editor_insert_supplier_name_required ),
                    Toast.LENGTH_SHORT ).show();
        } else if (TextUtils.isEmpty( supplierPhoneString )) {
            Toast.makeText( this, getString( R.string.editor_insert_supplier_phone_required ),
                    Toast.LENGTH_SHORT ).show();
        } else {
            //If the quantity string is empty, then set it to 0. Technically this should never happen anyway
            int quantity = 0;
            if (!TextUtils.isEmpty( quantityString )) {
                quantity = Integer.parseInt( quantityString );
            }

            // Create a ContentValues object where column names are the keys,
            // and Inventory attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put( InventoryEntry.COLUMN_PRODUCT_NAME, productNameString );
            values.put( InventoryEntry.COLUMN_MEDIA_TYPE, mediaType );
            values.put( InventoryEntry.COLUMN_PRICE, priceString );
            values.put( InventoryEntry.COLUMN_QUANTITY, quantity );
            values.put( InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString );
            values.put( InventoryEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString );

            //Determine if a new inventory item or existing inventory item
            if (currentInventoryItemUri == null) {
                //Generate the URL
                Uri newUri = getContentResolver().insert( InventoryEntry.CONTENT_URI, values );

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText( this, getString( R.string.editor_insert_inventory_failed ),
                            Toast.LENGTH_SHORT ).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText( this, getString( R.string.editor_insert_inventory_successful ),
                            Toast.LENGTH_SHORT ).show();
                }

            } else {
                //perform update on an existing inventory item
                int rowsAffected = getContentResolver().update( currentInventoryItemUri, values, null, null );

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText( this, getString( R.string.editor_update_inventory_failed ),
                            Toast.LENGTH_SHORT ).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText( this, getString( R.string.editor_update_inventory_successful ),
                            Toast.LENGTH_SHORT ).show();
                }

            }
            NavUtils.navigateUpFromSameTask( EditorActivity.this );
            // Exit activity
            finish();
        }


    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.unsaved_changes_dialog_msg );
        builder.setPositiveButton( R.string.discard, discardButtonClickListener );
        builder.setNegativeButton( R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing current item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private View.OnTouchListener thisTouchListener = new View.OnTouchListener() {
        //onTouchListener to determine if the phone number has been selected...if it has set text to ""
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;

            if (supplierPhoneEditText.isSelected()) {
                supplierPhoneEditText.setText( "" );
            }
            return false;
        }
    };
}
