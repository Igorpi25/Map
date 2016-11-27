package com.ivanov.tech.map.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle; 
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View; 
import android.view.View.OnClickListener;
import android.view.ViewGroup; 
 


import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory; 
import com.google.android.gms.maps.GoogleMap; 
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer; 
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory; 
import com.google.android.gms.maps.model.LatLng; 
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions; 
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.ivanov.tech.map.MultiDrawable;
import com.ivanov.tech.map.Person;
import com.ivanov.tech.map.R;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.profile.provider.DBContentProvider;
import com.ivanov.tech.profile.provider.DBContract;
import com.ivanov.tech.session.Session;

public class FragmentMapBase extends DialogFragment implements 
	
	OnClickListener, 
	ClusterManager.OnClusterClickListener<Person>, 
	ClusterManager.OnClusterInfoWindowClickListener<Person>, 
	ClusterManager.OnClusterItemClickListener<Person>, 
	ClusterManager.OnClusterItemInfoWindowClickListener<Person>,	
	LoaderManager.LoaderCallbacks<Cursor> {
	
	protected static final String TAG=FragmentMapBase.class.getSimpleName();
	
    protected static final int LOADER_USER = 1;
    
    //usersmap value keys:
    protected static final int USERSMAP_NAME = 0;
    protected static final int USERSMAP_STATUS = 1;
    protected static final int USERSMAP_PATH_ICON = 2;
        
    //HashMap<"user_id",values>
    //values: name,status,path_icon
    protected HashMap<Integer,HashMap<Integer,String>> usersmap=null;
    public Map<Integer,Person> personsmap=new HashMap<Integer,Person>();
        
	public MapView mapview;
    public GoogleMap googlemap;
    public ClusterManager<Person> clustermanager;
    
    protected Cursor cursor_userlocation=null;
    
    protected ServiceConnection serviceGPSconnection;
    
    protected PersonRenderer personrenderer=null;
    
    public static FragmentMapBase newInstance() {
		
    	FragmentMapBase f = new FragmentMapBase();
		
        return f;
    }

    @Override 
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
                             Bundle savedInstanceState) { 
    	Log.d(TAG, "onCreateView");
    	
        View v = inflater.inflate( getLayoutId() , container, 
                false); 
        mapview = (MapView) v.findViewById( getMapViewId() ); 

        mapview.onCreate(savedInstanceState);   
        
        try { 
            MapsInitializer.initialize(getActivity().getApplicationContext()); 
        } catch (Exception e) { 
        	Log.e(TAG, "onCreateView Exception e="+e);
        }  
        googlemap = mapview.getMap(); 
        
        clustermanager = new ClusterManager<Person>(getActivity(), googlemap);
        personrenderer=new PersonRenderer();
        clustermanager.setRenderer(personrenderer);
        googlemap.setOnCameraChangeListener(clustermanager);
        googlemap.setOnMarkerClickListener(clustermanager);
        googlemap.setOnInfoWindowClickListener(clustermanager);
        clustermanager.setOnClusterClickListener(this);
        clustermanager.setOnClusterInfoWindowClickListener(this);
        clustermanager.setOnClusterItemClickListener(this);
        clustermanager.setOnClusterItemInfoWindowClickListener(this);
        
        setHasOptionsMenu(true);
        
        return v; 
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        getLoaderManager().initLoader(LOADER_USER, null, this);    
    }
 
    @Override 
    public void onDestroy() { 
        super.onDestroy(); 
        Log.d(TAG, "onDestroy");
        mapview.onDestroy(); 
        
        getLoaderManager().destroyLoader(LOADER_USER);
    } 
    
    @Override 
    public void onPause() { 
        super.onPause(); 
        Log.d(TAG, "onPause");
        mapview.onPause(); 
    } 
     
    @Override 
    public void onLowMemory() { 
        super.onLowMemory(); 
        Log.d(TAG, "onLowMemory");
        mapview.onLowMemory(); 
    }
    
    @Override 
    public void onResume() { 
        super.onResume(); 
        Log.d(TAG, "onResume");
        
        mapview.onResume(); 
    } 
 
    @Override
    public void onStart(){
    	super.onStart();
    	Log.d(TAG, "onStart");
    	
    	startMarkerStatusChecker();
    	
    	serviceGPSconnection=com.ivanov.tech.map.Map.bindServiceGPS(getActivity());
    	com.ivanov.tech.map.Map.ongoingStartRecieve(getActivity().getApplicationContext(), getRecieverType(), 0,getRecieverClientId());
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	Log.d(TAG, "onStop");
    	
    	stopMarkerStatusChecker();
    	
    	if(serviceGPSconnection!=null)
    		com.ivanov.tech.map.Map.unbindServiceGPS(getActivity(),serviceGPSconnection);
    	
    	com.ivanov.tech.map.Map.ongoingStopRecieve(getActivity().getApplicationContext());
    	
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
//		menu.clear();
//	
//		((AppCompatActivity)getActivity()).getSupportActionBar().show();
//		((AppCompatActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
//		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Map base");
    }
    
