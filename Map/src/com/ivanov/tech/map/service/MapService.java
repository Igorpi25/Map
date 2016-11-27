package com.ivanov.tech.map.service;

import java.util.ArrayList;

import com.ivanov.tech.map.service.TransportMap;
import com.ivanov.tech.profile.service.TransportProfile;
import com.ivanov.tech.communicator.service.TransportBase;

public class MapService extends com.ivanov.tech.communicator.service.CommunicatorService{

	public final static String URL_DOMEN="igorpi25.ru";//Websocket server URL and port
    public final static String URL_SERVER="ws://"+URL_DOMEN+":8007";//Websocket server URL and port
    public final static String URL_START_SERVER="http://"+URL_DOMEN+"/v3/communicator/start";
    
	@Override
	public ArrayList<TransportBase> createTransports() {
		
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportProfile transportprofile=new TransportProfile(this);
		
		TransportMap transportmap=new TransportMap(this);		
		transports.add(transportmap);
		transports.add(transportprofile);
		
		return transports;
	}

	@Override
	public String getServerUrl() {
		// TODO Auto-generated method stub
		return URL_SERVER;
	}

	@Override
	public String getRestartServerUrl() {		
		return URL_START_SERVER;
	}

	@Override
	public String getCommunicatorServiceClass() {		
		return MapService.class.getCanonicalName();
	}

}