package com.ivanov.tech.map.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.ivanov.tech.map.Map;
import com.ivanov.tech.session.Session;

public class FragmentMapGroup extends FragmentMapBase{

	private static final String TAG=FragmentMapGroup.class.getSimpleName();
	
	protected static final int LOADER_GROUP_LOCATION = 4;
	
	public int groupid;
	
	public static FragmentMapGroup newInstance(int groupid) {
		
		FragmentMapGroup f = new FragmentMapGroup();
		f.groupid=groupid;
		
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        getLoaderManager().initLoader(LOADER_GROUP_LOCATION, null, FragmentMapGroup.this);
		
    }
	
    @Override 
    public void onDestroy() { 
        super.onDestroy(); 
        Log.d(TAG, "onDestroy");
        
        getLoaderManager().destroyLoader(LOADER_GROUP_LOCATION);
    }

//----------------BaseMethod---------------------    
    
    @Override
    protected int getRecieverType(){
    	
    	return Map.RECIEVER_TYPE_GROUP;    	
    }    
    
    @Override
    protected int getRecieverClientId(){
    	return groupid;
    }
	    
//----------------Loaders-----------------
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
        switch(id) {
            
            case LOADER_GROUP_LOCATION:{
            	String[] projection=null;
                Uri uri=null;

                com.ivanov.tech.map.provider.DBContract.UserLocation UserLocation=null;
                
                projection = new String[]{
                        UserLocation._ID,//0
                        UserLocation.COLUMN_NAME_USER_ID,//1
                        UserLocation.COLUMN_NAME_LATITUDE,//2
                        UserLocation.COLUMN_NAME_LONGITUDE,//3
                        UserLocation.COLUMN_NAME_TIMESTAMP,//4
                        UserLocation.COLUMN_NAME_ACCURACY,//5
                        UserLocation.COLUMN_NAME_PROVIDER,//6
                        UserLocation.COLUMN_NAME_TIMESTAMP_LOCAL//7                        
                };
                
                uri = Uri.parse(com.ivanov.tech.map.provider.DBContentProvider.URI_GROUP_LOCATION+"/"+groupid);
                                
                Log.d(TAG, "onCreateLoader LOADER_GROUP_LOCATION uri="+uri.toString());
                
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        uri, projection, null, null, null);
                
                return cursorLoader;
            }
        }
        
        return super.onCreateLoader(id, args);//UsersLoader;
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	
    	super.onLoadFinished(loader, data);//UserLoader
        
    	switch(loader.getId()){
        	        	
        	case LOADER_GROUP_LOCATION:
        		
        		if(data!=null)Log.d(TAG, "onLoadFinished LOADER_GROUP_LOCATION count="+data.getCount());
        		else Log.d(TAG, "onLoadFinished LOADER_GROUP_LOCATION data=null");
        		
        		cursor_userlocation=data;
        		refreshMarkers();        		
        		
        		if(usersmap!=null)
        			if( ( !usersmap.containsKey(0) )&&( !usersmap.containsKey(Session.getUserId()) ) ){
        				//updateUser(Session.getUserId());
        				Log.e(TAG, "onLoadFinished usersmap doesn't contain user");
        			}
        				
                
        		break;                
        }
    	        
    }
    
}
