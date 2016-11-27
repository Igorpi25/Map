package com.ivanov.tech.profile.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Date;

import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.session.Session;


public class DBContentProvider extends ContentProvider{

    private DBHelper dbHelper;
    
    private static final String TAG = "Profile."+DBContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.ivanov.tech.profile.provider.contentprovider_db";
   
    public static final Uri URI_USER = Uri.parse("content://" + AUTHORITY + "/" + DBContract.User.TABLE_NAME);
    public static final Uri URI_GROUP = Uri.parse("content://" + AUTHORITY + "/" + DBContract.Group.TABLE_NAME);
    public static final Uri URI_GROUPUSERS = Uri.parse("content://" + AUTHORITY + "/" + DBContract.GroupUsers.TABLE_NAME);
    
    public static final Uri URI_CONTACTS_GROUPS = Uri.parse("content://" + AUTHORITY + "/" + "contacts_groups");
    
    public static final Uri URI_SELECT_GROUP_ADD_USERS = Uri.parse("content://" + AUTHORITY + "/" + "select_group_add_users");
    
    private static final UriMatcher uriMatcher;
        
    private static final int USER = 1;
    private static final int USER_SERVER_ID = 11;
    
    private static final int GROUP = 2;
    private static final int GROUP_SERVER_ID = 21;
    
    private static final int GROUPUSERS = 3;
    private static final int GROUPUSERS_GROUPID = 31;
    private static final int GROUPUSERS_GROUPID_USERID = 311;
    private static final int GROUPUSERS_GROUPID_USERID_STATUS = 3111;
    
    private static final int SELECT_GROUP_ADD_USERS_GROUPID = 51;
        
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // a content URI pattern matches content URIs using wildcard characters:
        // *: Matches a string of any valid characters of any length.
        // #: Matches a string of numeric characters of any length.

        
        //All users
        uriMatcher.addURI(AUTHORITY, DBContract.User.TABLE_NAME, USER);
        //Single user
        uriMatcher.addURI(AUTHORITY, DBContract.User.TABLE_NAME+"/#", USER_SERVER_ID);
        
        //All groups
        uriMatcher.addURI(AUTHORITY, DBContract.Group.TABLE_NAME, GROUP);
        //Single group
        uriMatcher.addURI(AUTHORITY, DBContract.Group.TABLE_NAME+"/#", GROUP_SERVER_ID);
        
        //Groupusers
        uriMatcher.addURI(AUTHORITY, DBContract.GroupUsers.TABLE_NAME,GROUPUSERS);
        //All users of group
        uriMatcher.addURI(AUTHORITY, DBContract.GroupUsers.TABLE_NAME+"/#",GROUPUSERS_GROUPID);
        //User in group
        uriMatcher.addURI(AUTHORITY, DBContract.GroupUsers.TABLE_NAME+"/#/#",GROUPUSERS_GROUPID_USERID);
        //User in group with status
        uriMatcher.addURI(AUTHORITY, DBContract.GroupUsers.TABLE_NAME+"/#/#/#",GROUPUSERS_GROUPID_USERID_STATUS);
        
