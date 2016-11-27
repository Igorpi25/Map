package com.ivanov.tech.map.provider;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public final class DBContract {

    private static final String TAG = "DBContract";

    public DBContract(){}

    public static final String DATABASE_NAME = "map.db";
    public static final int DATABASE_VERSION = 1;
    
    public static final int PROVIDER_BAIDU=0;
    public static final int PROVIDER_GOOGLE=1;
        
    public static abstract class UserLocation implements BaseColumns {

        public static final String TABLE_NAME = "user_location";
        
        public static final String COLUMN_NAME_USER_ID = "userid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TIMESTAMP_LOCAL = "timestamp_local";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_ACCURACY = "accuracy";
        public static final String COLUMN_NAME_PROVIDER = "provider";
        
        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME_USER_ID + " INTEGER DEFAULT 0, " +
                        COLUMN_NAME_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        COLUMN_NAME_TIMESTAMP_LOCAL + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        COLUMN_NAME_LATITUDE + " REAL DEFAULT 0, " +
                        COLUMN_NAME_LONGITUDE + " REAL DEFAULT 0, " +
                        COLUMN_NAME_ACCURACY+ " REAL DEFAULT 0, " +
                        COLUMN_NAME_PROVIDER+ " INTEGER DEFAULT 0 " +
                 ");";
    }
    
    public static void onCreate(SQLiteDatabase db) {
    	Log.w(TAG, "onCreate");
    	
    	db.execSQL(UserLocation.CREATE_TABLE);
    }
    
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
    }

}