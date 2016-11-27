package com.ivanov.tech.map.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.ivanov.tech.session.Session;

public class DBContentProvider extends ContentProvider{

	private static final String TAG = "Map.ContentProvider";
	
    private DBHelper dbHelper;

    public static final String AUTHORITY = "com.ivanov.tech.map.provider.contentprovider_db";
   
    public static final String GROUP_LOCATION="group_location";
    public static final String MY_LOCATION="my_location";
    public static final String FRIENDS_LOCATION="friends_location";
    public static final String ONE_USER_LOCATION="one_user_location";
    
    public static final Uri URI_USER_LOCATION = Uri.parse("content://" + AUTHORITY + "/" + DBContract.UserLocation.TABLE_NAME);
    public static final Uri URI_GROUP_LOCATION = Uri.parse("content://" + AUTHORITY + "/"+GROUP_LOCATION);    
    public static final Uri URI_MY_LOCATION = Uri.parse("content://" + AUTHORITY + "/"+MY_LOCATION);
    public static final Uri URI_FRIENDS_LOCATION = Uri.parse("content://" + AUTHORITY + "/"+FRIENDS_LOCATION);
    public static final Uri URI_ONE_USER_LOCATION = Uri.parse("content://" + AUTHORITY + "/"+ONE_USER_LOCATION);
    
    private static final UriMatcher uriMatcher;
    
    private static final int SPEC_USER_LOCATION = 1;
    private static final int SPEC_USER_LOCATION_USERID = 11;    
    private static final int SPEC_GROUP_LOCATION_GROUPID = 21;    
    private static final int SPEC_MY_LOCATION = 3;    
    private static final int SPEC_FRIENDS_LOCATION = 4;
    private static final int SPEC_ONE_USER_LOCATION_CLIENTID = 5;
    
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // a content URI pattern matches content URIs using wildcard characters:
        // *: Matches a string of any valid characters of any length.
        // #: Matches a string of numeric characters of any length.
                
