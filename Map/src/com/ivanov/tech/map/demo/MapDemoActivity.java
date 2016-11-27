package com.ivanov.tech.map.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.ivanov.tech.communicator.Communicator;
import com.ivanov.tech.connection.Connection.ProtocolListener;
import com.ivanov.tech.map.Map;
import com.ivanov.tech.map.R;
import com.ivanov.tech.map.service.MapService;
import com.ivanov.tech.map.ui.FragmentMapGroup;
import com.ivanov.tech.map.ui.FragmentMapOneUser;
import com.ivanov.tech.profile.Profile;
import com.ivanov.tech.session.Session;

public class MapDemoActivity extends AppCompatActivity{
	
	//Profile URLs
	private static final String url_server = "http://igorpi25.ru/v3/";	
		
	private static final String url_searchcontact = url_server+"search_contact";
	public static final String url_avatarupload = url_server+"avatars/upload";
	public static final String url_grouppanoramaupload = url_server+"group_panorama/upload";
	private static final String url_creategroup = url_server+"create_group";	
		
	//Session URLs
	static final String url_testapikey=url_server+"testapikey";
	static final String url_login=url_server+"login";
	static final String url_register=url_server+"register";
			
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Session.Initialize(getApplicationContext(),url_testapikey,url_login,url_register);
        Profile.Initialize(getApplicationContext(),url_searchcontact,url_avatarupload,url_grouppanoramaupload,url_creategroup);
        Map.Initialize(getApplicationContext());
        Communicator.Initialize(getApplicationContext(), MapService.URL_SERVER, MapService.URL_START_SERVER, MapService.class.getCanonicalName());
        
        setContentView(R.layout.activity_main);
                
        showDemo();
        
    }

    private void showFragment(Fragment currentFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_container, currentFragment)
                .commit();
    }

    public void showDemo(){
    	Session.checkApiKey(this, getSupportFragmentManager(), R.id.main_container, new ProtocolListener(){

			@Override
			public void onCanceled() {
				finish();
			}

			@Override
			public void isCompleted() {
				
				Map.startMapService(MapDemoActivity.this);
				
				showFragment( FragmentMapGroup.newInstance(1) );//Session.getUserId()
			}
        });
    }
}