        //
        uriMatcher.addURI(AUTHORITY, "select_group_add_users"+"/#",SELECT_GROUP_ADD_USERS_GROUPID);
    }

    // system calls onCreate() when it starts up the provider.
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
            
            case USER:
            	
            	id = db.insert(DBContract.User.TABLE_NAME, null, values);
            	
                resultUri=Uri.parse(URI_USER+"/"+id);

                Log.d(TAG,"insert user uri="+uri.toString());

                getContext().getContentResolver().notifyChange(URI_USER, null);

                return resultUri;  
                
            case GROUP:
            	
            	id = db.insert(DBContract.Group.TABLE_NAME, null, values);
            	
                resultUri=Uri.parse(URI_GROUP+"/"+id);

                //Log.d("DBContentProvider","insert group resultUri="+resultUri.toString());

                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                
                return resultUri; 
            
                
            case GROUPUSERS_GROUPID_USERID: {
            	
            	String groupid = uri.getPathSegments().get(1);
            	String userid = uri.getPathSegments().get(2);
            	
            	if(values==null)values=new ContentValues();
            	
            	values.put(DBContract.GroupUsers.COLUMN_NAME_GROUPID, groupid);
            	values.put(DBContract.GroupUsers.COLUMN_NAME_USERID, userid);
            	
            	id = db.insert(DBContract.GroupUsers.TABLE_NAME, null, values);
            	
                //Log.d("DBContentProvider","insert "+uri);

                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid), null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid), null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);

                return Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid);
            	}
            case GROUPUSERS_GROUPID_USERID_STATUS:
            	            	
            	String groupid = uri.getPathSegments().get(1);
            	String userid = uri.getPathSegments().get(2);
            	String status = uri.getPathSegments().get(3);
            	
            	if(values==null)values=new ContentValues();
            	
            	values.put(DBContract.GroupUsers.COLUMN_NAME_GROUPID, groupid);
            	values.put(DBContract.GroupUsers.COLUMN_NAME_USERID, userid);
            	values.put(DBContract.GroupUsers.COLUMN_NAME_STATUS, status);
            	
            	
            	id = db.insert(DBContract.GroupUsers.TABLE_NAME, null, values);
            	
                //Log.d("DBContentProvider","insert "+uri);

                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid), null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid), null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);

                return Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid); 
                
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }

    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        
        
        if(uri.equals(URI_CONTACTS_GROUPS)){
        	
        	Log.d(TAG, "query uri=CONTACTS_GROUPS");
        	Cursor cursor=null;
        	
        	try {
                String sql=
                	"SELECT m.*, gu.status AS status " +
                	"FROM ( " +
	                	"SELECT groups._id AS _id, groups.server_id AS server_id, groups.name AS name, groups.url_icon AS url_icon, groups.url_avatar AS url_avatar, groups.url_full AS url_full, groups.changed_at AS changed_at, COUNT(groupusers.groupid) AS count " +               	
	                	"FROM groupusers AS groupusers " + 
	                	"LEFT OUTER JOIN groups AS groups ON groupusers.groupid = groups.server_id " +  
	                	"WHERE ( ( groupusers.status = 0 ) OR ( groupusers.status = 1 ) OR ( groupusers.status = 2 ) ) " +
			           	"GROUP BY groupusers.groupid " +
	                	"ORDER BY groupusers._id DESC " +
	                	") m "+
                	"INNER JOIN groupusers AS gu ON ( ( m.server_id = gu.groupid ) AND (gu.userid = "+Session.getUserId()+" ) ) ";
                	
                	
                                
                cursor=db.rawQuery(sql,null);
            	cursor.setNotificationUri(getContext().getContentResolver(),uri);
            	
            	
            } catch (SQLException e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID SQLException e2="+e);
            } catch (Exception e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID Exception e3="+e);
            } 
        	
        	Log.d(TAG, "query uri=CONTACTS_GROUPS cursor.count="+cursor.getCount());
        	
        	return cursor;
        } 
        
        if(uriMatcher.match(uri)==SELECT_GROUP_ADD_USERS_GROUPID){
        	
        	String groupid=uri.getPathSegments().get(1);
        	Log.d(TAG, "query uri=URI_SELECT_GROUP_ADD_USERS_GROUPID/"+groupid);
        	Cursor cursor=null;
        	
        	try {
                String sql=
                	"SELECT user.* " +
                	"FROM user AS user " +
                	"LEFT OUTER JOIN ( " +
	                	"SELECT groupusers.userid " +               	
	                	"FROM groupusers AS groupusers " +
	                	"WHERE ( groupusers.groupid = "+groupid+" ) AND ( ( groupusers.status = 0 ) OR ( groupusers.status = 1 ) OR ( groupusers.status = 2 ) ) "+		           			
		           	") AS groupusers ON user.server_id = groupusers.userid "+
	                "WHERE ( ( user.status = 3 ) AND (user.server_id != "+Session.getUserId()+" ) AND (groupusers.userid IS NULL) ) "
		           	;
                                
                cursor=db.rawQuery(sql,null);
            	
            	
            } catch (SQLException e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID SQLException e2="+e);
            } catch (Exception e) {
            	Log.e(TAG, "query SPEC_GROUP_LOCATION_GROUPID Exception e3="+e);
            } 
        	
        	Log.d(TAG, "query uri=URI_SELECT_GROUP_ADD_USERS_GROUPID/"+groupid+" cursor.count="+cursor.getCount());
        	
        	return cursor;
        } 
        

        String user_server_id=null;
        String group_server_id=null;
        
        switch (uriMatcher.match(uri)) {            
            
            case USER:
                queryBuilder.setTables(DBContract.User.TABLE_NAME);
                break;
            case USER_SERVER_ID:
                queryBuilder.setTables(DBContract.User.TABLE_NAME);
                user_server_id = uri.getPathSegments().get(1);
                
                if(user_server_id.equals("0")){            		
                	user_server_id=String.valueOf(Session.getUserId());
            		Log.d(TAG, "query User_id replaced rom 0 to "+user_server_id);
            	}
                
                queryBuilder.appendWhere(DBContract.User.COLUMN_NAME_SERVER_ID + "=" + user_server_id);
                break;
                
            case GROUP:
                queryBuilder.setTables(DBContract.Group.TABLE_NAME);
                break;
                
            case GROUP_SERVER_ID:
                queryBuilder.setTables(DBContract.Group.TABLE_NAME);
                group_server_id = uri.getPathSegments().get(1);                
                queryBuilder.appendWhere(DBContract.Group.COLUMN_NAME_SERVER_ID + "=" + group_server_id);
                break;
                
            case GROUPUSERS_GROUPID:{
            	//Log.d("DBContentProvider","query GROUPUSERS_GROUPID");
            	
            	queryBuilder.setTables(DBContract.GroupUsers.TABLE_NAME);
                String groupid = uri.getPathSegments().get(1);                
                
                String table = "groupusers as r inner join user as u on r.userid = u.server_id";
                String columns[] = { "r._id as _id", "r.userid as userid", "r.status as status", "u.name as name", "u.url_icon as url_icon","u.url_avatar as url_avatar","u.url_full as url_full" };
                String sel = "r.groupid = ?";
                String[] selArgs = {groupid};
                Cursor c = db.query(table, columns, sel, selArgs, null, null, null);
                
                c.setNotificationUri(getContext().getContentResolver(),URI_GROUPUSERS);
                c.setNotificationUri(getContext().getContentResolver(),uri);
                
                
                return c;
                
            }
            
            case GROUPUSERS_GROUPID_USERID:{
                queryBuilder.setTables(DBContract.GroupUsers.TABLE_NAME);
                String groupid = uri.getPathSegments().get(1);
                String userid = uri.getPathSegments().get(2);
                queryBuilder.appendWhere(DBContract.GroupUsers.COLUMN_NAME_USERID + "=" + userid);                
                break;
            }
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,selectionArgs, null, null, sortOrder);

        switch (uriMatcher.match(uri)) {
                       
            case USER:
                cursor.setNotificationUri(getContext().getContentResolver(),URI_USER);
                break;  
            case USER_SERVER_ID:
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_USER);
            	cursor.setNotificationUri(getContext().getContentResolver(),Uri.parse(URI_USER+"/"+user_server_id));
            	break;
            	
            case GROUP:
                cursor.setNotificationUri(getContext().getContentResolver(),URI_GROUP);
                break;  
            case GROUP_SERVER_ID:
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_GROUP);
            	cursor.setNotificationUri(getContext().getContentResolver(),Uri.parse(URI_GROUP+"/"+group_server_id));
            	break;
            
            case GROUPUSERS_GROUPID_USERID:
            	cursor.setNotificationUri(getContext().getContentResolver(),URI_GROUPUSERS);
                cursor.setNotificationUri(getContext().getContentResolver(),Uri.parse(URI_GROUPUSERS+"/"+uri.getPathSegments().get(1)));
                cursor.setNotificationUri(getContext().getContentResolver(),uri);
                break;  
        }

        Log.d(TAG,"query Uri="+uri.toString());

        return cursor;

    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleteCount;
        switch (uriMatcher.match(uri)) {

        	//Delete all users
        	case USER:
        		deleteCount = db.delete(DBContract.User.TABLE_NAME, selection, selectionArgs);
        		getContext().getContentResolver().notifyChange(URI_USER, null);
        		break;
            
        	//Delete one user
            case USER_SERVER_ID:
                String user_server_id = uri.getPathSegments().get(1);
                //Delete user
                selection = DBContract.User.COLUMN_NAME_SERVER_ID + "=" + user_server_id;
                deleteCount = db.delete(DBContract.User.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_USER, null);
                getContext().getContentResolver().notifyChange(uri, null);

                break;
                
            //Delete all groups
            case GROUP:
                deleteCount = db.delete(DBContract.Group.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                getContext().getContentResolver().notifyChange(uri, null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                break;
                
              //Delete one group
            case GROUP_SERVER_ID:
                String group_server_id = uri.getPathSegments().get(1);
                //Delete user
                selection = DBContract.Group.COLUMN_NAME_SERVER_ID + "=" + group_server_id;
                deleteCount = db.delete(DBContract.Group.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);

                break;
                
            //Clear GroupUsers table (Be careful!)
            case GROUPUSERS:
                deleteCount = db.delete(DBContract.GroupUsers.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                break;
                
            //Delete all users in group    
            case GROUPUSERS_GROUPID:{
            	String groupid = uri.getPathSegments().get(1);
            	selection = DBContract.GroupUsers.COLUMN_NAME_GROUPID + "=" + groupid;
            	
                deleteCount = db.delete(DBContract.GroupUsers.TABLE_NAME, selection, selectionArgs);
                
                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid), null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                
                break;
            }
            //Delete user from group    
            case GROUPUSERS_GROUPID_USERID:{
            	String groupid = uri.getPathSegments().get(1);
            	String userid = uri.getPathSegments().get(2);
            	selection = "("+DBContract.GroupUsers.COLUMN_NAME_GROUPID + "=" + groupid+ ") "
            				+"AND ("+DBContract.GroupUsers.COLUMN_NAME_USERID + "=" + userid+")";
            	
                deleteCount = db.delete(DBContract.GroupUsers.TABLE_NAME, selection, selectionArgs);
                
                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid), null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid), null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }


        Log.d(TAG,"delete Uri="+uri.toString());
        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updateCount;

        switch (uriMatcher.match(uri)) {

            case USER_SERVER_ID:
                String user_server_id = uri.getPathSegments().get(1);
                selection = DBContract.User.COLUMN_NAME_SERVER_ID + "=" + user_server_id;
                updateCount = db.update(DBContract.User.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_USER, null);
                getContext().getContentResolver().notifyChange(URI_USER, null);
                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                
                break;
                
            case GROUP_SERVER_ID:
                String group_server_id = uri.getPathSegments().get(1);
                selection = DBContract.Group.COLUMN_NAME_SERVER_ID + "=" + group_server_id;
                updateCount = db.update(DBContract.Group.TABLE_NAME, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(URI_GROUP, null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);
                break;
                
            case GROUPUSERS:
            	            	
            	updateCount = db.update(DBContract.GroupUsers.TABLE_NAME, values, selection, selectionArgs);
            	
                Log.d("DBContentProvider","update "+uri);
                
                int groupid=values.getAsInteger(DBContract.GroupUsers.COLUMN_NAME_GROUPID);
                int userid=values.getAsInteger(DBContract.GroupUsers.COLUMN_NAME_USERID);

                getContext().getContentResolver().notifyChange(URI_GROUPUSERS, null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid), null);
                getContext().getContentResolver().notifyChange(Uri.parse(URI_GROUPUSERS+"/"+groupid+"/"+userid), null);
                getContext().getContentResolver().notifyChange(URI_CONTACTS_GROUPS, null);

                break;
            
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        Log.d(TAG,"update Uri="+uri.toString());

        getContext().getContentResolver().notifyChange(uri, null);
        
        return updateCount;
    }
    

}