        //Users location
        uriMatcher.addURI(AUTHORITY, DBContract.UserLocation.TABLE_NAME, SPEC_USER_LOCATION);
        uriMatcher.addURI(AUTHORITY, DBContract.UserLocation.TABLE_NAME+"/#", SPEC_USER_LOCATION_USERID);        
        uriMatcher.addURI(AUTHORITY, GROUP_LOCATION+"/#", SPEC_GROUP_LOCATION_GROUPID);
        uriMatcher.addURI(AUTHORITY, MY_LOCATION, SPEC_MY_LOCATION);
        uriMatcher.addURI(AUTHORITY, FRIENDS_LOCATION, SPEC_FRIENDS_LOCATION);
        uriMatcher.addURI(AUTHORITY, ONE_USER_LOCATION+"/#", SPEC_ONE_USER_LOCATION_CLIENTID);
        
    }

    @Override
    public boolean onCreate() {
    	
        dbHelper = new DBHelper(getContext());
        
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;
        Uri resultUri;
        switch (uriMatcher.match(uri)) {
            
            case SPEC_USER_LOCATION:
            	Log.d(TAG,"insert USER_LOCATION");
            	
            	id = db.insert(DBContract.UserLocation.TABLE_NAME, null, values);
            	
                resultUri=Uri.parse(SPEC_USER_LOCATION+"/"+id);
                
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                getContext().getContentResolver().notifyChange(resultUri,null);
                return resultUri;   
                
            case SPEC_MY_LOCATION:
            	Log.d(TAG,"insert MY_LOCATION");
            	
            	values.put(DBContract.UserLocation.COLUMN_NAME_USER_ID, Session.getUserId());            	
            	id = db.insert(DBContract.UserLocation.TABLE_NAME, null, values);
            	
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                getContext().getContentResolver().notifyChange(uri,null);
                
                resultUri=Uri.parse(SPEC_USER_LOCATION+"/"+id);
                return resultUri;
                        
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        
        
        if(uriMatcher.match(uri)==SPEC_FRIENDS_LOCATION){
        	
        	Log.d(TAG, "query SPEC_FRIENDS_LOCATION");
        	Cursor cursor=null;
        	
        	SQLiteDatabase db = dbHelper.getWritableDatabase();
        	        	
        	try{        	
        		db.execSQL("ATTACH DATABASE ? AS db_profile", new String[]{ getContext().getDatabasePath("profile.db").getPath() });
        	}catch (SQLException e) {
            	Log.e(TAG, "query SPEC_FRIENDS_LOCATION SQLException e1="+e);
            }
        	
        	try {
                String sql=
                	"SELECT l._id, l.userid, l.latitude, l.longitude, l.timestamp, l.accuracy, l.provider, l.timestamp_local " +                	
                	"FROM 	( SELECT user.server_id AS userid "+
		           			"FROM db_profile.user AS user " + 
		           			"WHERE ( user.status = 3 ) OR ( user.server_id = 0 ) OR ( user.server_id = "+Session.getUserId()+" ) "+		           			
		           			") AS u "+
		           	"INNER JOIN " + DBContract.UserLocation.TABLE_NAME + " AS l ON u.userid = l.userid "
		           	;
                                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_USER_LOCATION);
            	
            	
            } catch (SQLException e) {
            	Log.e(TAG, "query SPEC_FRIENDS_LOCATION SQLException e2="+e);
            } catch (Exception e) {
            	Log.e(TAG, "query SPEC_FRIENDS_LOCATION Exception e3="+e);
            } 
        	
        	
        	return cursor;
        }  
        
        if(uriMatcher.match(uri)==SPEC_GROUP_LOCATION_GROUPID){
        	
        	Log.d(TAG, "query SPEC_GROUP_LOCATION_GROUPID uri="+uri);
        	Cursor cursor=null;
        	
        	SQLiteDatabase db = dbHelper.getWritableDatabase();
        	        	
        	try{        	
        		db.execSQL("ATTACH DATABASE ? AS db_profile", new String[]{ getContext().getDatabasePath("profile.db").getPath() });
        	}catch (SQLException e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID SQLException e1="+e);
            }
        	
        	String group_id = uri.getPathSegments().get(1);
        	
        	try {
                String sql=
                	"SELECT l._id, l.userid, l.latitude, l.longitude, l.timestamp, l.accuracy, l.provider, l.timestamp_local " +                	
                	"FROM 	( SELECT groupusers.userid AS userid "+
		           			"FROM db_profile.groupusers AS groupusers " + 
		           			"WHERE ( groupusers.groupid = "+group_id+" ) "+		           			
		           			") AS u "+
		           	"INNER JOIN " + DBContract.UserLocation.TABLE_NAME + " AS l ON u.userid = l.userid "
		           	;
                                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_USER_LOCATION);
            	
            	
            } catch (SQLException e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID SQLException e2="+e);
            } catch (Exception e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID Exception e3="+e);
            } 
        	
        	Log.d(TAG, "query SPEC_GROUP_LOCATION_GROUPID cursor.count="+cursor.getCount());
        	
        	return cursor;
        } 
        
        
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String user_server_id=null;
        Cursor cursor = null;
        
        switch (uriMatcher.match(uri)) {            
            
            case SPEC_USER_LOCATION:
            	queryBuilder.setTables(DBContract.UserLocation.TABLE_NAME);
            	cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_USER_LOCATION); 
                break;
                
            case SPEC_USER_LOCATION_USERID:
            	queryBuilder.setTables(DBContract.UserLocation.TABLE_NAME);
                user_server_id = uri.getPathSegments().get(1);
                
                if(user_server_id.equals("0")){            		
                	user_server_id=String.valueOf(Session.getUserId());
            		Log.d(TAG, "query User_id replaced rom 0 to "+user_server_id);
            	}
                
                queryBuilder.appendWhere(DBContract.UserLocation.COLUMN_NAME_USER_ID + " = " + user_server_id);
                cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                break;     
                
            case SPEC_MY_LOCATION:
            	queryBuilder.setTables(DBContract.UserLocation.TABLE_NAME);  
                user_server_id=String.valueOf(Session.getUserId());
                queryBuilder.appendWhere(DBContract.UserLocation.COLUMN_NAME_USER_ID + " = " + user_server_id);
                cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                break;
            
            case SPEC_ONE_USER_LOCATION_CLIENTID:
            	Log.d(TAG, "query SPEC_ONE_USER_LOCATION");
            	
            	queryBuilder.setTables(DBContract.UserLocation.TABLE_NAME);
                user_server_id = uri.getPathSegments().get(1);
                
                String where="( "
                		+ "( "+DBContract.UserLocation.COLUMN_NAME_USER_ID+" = "+user_server_id+" ) OR"
                		+ "( "+DBContract.UserLocation.COLUMN_NAME_USER_ID+" = "+String.valueOf(Session.getUserId())+" ) OR"
                		+ "( "+DBContract.UserLocation.COLUMN_NAME_USER_ID+" = 0 ) "
                		+ ") ";
                
                queryBuilder.appendWhere(where);
                
