package com.ivanov.tech.map.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.ivanov.tech.map.Map;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;

public class FragmentMapOneUser extends FragmentMapBase{

	private static final String TAG=FragmentMapOneUser.class.getSimpleName();
	
	protected static final int LOADER_ONE_USER_LOCATION = 3;
	
	public int clientid;
	
	public static FragmentMapOneUser newInstance(int clientid) {
		
		FragmentMapOneUser f = new FragmentMapOneUser();
		f.clientid=clientid;
		
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        getLoaderManager().initLoader(LOADER_ONE_USER_LOCATION, null, FragmentMapOneUser.this);
		
    }
	
    @Override 
    public void onDestroy() { 
        super.onDestroy(); 
        Log.d(TAG, "onDestroy");
        
        getLoaderManager().destroyLoader(LOADER_ONE_USER_LOCATION);
    }

//----------------BaseMethod---------------------    
    
    @Override
    protected int getRecieverType(){
    	
    	return Map.RECIEVER_TYPE_ONE_USER;    	
    }
    
    @Override
    protected int getRecieverClientId(){
    	return clientid;
    }
	    
//----------------Loaders-----------------
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
        switch(id) {
            
            case LOADER_ONE_USER_LOCATION:{
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
                
                uri = Uri.parse(com.ivanov.tech.map.provider.DBContentProvider.URI_ONE_USER_LOCATION+"/"+clientid);
                                
                Log.d(TAG, "onCreateLoader LOADER_ONE_USER_LOCATION uri="+uri.toString());
                
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
        	        	
        	case LOADER_ONE_USER_LOCATION:
        		
        		if(data!=null)Log.d(TAG, "onLoadFinished LOADER_ONE_USER_LOCATION count="+data.getCount());
        		else Log.d(TAG, "onLoadFinished LOADER_ONE_USER_LOCATION data=null");
        		
        		cursor_userlocation=data;
        		refreshMarkers();
        		
        		break;                
        }
    	        
    }
    
}