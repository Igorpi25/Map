package com.ivanov.tech.map.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.util.Log;

import com.ivanov.tech.communicator.service.TransportBase;
import com.ivanov.tech.map.Map;
import com.ivanov.tech.map.provider.DBContentProvider;
import com.ivanov.tech.map.provider.DBContract;
import com.ivanov.tech.session.Session;

public class TransportMap extends TransportBase implements OnLoadCompleteListener<Cursor>{

	private static final String TAG = TransportMap.class
            .getSimpleName();    
    
    //INCOMING_COORS values
	public static final String JSON_TRANSPORT="transport";
    public static final String JSON_TYPE="type";
    
    //JSON_TYPE is INCOMING_COORS or OUTGOING_COORS
    private static final String JSON_USER_ID="userid";
    private static final String JSON_LATITUDE="latitude";
    private static final String JSON_LONGITUDE="longitude";
    private static final String JSON_ACCURACY="accuracy";
    private static final String JSON_PROVIDER="provider";
    private static final String JSON_TIMESTAMP="timestamp";
    
    public static final String JSON_RADIUS="radius";
    public static final String JSON_RECIEVER_TYPE="reciever_type";
    public static final String JSON_RECIEVER_CLIENTID="clientid";
    
    private static final int LOADER_MY_LOCATION=3;
    
    protected int onconnect_reciever_type=0;//If not 0 send ongoingStartReciever
    protected int onconnect_reciever_clientid=0;
    
    CursorLoader mCursorLoader=null;
    android.content.ServiceConnection serviceconnection_toServiceGPS=null;
        
    public TransportMap(Context context) {    		
		super(context);
	}
    
//------------ConnectionService------------------------    
    
    @Override
    public void onCommunicatorServiceCreate() {	

    	
		Log.d(TAG, "onConnectionServiceCreate");
		
		if(mCursorLoader==null){
 			mCursorLoader = createLoader(LOADER_MY_LOCATION);
 			mCursorLoader.registerListener(LOADER_MY_LOCATION, this);
 		}
 		
	    mCursorLoader.startLoading();
	    
	}
    
    @Override
    public void onCommunicatorServiceDestroy() {	   
    	
		Log.d(TAG, "onConnectionServiceDestroy");
		
		if(mCursorLoader!=null){
 			mCursorLoader.stopLoading();
 		}
		
		if(serviceconnection_toServiceGPS!=null){
    		Map.unbindServiceGPS(this,serviceconnection_toServiceGPS);
    		serviceconnection_toServiceGPS=null;
		}
		
	}

//------------TransportProtocol------------------
    
    @Override
  	public boolean onOutgoingMessage(int transport, JSONObject json){
    	     
    	Log.d(TAG, "onOutgoingMessage transport="+transport+" json="+json);
	    
	    //Если это сообщение связанное с картой
	    if( transport==Map.TRANSPORT_MAP ) {
	    	
	    	try {
	    	
	    	switch(json.getInt(JSON_TYPE)){	        		
        	
        	case Map.OUTGOING_START_RECIEVE:{
        		
        		//Start recieve when connect        		
				onconnect_reciever_type=json.getInt(JSON_RECIEVER_TYPE);				
				onconnect_reciever_clientid=json.getInt(JSON_RECIEVER_CLIENTID);
        		
        		
        		outgoingStartRecieve(onconnect_reciever_type,0,onconnect_reciever_clientid);
        		
        		}break;
        		
	        case Map.OUTGOING_STOP_RECIEVE:{
	        	
	        	//Dont recieve when connect
	        	onconnect_reciever_type=0;
	        	
	        	outgoingStopRecieve();		
        		
	        	}break;
	        	
	        case Map.OUTGOING_COORS:{
	    	
	        					
		    	Log.d(TAG, "onOutgoingMessage OUTGOING_COORS json="+json);
		    	
				sendMessage(Map.OUTGOING_COORS,0, json);	
				
				break;
				}
	    	}
	    	
	    	} catch (JSONException e) {
	    		Log.d(TAG, "onOutgoingMessage JSONException e="+e);
			}
	    	
	    	return true;
	    }
	    
	    return false;
	}
    
