package com.qburst.docphin.app;


import android.app.Application;
import android.content.SharedPreferences;

public class DocphinApp extends Application {

	private static DocphinApp _sharedApplication;
	
	@Override
	public void onCreate() {
		super.onCreate();
		_sharedApplication = this;
		
	}
	
	public static DocphinApp getSharedApplication(){
        return _sharedApplication;
    }
	
	/**
     * @return SharedPreferences object
     */
    public static SharedPreferences getAppPreferences(){

        SharedPreferences preferences =
        		DocphinApp.getSharedApplication().getSharedPreferences(
                        DocphinConstants.kSharedPreference, MODE_WORLD_READABLE);
        return preferences;
    }

    public void saveSessionToken(String sessionToken){
        SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
        prefsEditor.putString(DocphinConstants.kSessionToken, sessionToken);
        prefsEditor.commit();
    }
    
    public void saveLastLoginTime(String currentTimeInMilliseconds){
        SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
        prefsEditor.putString(DocphinConstants.kLastLoginKey, currentTimeInMilliseconds);
        prefsEditor.commit();
    }
    
    public String getSessionToken(){
        return getAppPreferences().getString(DocphinConstants.kSessionToken, "");
    }
    
    public String getLastLoginTime(){
        return getAppPreferences().getString(DocphinConstants.kLastLoginKey, "");
    }
    
    

	

	
	
}