//----------Markers---------------------
    
    public class PersonRenderer extends DefaultClusterRenderer<Person> {
        
    	private final IconGenerator mIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getActivity().getApplicationContext(), googlemap, clustermanager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity().getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
            
        }

        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            
        	if(person.urlbitmap==null){
        		mImageView.setImageResource(person.ic_missing);        		
                Bitmap icon = mIconGenerator.makeIcon();
                
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
                
        		loadMarkerIcon(person,markerOptions);
        	}else{
        		mImageView.setImageBitmap(person.urlbitmap);
                Bitmap icon = mIconGenerator.makeIcon();
                
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        	}
        	
        	if(pastTime(person.timestamp)*1000>=activetime)
        		markerOptions.alpha(0.3f);
			else
				markerOptions.alpha(1.0f);
            
        }
        
        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
        	
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return false;//cluster.getSize() > 1;
        }
            
        private void loadMarkerIcon(final Person person, final MarkerOptions markerOptions) {

        	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon");
        	        	
        	Glide.with(getActivity()).load(person.url_icon).asBitmap().fitCenter().placeholder(R.drawable.turtle).error(R.drawable.stefan).into(new SimpleTarget<Bitmap>(){

        		@Override
        		public void onResourceReady(Bitmap bitmap,
        				GlideAnimation<? super Bitmap> glideanimation) {
        			Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" WxH="+bitmap.getWidth()+"x"+bitmap.getHeight());
        			
        			person.urlbitmap=bitmap;
        			
        			mImageView.setImageBitmap(person.urlbitmap);        			
                    Bitmap icon = mIconGenerator.makeIcon();
        			
                    if(PersonRenderer.this.getMarker(person)!=null){
                    	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" getMarker OK");
                    	PersonRenderer.this.getMarker(person).setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                    }else{
                    	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" getMarker=null");
                    	markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
                    }
        			
        		}
            	
            });
        }
        
        private void loadClusterIcon(final Person person, final Cluster<Person> cluster) {
        	
        	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon");
        	        	
        	Glide.with(getActivity()).load(person.url_icon).asBitmap().fitCenter().into(new SimpleTarget<Bitmap>(){

        		@Override
        		public void onResourceReady(Bitmap bitmap,
        				GlideAnimation<? super Bitmap> glideanimation) {
        			Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" WxH="+bitmap.getWidth()+"x"+bitmap.getHeight());
        			
        			person.urlbitmap=bitmap;
        			
        			PersonRenderer.this.getMarker(cluster).setIcon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)));
        			
        		}
            	
            });
        }
        
        private Bitmap getClusterIcon(Cluster<Person> cluster){
        	// Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                
                if(p.urlbitmap==null){
                	Drawable drawable = getResources().getDrawable(p.ic_missing);
                	drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                    
                    loadClusterIcon(p,cluster);
                }else{
                	BitmapDrawable bitmapdrawable = new BitmapDrawable(p.urlbitmap);
                	bitmapdrawable.setBounds(0, 0, width, height);
                    profilePhotos.add(bitmapdrawable);
                }
                                
                
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            
            return icon;
        }
                
    }
    
    public void refreshMarkers(){
    	Log.d(TAG, "refreshMarkers");
    	
    	if( (cursor_userlocation==null)||(usersmap==null) )return;
    	
    	int userid,  provider;
    	double latitude, longitude;
    	long timestamp,timestamplocal;
    	float accuracy;
    	
    	cursor_userlocation.moveToPosition(-1);
    	
    	Log.d(TAG,"refreshMarkers cursor_userlocation.count="+cursor_userlocation.getCount());
    	
    	while( cursor_userlocation.moveToNext() ) {
    		    		
            userid=cursor_userlocation.getInt(1);//COLUMN_NAME_USER_ID
    		latitude=cursor_userlocation.getDouble(2);//COLUMN_NAME_LATITUDE
    		longitude=cursor_userlocation.getDouble(3);//COLUMN_NAME_LONGITUDE
    		timestamp=cursor_userlocation.getLong(4);//COLUMN_NAME_TIMESTAMP
    		accuracy=cursor_userlocation.getFloat(5);//COLUMN_NAME_ACCURACY
    		provider=cursor_userlocation.getInt(6);//COLUMN_NAME_PROVIDER
    		
    		//Нужен для определения времени с последнего обновления
    		timestamplocal=cursor_userlocation.getLong(7);//COLUMN_NAME_TIMESTAMP_LOCAL
    		
    		Log.d(TAG, "refreshMarkers userid="+userid);
    		
    		if(personsmap.containsKey(userid)){
    			
    			Person person=personsmap.get(userid);    			
    			person.accuracy=accuracy;
    			person.timestamp=timestamplocal;
    			person.provider=provider;
    			
    			Log.d(TAG, "refreshMarkers update person user.id"+userid+" user.name="+person.name+" accuracy="+accuracy);
    			
    			clustermanager.cluster();
    			if(!position(latitude,longitude).equals(person.position)){
    				moveMarker(person, personrenderer.getMarker(person), position(latitude,longitude));
    				
    				if(pastTime(person.timestamp)*1000>=activetime)
    					invalidateMarker(personrenderer.getMarker(person));
    				else
    					validateMarker(personrenderer.getMarker(person));
    			}
    			
    			
    		}else{
    			Log.d(TAG, "refreshMarkers add person userid="+userid);
    			
    			HashMap<Integer,String> user=usersmap.get(userid);
    			
    			if(user!=null){
    				Log.d(TAG, "refreshMarkers add person user.id"+userid+" user.name="+user.get(USERSMAP_NAME)+" accuracy="+accuracy);
    				//updateUser(userid);
    				
    				Person person=new Person(userid, position(latitude,longitude), accuracy, timestamplocal, user.get(USERSMAP_NAME), R.drawable.ic_no_icon,user.get(USERSMAP_PATH_ICON));
    			
    				personsmap.put(userid, person);    			
    				clustermanager.addItem(person);
    				clustermanager.cluster();
    			
	    			if(userid==Session.getUserId()){
	    				Log.d(TAG, "refreshMarkers add person moveCamera");
	    				googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(position(latitude,longitude), 17.5f));
	    			}
	    			
    			}else{
    				Log.e(TAG, "refreshMarkers refreshMarkers add person user=null");
    				//updateUser(userid);
    				
    			}
    		}
    	}
    	
    	
    	
    	//clustermanager.g
    }
    
    public void moveMarker(final Person person,final Marker marker, final LatLng toPosition) {
    	
    	
//    	person.position=toPosition;
//    	marker.setPosition(toPosition);
//    	if(true)return;
    	
    	if((marker==null)||(person==null))return;
    	
//    	//Если кто-то уже двигает маркер, то отбой
//    	if( (!marker.isVisible()) || (!marker.getPosition().equals(person.position)) ){            		
//    		Log.d(TAG,"moveMarker already moving");
//    		return;
//    	}
    	
    	//На случай если кто-то потребует координаты person
    	person.position=toPosition;
    	
    	//Запоминаем где находится маркер, чтобы знать если кто нибудь его тронет
    	person.lastmarkerposition=marker.getPosition();
    	    		
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googlemap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        
        final long duration = 200;
        
        final Interpolator interpolator = new LinearInterpolator();
        
        handler.post(new Runnable() {
            @Override
            public void run() {
            	if((marker==null)||(person==null))return;
            	
            	//Если кто-то вмешался в координаты маркера, то сразу останавливаемся
            	if( (!marker.isVisible()) || (!marker.getPosition().equals(person.lastmarkerposition)) ){
            		Log.d(TAG,"moveMarker somebody moved");
            		return;
            	}
            	
            	long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;                
                
                marker.setPosition(new LatLng(lat, lng));
                person.lastmarkerposition=new LatLng(lat, lng);

                if (t < 1.0) {
                    // Post again 16ms later.                    
                	handler.postDelayed(this, 16);
                } 
            }
        });
        
        
            
    }
    