    public boolean onIncomingMessage(int transport, JSONObject json){
		Log.d(TAG, "onIncomingMessage transport="+transport+" json="+json);
		
		if(transport==Map.TRANSPORT_MAP){
        	
        	try {
        		
				switch(json.getInt(JSON_TYPE)){	        		
					
				case Map.INCOMING_START_BROADCAST:
					Log.d(TAG, "onIncomingMessage INCOMING_START_BROADCAST");
					
					if(serviceconnection_toServiceGPS==null)
						serviceconnection_toServiceGPS=Map.bindServiceGPS(this);
					
					if(mCursorLoader!=null)
						mCursorLoader.forceLoad();
					
					break;
					
				case Map.INCOMING_STOP_BROADCAST:
					Log.d(TAG, "onIncomingMessage INCOMING_STOP_BROADCAST");
					
					if(serviceconnection_toServiceGPS!=null){
						Map.unbindServiceGPS(this,serviceconnection_toServiceGPS);
						serviceconnection_toServiceGPS=null;
					}
					
					break;
					
				case Map.INCOMING_COORS:
					
					Log.d(TAG, "onIncomingMessage INCOMING_COORS json="+json);
					
					if(json.has(JSON_TIMESTAMP)){
						long date_timestamp=json.getLong(JSON_TIMESTAMP);
						//Переводим из секунд (на сервере Linux UTC в секундах) к миллисикундам(Андроид-клиенте timestamp в миллисекундах)
						date_timestamp=date_timestamp*1000;
						//Подменяем JSON
						json.remove(JSON_TIMESTAMP);
						json.put(JSON_TIMESTAMP, date_timestamp);		        		
					}
					
					int userid=json.getInt(JSON_USER_ID);
					
					
					updateUserLocation(json);
					
				    break;
				
				case Map.INCOMING_START_RECIEVE_CONFIRM:	        	
					Log.d(TAG, "onIncomingMessage INCOMING_START_RECIEVE_CONFIRM");
					
					break;
				
				}//switch end
				
			} catch (JSONException e) {
				Log.d(TAG, "onIncomingMessage JSONException e="+e);
			}
        	
        	return true;
        }
		
		return false;
	}

    @Override
   	public void onOutgoingFailed(int outgoing_failed_type, int message_id) {
   		
       	switch(outgoing_failed_type){
   			case Map.OUTGOING_START_RECIEVE: 
   				Log.e(TAG, "onOutgoingFailed OUTGOING_START_RECIEVE");
   				break;
   			case Map.OUTGOING_STOP_RECIEVE: 
   				Log.e(TAG, "onOutgoingFailed OUTGOING_STOP_RECIEVE");
   				break;				
   			case Map.OUTGOING_COORS:
   				Log.e(TAG, "onOutgoingFailed OUTGOING_COORS");
   				break;
   		}
   	}	
    
//------------WebsocketClientListener------------------------
                
    @Override
    public void onConnect() {
    	super.onConnect();
    	Log.d(TAG, "onConnect");
		
    	if(onconnect_reciever_type>0){
    		outgoingStartRecieve(onconnect_reciever_type,0,onconnect_reciever_clientid);
    	}	    
    }
    
    @Override
    public void onDisconnect(int code, String reason) {
        super.onDisconnect(code, reason);                
        Log.d(TAG, "onDisconnect");  
        
    }
    
    @Override
    public void onError(Exception error) {
    	super.onError(error);        
        Log.d(TAG, "onError");
        
        if(serviceconnection_toServiceGPS!=null){
    		Map.unbindServiceGPS(this,serviceconnection_toServiceGPS);
    		serviceconnection_toServiceGPS=null;
        }
    }
    
//------------------Outgoing Messages-------------------
    
    protected void outgoingStartRecieve(int reciever_type, float radius, int clientid){
    	JSONObject json=new JSONObject();
		try {
			json.put(JSON_TRANSPORT, Map.TRANSPORT_MAP);
			json.put(JSON_TYPE, Map.OUTGOING_START_RECIEVE);
			json.put(JSON_RECIEVER_TYPE, reciever_type);			
			json.put(JSON_RADIUS, 0);//radius in meters, where I will search friends//allways 0 in this version			
			json.put(JSON_RECIEVER_CLIENTID, clientid);
			
		}catch(JSONException e) {
			Log.e(TAG, "outgoingStartRecieve OUTGOING_START_RECIEVE JSONException e="+e);
		}
		
		Log.d(TAG, "outgoingStartRecieve OUTGOING_START_RECIEVE json="+json.toString());
		
		sendMessage(Map.TRANSPORT_MAP,Map.OUTGOING_START_RECIEVE, json);		
		
    }
    
    protected void outgoingStopRecieve(){
    	JSONObject json=new JSONObject();
		try {
			json.put(JSON_TRANSPORT, Map.TRANSPORT_MAP);
			json.put(JSON_TYPE, Map.OUTGOING_STOP_RECIEVE);
								
		}catch(JSONException e) {
			Log.e(TAG, "outgoingStopRecieve OUTGOING_STOP_RECIEVE JSONException e="+e);
		}
				
		Log.d(TAG, "outgoingStopRecieve OUTGOING_STOP_RECIEVE json="+json.toString());
		
		sendMessage(Map.TRANSPORT_MAP,Map.OUTGOING_STOP_RECIEVE, json);
    }
 
//------------DB Methods--------------------------
	
