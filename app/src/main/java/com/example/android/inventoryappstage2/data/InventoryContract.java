package com.example.android.inventoryappstage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ChandaB on 8/24/2018.
 * Contract for the Inventory database
 */

public final class InventoryContract {

    //Empty constructor for the InventoryContract class
    private InventoryContract() {
    }

    //Content Authority constant
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstage2";

    //Constant for base URI actions will be appended to this URI value
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Constant for the table name that will be accessed
    public static final String PATH_INVENTORY = "inventory";

    //Inner class to define the inventory table
    public static final class InventoryEntry implements BaseColumns {

        //URI to access pets table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //Name of table for inventory items
        public static final String TABLE_NAME = "inventory";

        //ID defined as an integer, will autoincrement
        public final static String _ID = BaseColumns._ID;

        //Product Name defined as TEXT
        public static final String COLUMN_PRODUCT_NAME = "product_name";

        //Type of media
        public static final String COLUMN_MEDIA_TYPE = "media_type";

        //Price defined as REAL
        public static final String COLUMN_PRICE = "price";

        //Quantity defined as INTEGER
        public static final String COLUMN_QUANTITY = "quantity";

        //Supplier Name defined as TEXT
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        //Supplier Phone Number defined as TEXT
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        //Values to identify what type of book and/or media the product is
        public static final int MEDIA_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TYPE_HARDCOVER = 1;
        public static final int MEDIA_TYPE_PAPERBACK = 2;
        public static final int MEDIA_TYPE_AUDIOBOOK = 3;
        public static final int MEDIA_TYPE_KINDLE = 4;
        public static final int MEDIA_TYPE_EPUB = 5;
        public static final int MEDIA_TYPE_MP3 = 6;

        public static boolean isValidMediaType(Integer mediaType) {
            if (mediaType == MEDIA_TYPE_UNKNOWN || mediaType == MEDIA_TYPE_PAPERBACK ||
            mediaType == MEDIA_TYPE_AUDIOBOOK || mediaType == MEDIA_TYPE_HARDCOVER ||
            mediaType == MEDIA_TYPE_KINDLE || mediaType == MEDIA_TYPE_EPUB ||
                    mediaType == MEDIA_TYPE_MP3
                    ) {
                return true;
            }
            return false;
        }
    }
}