//                selection="( ( ? = ? ) OR ( ? = ? ) OR ( ? = ? ))";
//                
//                selectionArgs=new String[]{
//                		DBContract.UserLocation.COLUMN_NAME_USER_ID,user_server_id,
//                		DBContract.UserLocation.COLUMN_NAME_USER_ID,String.valueOf(Session.getUserId()),
//                		DBContract.UserLocation.COLUMN_NAME_USER_ID,"0"
//                };
                		
                cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(),URI_USER_LOCATION);
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return cursor;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleteCount;
        switch (uriMatcher.match(uri)) {
        
        	//Delete messages of private conversation with user
            case SPEC_USER_LOCATION_USERID:
                String user_server_id = uri.getPathSegments().get(1);
                //Delete conversation
                selection = DBContract.UserLocation.COLUMN_NAME_USER_ID + " = " + user_server_id;
                deleteCount = db.delete(DBContract.UserLocation.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                getContext().getContentResolver().notifyChange(uri,null);
                break;
                
            //Delete all messages
            case SPEC_USER_LOCATION:
                deleteCount = db.delete(DBContract.UserLocation.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                break;
                   
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Log.d(TAG,"delete Uri="+uri.toString());
        getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
        getContext().getContentResolver().notifyChange(uri, null);
        
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount;
        		
        switch (uriMatcher.match(uri)) {
        	case SPEC_USER_LOCATION:
        		updateCount = db.update(DBContract.UserLocation.TABLE_NAME, values, selection, selectionArgs);
        		
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                
                
                break;
        	case SPEC_USER_LOCATION_USERID:
        		String user_server_id = uri.getPathSegments().get(1);        		
                
                if(user_server_id.equals("0")){            		
                	user_server_id=String.valueOf(Session.getUserId());            		
            	}
        		
                selection = DBContract.UserLocation.COLUMN_NAME_USER_ID + " = " + user_server_id;
                
        		updateCount = db.update(DBContract.UserLocation.TABLE_NAME, values, selection, selectionArgs);
        		
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                getContext().getContentResolver().notifyChange(uri, null);
                
                
                break;
                
        	case SPEC_MY_LOCATION:
        		
        		user_server_id=String.valueOf(Session.getUserId());
                selection = DBContract.UserLocation.COLUMN_NAME_USER_ID + " = " + user_server_id;
                
        		updateCount = db.update(DBContract.UserLocation.TABLE_NAME, values, selection, selectionArgs);
        		
                getContext().getContentResolver().notifyChange(URI_USER_LOCATION, null);
                getContext().getContentResolver().notifyChange(uri, null);
                                
                break;
                        
        	default:
        		throw new IllegalArgumentException("Unsupported URI: " + uri);
        }


        Log.d(TAG,"update Uri="+uri.toString());
       
        return updateCount;
    	
    }
    

}