 	void updateUserLocation(JSONObject json) {
 		  		
 		//Log.d(TAG, "updateUserLocation json="+json);
 		
 		ContentValues values=new ContentValues();	
 		int userid=-1;
		
		try {			
			userid=json.getInt(JSON_USER_ID);
			values.put(DBContract.UserLocation.COLUMN_NAME_USER_ID, userid );
			values.put(DBContract.UserLocation.COLUMN_NAME_LATITUDE, json.getDouble(JSON_LATITUDE) );	
			values.put(DBContract.UserLocation.COLUMN_NAME_LONGITUDE, json.getDouble(JSON_LONGITUDE) );
			values.put(DBContract.UserLocation.COLUMN_NAME_ACCURACY, json.getDouble(JSON_ACCURACY) );
			values.put(DBContract.UserLocation.COLUMN_NAME_PROVIDER, json.getInt(JSON_PROVIDER) );
			values.put(DBContract.UserLocation.COLUMN_NAME_TIMESTAMP, json.getLong(JSON_TIMESTAMP) );
			values.put(DBContract.UserLocation.COLUMN_NAME_TIMESTAMP_LOCAL, System.currentTimeMillis() );
		} catch (JSONException e) {
			Log.e(TAG, "updateUserLocation JSONException e="+e);
		}
		
		
		if(getContentResolver().update(Uri.parse(DBContentProvider.URI_USER_LOCATION+"/"+userid), values, null, null)==0){
	 		getContentResolver().insert(DBContentProvider.URI_USER_LOCATION, values);
	 	}
	}
 	
//--------------Utils-----------------------------
 	
 	private void sendOnePoint(Cursor cursor){
 		//Log.d(TAG, "sendOnePoint count="+cursor.getCount());
 		
 		if(cursor.getCount()==0)return;
 		
		cursor.moveToFirst();
		
		JSONObject json=new JSONObject();
		try {
			json.put(JSON_TRANSPORT, Map.TRANSPORT_MAP);
			json.put(JSON_TYPE, Map.OUTGOING_COORS);
				
			json.put(JSON_USER_ID, Session.getUserId());
			
			json.put(JSON_LATITUDE, cursor.getDouble(cursor.getColumnIndex(DBContract.UserLocation.COLUMN_NAME_LATITUDE)));
			json.put(JSON_LONGITUDE, cursor.getDouble(cursor.getColumnIndex(DBContract.UserLocation.COLUMN_NAME_LONGITUDE)));
			json.put(JSON_ACCURACY, cursor.getFloat(cursor.getColumnIndex(DBContract.UserLocation.COLUMN_NAME_ACCURACY)));
			json.put(JSON_PROVIDER, cursor.getInt(cursor.getColumnIndex(DBContract.UserLocation.COLUMN_NAME_PROVIDER)));
									
		}catch(JSONException e) {
			Log.e(TAG, "sendOnePoint JSONException_2 e="+e);
		}
		
		Log.d(TAG, "sendOnePoint OUTGOING_COORS json="+json.toString());
				
		sendMessage(Map.TRANSPORT_MAP,Map.OUTGOING_COORS, json);	
			
	}
 	
//-----------------Time Utils---------------------
 	
//---------------Timestamp Utilities----------------------------
 	
 	private String timestampToString(long timestamp){
		
	 	Date date=new Date(timestamp);
	 	
	 	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 	String asd=format.format(date);
	 	
	 	//Log.d(TAG, "timestampToString timestamp="+timestamp+" asd="+asd);
	 	
	 	return asd;
	}
 	
 	private long stringToTimestamp(String date_time_string){
		
	 	long timestamp=Timestamp.valueOf(date_time_string).getTime();
	 	
	 	//Log.d(TAG, "stringToTimestamp string="+date_time_string+" timestamp="+timestamp);
	 	
	 	return timestamp;
	}
 	
//-------------Loader<Cursor>------------------
	
    public CursorLoader createLoader(int id) {
    	
    	Log.d(TAG, "createLoader");

        String[] projection=null;
        Uri uri=null;

        switch(id) {
            case LOADER_MY_LOCATION:
                projection = new String[]{
                        DBContract.UserLocation.COLUMN_NAME_TIMESTAMP,
                        DBContract.UserLocation.COLUMN_NAME_LATITUDE,
                        DBContract.UserLocation.COLUMN_NAME_LONGITUDE,
                        DBContract.UserLocation.COLUMN_NAME_ACCURACY,
                        DBContract.UserLocation.COLUMN_NAME_PROVIDER                        
                };
                
                uri = DBContentProvider.URI_MY_LOCATION;
                
                break;
            
        }
        
        CursorLoader cursorLoader = new CursorLoader(this,
                uri, projection, null, null, null);
        
        return cursorLoader;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor data) {
    	
        switch(loader.getId()){
            case LOADER_MY_LOCATION:
            	//Log.d(TAG, "onLoadComplete LOADER_MY_LOCATION");            	
            	sendOnePoint(data);            	
                break;            
        }
    }

}