//--------------------MarkerStatusChecker------------------------------
    
    public final Handler handler_markerstatuschecker = new Handler();
    public final long interval=10*1000;
    
    public final long activetime=2*60*1000;
    public final long updateselftime=30*1000;
    
    public void startMarkerStatusChecker(){
    	handler_markerstatuschecker.post(runnable_markerstatuschecker);
    }
    
    public void stopMarkerStatusChecker(){
    	handler_markerstatuschecker.removeCallbacks(runnable_markerstatuschecker);
    }
    
    public Runnable runnable_markerstatuschecker=new Runnable() {
        @Override
        public void run() {
        	checkMarkersStatus();
        	
        	handler_markerstatuschecker.postDelayed(runnable_markerstatuschecker,interval);
        }
    };
    
    public void checkMarkersStatus(){
    	Log.d(TAG, "checkMarkersStatus");
    	
    	if((personsmap!=null)&&(personrenderer!=null))
    	
    	for(Entry<Integer,Person> entry : personsmap.entrySet()){
    		
    		Person person=entry.getValue();    		
    		
    		if( (person.user_id==Session.getUserId())&&(pastTime(person.timestamp)*1000>=updateselftime) ){
    			com.ivanov.tech.map.Map.startServiceGPSForCurrentBestLocation(getActivity());
    		}
    		
    		if(pastTime(person.timestamp)*1000>=activetime){
    			Marker marker=personrenderer.getMarker(entry.getValue());
    			invalidateMarker(marker);        		
    		}
    	}
    }
    
    public void validateMarker(Marker marker){
    	Log.d(TAG, "validateMarker");
		if(marker!=null){
			marker.setVisible(true);
			marker.setAlpha(1.0f);
		}
    }
    
    public void invalidateMarker(Marker marker){
    	Log.d(TAG, "invalidateMarker");
    	marker.setVisible(false);
    	//if(marker!=null)marker.setAlpha(0.3f);Выключаем в рамках хакатона
    }
    
