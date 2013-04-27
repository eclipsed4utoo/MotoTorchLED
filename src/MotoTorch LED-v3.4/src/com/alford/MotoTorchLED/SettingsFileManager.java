package com.alford.MotoTorchLED;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsFileManager {

	public static final String PREFS_NAME = "MyPrefsFile";
	SharedPreferences settings;
	Context m_context;
	
	public SettingsFileManager(Context context){
		m_context = context;
	}
	
	public void SaveStringToSettings(String settingName, String value){
    	settings = m_context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(settingName, value);
        
        editor.commit();    
    }
    
    public void SaveBooleanToSettings(String settingName, boolean value){
    	settings = m_context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(settingName, value);

        editor.commit();    
    }
    
    public void SaveIntToSettings(String settingName, int value){
    	settings = m_context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(settingName, value);

        editor.commit();    
    }
    
    public String GetStringFromSettings(String settingName){
    	 settings = m_context.getSharedPreferences(PREFS_NAME, 0);
	     
	     return settings.getString(settingName, "");
    }
    
    public boolean GetBooleanFromSettings(String settingName){
   	 	 settings = m_context.getSharedPreferences(PREFS_NAME, 0);
	     
	     return settings.getBoolean(settingName, false);
   }
    
    public int GetIntFromSettings(String settingName){
    	settings = m_context.getSharedPreferences(PREFS_NAME, 0);
	     
	     return settings.getInt(settingName, 0);
    }
    
    
}