//-------------------------------------------------------------------
    
    public LatLng position(double latitude, double longitude) {
        return new LatLng(latitude,longitude);
    }
        
//----------OnClusterClickListener----------------------------
    
    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(getActivity(), cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
                
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person person) {
        
    	//Прошедшее время в секундах
    	long pasttime=pastTime(person.timestamp);
    	
    	String text;
    	if(pasttime<60){    		
    		text="Updated "+pasttime+" seconds ago";    		
    	}else {
    		text="Updated "+pasttime/60+" minites "+((pasttime%60>0)?pasttime%60+" seconds":"")+" ago";
    	}
    	
    	personrenderer.getMarker(person).setSnippet( text );
    	
    	return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person person) {
    	//showUserProfile(person.user_id);
    }

//--------------Utils--------------------

    protected long pastTime(long timestamp){    
    	Log.d(TAG, "pastTime timestamp="+timestamp+" current="+System.currentTimeMillis()+"pastTime="+(System.currentTimeMillis() - timestamp));
    	return (System.currentTimeMillis() - timestamp)/1000;
    }
    
//-------------Base Methods-------------------
    
    protected int getLayoutId() {
        return R.layout.fragment_base;
    }
        
    protected int getMapViewId() {
        return R.id.fragment_base_supportmapfragment;
    }

    protected int getRecieverType(){
    	return 0;
    }

    protected int getRecieverClientId(){
    	return 0;
    }
    
//-----------------Demo-----------------------    
    
    @Override
	public void onClick(View v) {
				
	}
        
//------------Users------------------------------
    
    public HashMap<Integer,HashMap<Integer,String>> convertUsersCursorToHashMap(Cursor usersCursor){
    	HashMap<Integer,HashMap<Integer,String>> data = new HashMap<Integer,HashMap<Integer,String>>();
         
    	Log.d(TAG, "convertUsersCursorToHashMap usersCursor.count="+usersCursor.getCount());
    	    	
        usersCursor.moveToPosition(-1);
        while( usersCursor.moveToNext() ){
        	HashMap<Integer,String> value = new HashMap<Integer,String>();
            try{            
	        	value.put(USERSMAP_NAME, usersCursor.getString(usersCursor.getColumnIndex(DBContract.User.COLUMN_NAME_NAME)));
	        	value.put(USERSMAP_STATUS, String.valueOf(usersCursor.getInt(usersCursor.getColumnIndex(DBContract.User.COLUMN_NAME_STATUS))) );
	        	value.put(USERSMAP_PATH_ICON, usersCursor.getString(usersCursor.getColumnIndex(DBContract.User.COLUMN_NAME_URL_ICON)));
	        	
	        	
            }catch(NullPointerException e){
            	//вот это может быть нужно updateUser(usersCursor.getInt(usersCursor.getColumnIndex(DBContract.User.COLUMN_NAME_SERVER_ID)));
            	Log.e(TAG, "NullPointerException convertUsersCursorToHashMap e="+e);
            	return data;
            }
            
            data.put(usersCursor.getInt(usersCursor.getColumnIndex(DBContract.User.COLUMN_NAME_SERVER_ID)),value);
        }
        
        return data;
    }

    protected void buildUsers(Cursor usersCursor){
    	Log.d(TAG, "buildUsers");
    	
    	usersmap=convertUsersCursorToHashMap(usersCursor);
    	
    }
      
//----------------UserLoaders-----------------
	
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	       
        switch(id) {
            case LOADER_USER: {
            	String[] projection=null;
                Uri uri=null;

                projection = new String[]{
                        DBContract.User._ID,
                        DBContract.User.COLUMN_NAME_SERVER_ID,
                        DBContract.User.COLUMN_NAME_NAME,
                        DBContract.User.COLUMN_NAME_STATUS,
                        DBContract.User.COLUMN_NAME_URL_ICON,                        
                };
                uri = DBContentProvider.URI_USER;
                                
                Log.d(TAG, "onCreateLoader LOADER_USER uri="+uri.toString());
                
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        uri, projection, null, null, null);
                
                return cursorLoader;
            }
            
        }
        
        return null;
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	        
    	switch(loader.getId()){            
        	
    		case LOADER_USER:
        		if(data!=null){
        			Log.d(TAG, "onLoadFinished LOADER_USER count="+data.getCount());
        			buildUsers(data);
            		refreshMarkers();
        		}else{ 
        			Log.d(TAG, "onLoadFinished LOADER_USER data=null");
        		}
        		
        		break;
        	
        }
    	        
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    	 //Log.d(TAG, "LOADER_USER onLoaderReset");    
    }
	
}
