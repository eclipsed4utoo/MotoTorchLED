package com.alford.MotoTorchLED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MotoTorch extends Activity {
    /** Called when the activity is first created. */	
	
	// Setting names
	private static final String PREFS_DIMSCREEN = "dimScreen";
	private static final String PREFS_TURN_OFF_SCREEN = "turnOffScreen";
	private static final String PREFS_SCREEN_TIMEOUT = "screenTimeout";
	private static final String PREFS_MORSE_CODE_TEXT = "morseCodeText";
	private static final String PREFS_IS_LED_ON = "isLEDOn";	
	private static final String PREFS_IS_STROBE_ON = "isStrobeOn";
	private static final String PREFS_IS_MORSE_CODE_ON = "isMorseOn";
	private static final String PREFS_RUN_CONTINUOUSLY = "runContinuously";
	private static final String PREFS_SCREEN_BRIGHTNESS_OLD = "screenBrightnessOld";
	private static final String PREFS_SCREEN_BRIGHTNESS_NEW = "screenBrightnessNew";
	private static final String PREFS_STROBE_DELAY = "strobeDelay";
	private static final String PREFS_STROBE_DURATION = "strobeDuration";
	private static final String PREFS_WIDGET_ICON = "widgetIcon";
	private static final String PREFS_AGREEMENT = "agreement";
	private static final String PREFS_AUTO_BRIGHTNESS_POPUP = "autoBrightnessPopup";
	private static final String PREFS_NEXUS_ONE_POPUP = "nexusOnePopup";
	//private static final String PREFS_START_ON_CAMERA = "startOnCamera";
	private static final String PREFS_TORCH_ON_START = "torchOnStart";
	private static final String PREFS_TORCH_OFF_EXIT = "torchOffExit";
	private static final String PREFS_PROCESS_ID = "processID";
	
	private static final int MINIMUM_BACKLIGHT = 5;
	
	private static final int WIDGET_ICON_TORCH = 1;
	private static final int WIDGET_ICON_FLAME_NO_FRAME = 2;
	private static final int WIDGET_ICON_FLAME_WITH_FRAME = 3;
	
	static final ComponentName THIS_APPWIDGET = new ComponentName("com.alford.MotoTorchLED", "com.alford.MotoTorchLED.FlashlightWidget");
	
	private Context m_context;
	private SettingsFileManager settingsFileManager = null;
	private AlertDialog alertDialog = null;
	//private Camera camera = null;
	
	static MorseCode myMorseCode = null;
	static Strobe myStrobe = null;
	Light myLight = null;
	
	private static final String TAG = "MotoTorch LED - MotoTorch";
	
	@Override
	protected void onPause(){
		btnExit_Click(null);
		super.onPause();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                
        m_context = getApplicationContext();
        
        settingsFileManager = new SettingsFileManager(m_context); 
        //settingsFileManager.SaveBooleanToSettings(PREFS_AGREEMENT, false);
        //settingsFileManager.SaveBooleanToSettings(PREFS_NEXUS_ONE_POPUP, false);
        
        try {
        	if(settingsFileManager.GetIntFromSettings(PREFS_WIDGET_ICON) == 0){
        		settingsFileManager.SaveIntToSettings(PREFS_WIDGET_ICON, WIDGET_ICON_FLAME_WITH_FRAME);
        	}
        	settingsFileManager.SaveIntToSettings(PREFS_PROCESS_ID, Process.myPid());
        	//settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, false);
        	
            int autobright = Settings.System.getInt(m_context.getContentResolver(), "screen_brightness_mode");
        	//Settings.System.putInt(getContentResolver(), "screen_brightness_mode", 0);  

            // Gets the screen current timeout setting
        	int setting = Settings.System.getInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
			settingsFileManager.SaveIntToSettings(PREFS_SCREEN_TIMEOUT, setting);
			
			// Gets the screen current brightness
			setting = Settings.System.getInt(m_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			
			if (setting == 0)
				setting = 150;
			
			settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_OLD, setting);
			
			setting = settingsFileManager.GetIntFromSettings(PREFS_STROBE_DELAY);
			
			if(setting == 0)
				settingsFileManager.SaveIntToSettings(PREFS_STROBE_DELAY, 100);
			
			setting = settingsFileManager.GetIntFromSettings(PREFS_STROBE_DURATION);
			
			if(setting == 0)
				settingsFileManager.SaveIntToSettings(PREFS_STROBE_DURATION, 100);

			// checks to see if the AutoBrightness setting is set.
			if (autobright == 1 && settingsFileManager.GetBooleanFromSettings(PREFS_AGREEMENT)){
				ShowAutoBrightMessage();
				settingsFileManager.SaveBooleanToSettings(PREFS_DIMSCREEN, false);
				settingsFileManager.SaveBooleanToSettings(PREFS_TURN_OFF_SCREEN, false);
			}
			
			if (!settingsFileManager.GetBooleanFromSettings(PREFS_AGREEMENT))
	        	ShowAgreement();
			
			if (Utilities.IsAndroid22OrHigher() && !Utilities.IsDROID())
			{
				if (!settingsFileManager.GetBooleanFromSettings(PREFS_NEXUS_ONE_POPUP))
					ShowNexusOneAgreement();
			}
			
	        GetDimScreenValue(autobright); 
	        GetTurnOffScreenValue(autobright);
	    	SetBrightnessBarInitialValue(autobright);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
        
        GetRunContinuouslyValue();
        GetMorseCodeText();
        
        if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
        	// Only need to do these for Android 2.1.
        	DetermineIfLEDIsOn();
        	DetermineIfMorseIsOn();
        	DetermineIfStrobeIsOn();
        }
        
    	SetDelayBarInitialValue();
    	SetDurationBarInitialValue();
    	
    	if (Utilities.IsAndroid22OrHigher() && !Utilities.IsDROID()){
    		// need to check to see if the LEDs are currently on
    		//  if it is on, we can't allow the application to open
    		//  because it can't get the instance of the camera.
    		
    		if (settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON)){
    			ShowLEDAlreadyOnMessage();
    		}
    		//else{
    		//	if (camera == null){
            //		camera = Camera.open();
    		//	}
    		//}
    	}
    	else{
    		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
        		// only do this code for Android 2.1
        		// Android 2.2 will not be allowed to run without the application open
        		// so there is no need to check this.
        		if (myLight == null){
        			myLight = new Light(getApplicationContext());
        		}
        		
        		if(!myLight.IsFlashOn()){
        			if(settingsFileManager.GetBooleanFromSettings(PREFS_TORCH_ON_START))
        				btnLED_Click(null);
        		}
        	}
        	
    		Button b = (Button)findViewById(R.id.btnLED);
    		b.requestFocus();
    	}
    }
	
	@Override
	public void onResume(){
		super.onResume();
		int autobright = 0;
		
		try {
			autobright = Settings.System.getInt(m_context.getContentResolver(), "screen_brightness_mode");
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		if (Utilities.IsAndroid22OrHigher() && !Utilities.IsDROID()){
    		// need to check to see if the LEDs are currently on
    		//  if it is on, we can't allow the application to open
    		//  because it can't get the instance of the camera.
    		
    		if (settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON)){
    			ShowLEDAlreadyOnMessage();
    		}
    		//else{
    		//	if (camera == null)
            //		camera = Camera.open();
    		//}
    	}
		else{
			if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				// only do this code for Android 2.1
	    		// Android 2.2 will not be allowed to run without the application open
	    		// so there is no need to check this.
				if (myLight == null){
					myLight = new Light(getApplicationContext());
				}
				
				if(!myLight.IsFlashOn()){
					if(settingsFileManager.GetBooleanFromSettings(PREFS_TORCH_ON_START))
						btnLED_Click(null);
				}
			}
		}
		
		GetDimScreenValue(autobright); 
        GetTurnOffScreenValue(autobright);
    	SetBrightnessBarInitialValue(autobright);
    	
    	if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
    		DetermineIfLEDIsOn();
        	DetermineIfMorseIsOn();
        	DetermineIfStrobeIsOn();
    	}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Select Widget Icon");
        menu.add(0, 1, 0, "Help");
        menu.add(0, 2, 0, "Torch Settings");
        menu.add(0, 3, 0, "Application Settings");
        return true;
    }
	
	/* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 0:
        	AlertDialog.Builder builder;
        	
        	Context mContext = getApplicationContext();
        	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout = inflater.inflate(R.layout.widget_icon_selection,
        	                               (ViewGroup) findViewById(R.id.layout_root));
        	
        	builder = new AlertDialog.Builder(this);
        	builder.setView(layout);
        	
        	ImageView torch = (ImageView)layout.findViewById(R.id.img_torch_setting);
        	ImageView flame_no_frame = (ImageView)layout.findViewById(R.id.img_flame_setting_no_frame);
        	ImageView flame_with_frame = (ImageView)layout.findViewById(R.id.img_flame_setting_with_frame);
        	
        	torch.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					settingsFileManager.SaveIntToSettings(PREFS_WIDGET_ICON, WIDGET_ICON_TORCH);
					UpdateWidgetIcon(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON), false, false);
					Toast.makeText(getApplicationContext(), "Icon changed for Widget", Toast.LENGTH_SHORT).show();
			    	alertDialog.dismiss();
				}
        	});
        	
        	flame_no_frame.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					settingsFileManager.SaveIntToSettings(PREFS_WIDGET_ICON, WIDGET_ICON_FLAME_NO_FRAME);
					UpdateWidgetIcon(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON), false, false);
					Toast.makeText(getApplicationContext(), "Icon changed for Widget", Toast.LENGTH_SHORT).show();
			    	alertDialog.dismiss();
				}
        	});
        	
        	flame_with_frame.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					settingsFileManager.SaveIntToSettings(PREFS_WIDGET_ICON, WIDGET_ICON_FLAME_WITH_FRAME);
					UpdateWidgetIcon(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON), false, false);
					Toast.makeText(getApplicationContext(), "Icon changed for Widget", Toast.LENGTH_SHORT).show();
			    	alertDialog.dismiss();
				}
        	});
        	
        	alertDialog = builder.create();
        	alertDialog.show();
            return true;
        case 1:
        	Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://eclipsed4utoo.com/blog/mototorch-led-flashlight-android-application/"));
        	startActivity(myIntent);
            return true;
        case 2:
    		AlertDialog.Builder builder2;
        	
        	Context mContext2 = getApplicationContext();
        	LayoutInflater inflater2 = (LayoutInflater) mContext2.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout2 = inflater2.inflate(R.layout.torch_settings,
        	                               (ViewGroup) findViewById(R.id.torch_settings_root));
        	
        	builder2 = new AlertDialog.Builder(this);
        	builder2.setView(layout2);
        	
        	final CheckBox dimScreenCheckBox = (CheckBox)layout2.findViewById(R.id.chkTorchSettingsDimScreen);
        	final CheckBox turnOffScreenCheckBox = (CheckBox)layout2.findViewById(R.id.chkTorchSettingsTurnOffScreen);
        	final SeekBar brightnessSeek = (SeekBar)layout2.findViewById(R.id.seekTorchSettingsBrightness);
        	
        	boolean dimScreen = settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN);
        	boolean turnOffScreen = settingsFileManager.GetBooleanFromSettings(PREFS_TURN_OFF_SCREEN);
        	int screenBrightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
        	
        	if (dimScreen){
        		if (screenBrightness > MINIMUM_BACKLIGHT)
        			brightnessSeek.setProgress(screenBrightness);
        		else
        			brightnessSeek.setProgress(MINIMUM_BACKLIGHT);
        		
        		dimScreenCheckBox.setChecked(true);
        	}
        	else {
        		brightnessSeek.setProgress(MINIMUM_BACKLIGHT);
        		brightnessSeek.setEnabled(false);
        	}
        	
        	turnOffScreenCheckBox.setChecked(turnOffScreen);
        	
        	dimScreenCheckBox.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
			    	settingsFileManager.SaveBooleanToSettings(PREFS_DIMSCREEN, dimScreenCheckBox.isChecked());
			    	
					if (!dimScreenCheckBox.isChecked()){
						brightnessSeek.setProgress(MINIMUM_BACKLIGHT);
						brightnessSeek.setEnabled(false);  
					}
					else{
						
						int progressOld = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
				    	int progressNew = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
				    	
				    	if (progressNew < MINIMUM_BACKLIGHT){
				    		// first time running or has not set slider before
				    		// use the current screen brightness
				    		
				    		if (progressOld > 0)
				    			brightnessSeek.setProgress(progressOld);
				        	else
				        		brightnessSeek.setProgress(MINIMUM_BACKLIGHT);
				    	}
				    	else{
				    		if (progressNew >= MINIMUM_BACKLIGHT)
				    			brightnessSeek.setProgress(progressNew);
				        	else
				        		brightnessSeek.setProgress(MINIMUM_BACKLIGHT);
				    	}
				    	
				    	brightnessSeek.setEnabled(true);  
					}	
				}
        	});
        	
        	turnOffScreenCheckBox.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
			    	settingsFileManager.SaveBooleanToSettings(PREFS_TURN_OFF_SCREEN, turnOffScreenCheckBox.isChecked());
				}
        	});
        	
        	brightnessSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
	            	if(fromUser){
		            	if (progress >= MINIMUM_BACKLIGHT){
		            		settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, progress);
		                }
		                else {
		             	    settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, MINIMUM_BACKLIGHT);
		             	    seekBar.setProgress(MINIMUM_BACKLIGHT);
		                }
	            	}
	            } 
	
	            public void onStartTrackingTouch(SeekBar seekBar) {} 
	            public void onStopTrackingTouch(SeekBar seekBar) {} 
	             
	    	}); 
        	
        	alertDialog = builder2.create();
        	alertDialog.show();
        	return true;
        case 3:
        	AlertDialog.Builder builder3;
        	
        	Context mContext3 = getApplicationContext();
        	LayoutInflater inflater3 = (LayoutInflater) mContext3.getSystemService(LAYOUT_INFLATER_SERVICE);
        	View layout3 = inflater3.inflate(R.layout.application_settings,
        	                               (ViewGroup) findViewById(R.id.application_settings_layout_root));
        	
        	builder3 = new AlertDialog.Builder(this);
        	builder3.setView(layout3);
        	
        	final CheckBox turnOnTorch = (CheckBox)layout3.findViewById(R.id.chkApplicationSettingsTurnOnTorch);
        	final CheckBox turnOffTorch = (CheckBox)layout3.findViewById(R.id.chkApplicationSettingsTurnOffTorch);
        	
        	boolean torchSetting = settingsFileManager.GetBooleanFromSettings(PREFS_TORCH_ON_START);
        	boolean torchOffSetting = settingsFileManager.GetBooleanFromSettings(PREFS_TORCH_OFF_EXIT);
        	
        	turnOnTorch.setChecked(torchSetting);
        	turnOffTorch.setChecked(torchOffSetting);
        	
        	turnOnTorch.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
			    	settingsFileManager.SaveBooleanToSettings(PREFS_TORCH_ON_START, turnOnTorch.isChecked());
			    	//alertDialog.dismiss();
				}
        	});
        	
        	turnOffTorch.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
			    	settingsFileManager.SaveBooleanToSettings(PREFS_TORCH_OFF_EXIT, turnOffTorch.isChecked());
			    	//alertDialog.dismiss();
				}
        	});
        	
        	alertDialog = builder3.create();
        	alertDialog.show();
        	return true;
        }
        return false;
    }
	
    
    
    public void chkDimScreen_Click(View view){
    	CheckBox checkBox = (CheckBox) findViewById(R.id.chkDimScreen);
    	settingsFileManager.SaveBooleanToSettings(PREFS_DIMSCREEN, checkBox.isChecked());
    	
    	SeekBar brightnessBar = (SeekBar)findViewById(R.id.seekBrightness);
		if (!checkBox.isChecked()){
			brightnessBar.setProgress(MINIMUM_BACKLIGHT);
			brightnessBar.setEnabled(false);  
		}
		else{
			
			int progressOld = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
	    	int progressNew = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
	    	
	    	if (progressNew < MINIMUM_BACKLIGHT){
	    		// first time running or has not set slider before
	    		// use the current screen brightness
	    		
	    		if (progressOld > 0)
	        		brightnessBar.setProgress(progressOld);
	        	else
	        		brightnessBar.setProgress(MINIMUM_BACKLIGHT);
	    	}
	    	else{
	    		if (progressNew >= MINIMUM_BACKLIGHT)
	        		brightnessBar.setProgress(progressNew);
	        	else
	        		brightnessBar.setProgress(MINIMUM_BACKLIGHT);
	    	}
	    	
			brightnessBar.setEnabled(true);  
		}	
		
    	checkBox = null;
    }
    
    public void chkTurnOffScreen_Click(View view){
    	CheckBox checkBox = (CheckBox) findViewById(R.id.chkTurnOffScreen);
    	settingsFileManager.SaveBooleanToSettings(PREFS_TURN_OFF_SCREEN, checkBox.isChecked());
    	checkBox = null;
    }
    
    public void chkContinueRunning_Click(View view){
    	CheckBox checkBox = (CheckBox) findViewById(R.id.chkContinueRunning);
    	settingsFileManager.SaveBooleanToSettings(PREFS_RUN_CONTINUOUSLY, checkBox.isChecked());
    	checkBox = null;
    }
    
    public void btnMorseCode_Click(View view){
    	// Sets the other buttons back to default
    	ResetStrobe();
	   	ResetLED();
    	
    	boolean isOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
    	
    	if(isOn){
    		// Morse Code is currently running
    		// Turn it off
    		settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, false);
    		myMorseCode.keepOn = false;
    		
    		try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
			myMorseCode = null;
			
			SetMorseCodeButtonEnabled();
    	}
    	else {
    		// Morse Code is not currently running
    		// Turn it on
    		EditText textBox = (EditText)findViewById(R.id.txtMorseCode);
        	String inputText = textBox.getText().toString();
        	textBox = null;
        	
        	CheckBox checkBox = (CheckBox) findViewById(R.id.chkContinueRunning);
            boolean runContinuously = checkBox.isChecked();
            checkBox = null;
        	
        	settingsFileManager.SaveStringToSettings(PREFS_MORSE_CODE_TEXT, inputText);
        	settingsFileManager.SaveBooleanToSettings(PREFS_RUN_CONTINUOUSLY, runContinuously);
        	settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, true);
        	
        	if (myLight == null)
        		myLight = new Light(getApplicationContext());
        	
        	if(myMorseCode == null)
        		myMorseCode = new MorseCode(inputText, myLight);
    		
    		myMorseCode.keepOn = true;
    		myMorseCode.runContinuously = runContinuously;
    		myMorseCode.execute(null, null, null);
        	
    		if(runContinuously){
	        	SetMorseCodeButtonDisabled();
    		}
    		else{
    			settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, false);
    			
    			try {
    				Thread.sleep(300);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
        		
    			myMorseCode = null;
    		}
    	}
    }
    
    public void btnLED_Click(View view){
    	 // Sets the other buttons back to default
    	 ResetStrobe();
    	 ResetMorseCode();
    	 
    	 boolean turnOffScreen = settingsFileManager.GetBooleanFromSettings(PREFS_TURN_OFF_SCREEN);
    	 boolean dimScreen = settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN);
	     
    	 if (myLight == null){
 			myLight = new Light(getApplicationContext());
 		}
	     
	   	 if (myLight.IsFlashOn())
	   	 {
	   		 // Flash is currently on
	   		 // Turn it off
	   		 myLight.TurnOffTheLight();
	   			   		   		
	         if (turnOffScreen){
	        	int timeout = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_TIMEOUT);
	        	Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
	         }
	        
	         if(dimScreen){
	        	 int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
	        	 if(brightness >= MINIMUM_BACKLIGHT){
	        	 	 SetScreenBrightness(brightness);
	        	 }
	        	 else{
	        	 	 SetScreenBrightness(MINIMUM_BACKLIGHT);
	        	 }	
	         }
	        
	   		 settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, false);
	   		 SetFlashlightButtonEnabled();
			 
			 UpdateWidgetIcon(false, false, false);
	   	 }
	   	 else
	   	 {
	   		 // Flash is currently off
	   		 // Turn it on
	   		 myLight.TurnOnTheLight();
	   			   		
	   		 if (turnOffScreen){
	   		 	Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);
	   		 }
	   		
	   		 if(dimScreen){
	        	 int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
	        	
	        	 if(brightness >= MINIMUM_BACKLIGHT){
	        	 	 SetScreenBrightness(brightness);
	        	 }
	        	 else{
	        	 	 SetScreenBrightness(MINIMUM_BACKLIGHT);
	        	 }
	         }
	   		
	   		 settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, true);  
	       	 SetFlashlightButtonDisabled();
			 
			 UpdateWidgetIcon(true, false, false);
	   	 }  	
    }    

    public void btnStartStrobe_Click(View view){
    	// Sets the other buttons back to default
	   	ResetMorseCode();
	   	ResetLED();
	   	
	   	// Start of code for strobe
    	boolean isOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
    	
    	if (myLight == null)
    		myLight = new Light(getApplicationContext());
    	
    	if(myStrobe == null)
    		myStrobe = new Strobe(myLight);
    	
    	if(isOn){
    		settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, false);
    		myStrobe.keepOn = false;
    		
    		try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
			myStrobe = null;
			
			SetStrobeButtonEnabled();
    	}
    	else {
    		settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, true);
    		myStrobe.keepOn = true;
    		myStrobe.delay = settingsFileManager.GetIntFromSettings(PREFS_STROBE_DELAY);
    		myStrobe.duration = settingsFileManager.GetIntFromSettings(PREFS_STROBE_DURATION);
    		myStrobe.execute(null, null, null);
        	
        	SetStrobeButtonDisabled();
    	}
    }
    
    public void btnExit_Click(View view){
    	 settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, false);
	   	 settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, false);
	   	 
	   	 ResetMorseCode();
	   	 
	   	 ResetStrobe();
	   	 
	   	if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
	   		 
		   	 if(settingsFileManager.GetBooleanFromSettings(PREFS_TORCH_OFF_EXIT)){
		   		 if(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON)){
		   			 TurnOffTorch();
		   			 
		   			 //if (myLight != null)
		   			//	 myLight.ReleaseCamera();
		   		 }
		   	 }
		   	 
		   	 UpdateWidgetIcon(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON), false, false);
		   	 
		   	 MotoTorch.this.finish();
	   	 }
	   	 else {
	   		 // Not able to allow the user to leave the application with the 
	   		 //   Exit button and still leave the LED on. 
	   		 if (myLight != null)
   				 myLight.ReleaseCamera();
		   	 
		   	 MotoTorch.this.finish();
	   	 }
    }
    
    public void GetDimScreenValue(int autobright){
    	// Gets the last saved state of the checkbox
        //    and sets the checkstate
        CheckBox checkBox = (CheckBox) findViewById(R.id.chkDimScreen);
        if (autobright == 0) {
        	checkBox.setChecked(settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN));
        	checkBox.setClickable(true);
        	checkBox.setEnabled(true);
        }	
        else
        {
        	checkBox.setChecked(false);
        	checkBox.setClickable(false);
        	checkBox.setEnabled(false);
        }
        checkBox = null;
    }
    
    public void GetTurnOffScreenValue(int autobright){
    	// Gets the last saved state of the checkbox
        //    and sets the checkstate
        CheckBox checkBox = (CheckBox) findViewById(R.id.chkTurnOffScreen);
        if (autobright == 0) {
        	checkBox.setChecked(settingsFileManager.GetBooleanFromSettings(PREFS_TURN_OFF_SCREEN));
        	checkBox.setClickable(true);
        	checkBox.setEnabled(true);
        }
        else	
        {
        	checkBox.setChecked(false);
        	checkBox.setClickable(false);
        	checkBox.setEnabled(false);
        }
       	checkBox = null;
    }
    
    public void GetRunContinuouslyValue(){
    	// Gets the last saved state of the checkbox
        //    and sets the checkstate
        CheckBox checkBox = (CheckBox) findViewById(R.id.chkContinueRunning);
        checkBox.setChecked(settingsFileManager.GetBooleanFromSettings(PREFS_RUN_CONTINUOUSLY));
        checkBox = null;
    }
    
    public void GetMorseCodeText(){
    	// Gets the last saved Morse Code text and
        //    sets it to the EditText control
        EditText textBox = (EditText)findViewById(R.id.txtMorseCode);
    	textBox.setText(settingsFileManager.GetStringFromSettings(PREFS_MORSE_CODE_TEXT));
    }
    
    public void DetermineIfLEDIsOn(){
    	// Checks to see if the LED is currently on
        //    then determines whether the flashlight turned it on
        boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON);
        boolean dimScreen = settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN);	
        
    	if (isLEDOn)
    	{
    		SetFlashlightButtonDisabled();
    		
    		if(dimScreen){
	        	int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
	        	
	        	if(brightness >= MINIMUM_BACKLIGHT){
	        		SetScreenBrightness(brightness);
	        	}
	        	else{
	        		SetScreenBrightness(MINIMUM_BACKLIGHT);
	        	}
	        }
    	}
    	else
    	{
    		SetFlashlightButtonEnabled();
    	}
    }
    
    public void DetermineIfStrobeIsOn(){
    	// Checks to see if the LED is currently on
        //    then determines whether the flashlight turned it on
        boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
        	
    	if (isLEDOn)
    		SetStrobeButtonDisabled();
    	else
    		SetStrobeButtonEnabled();
    }
    
    public void DetermineIfMorseIsOn(){
    	// Checks to see if the LED is currently on
        //    then determines whether the flashlight turned it on
        boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
        	
    	if (isLEDOn)
    		SetMorseCodeButtonDisabled();
    	else
    		SetMorseCodeButtonEnabled();
    }
    
    public void SetFlashlightButtonDisabled(){
    	Button button = (Button) findViewById(R.id.btnLED);
    	button.setText("Turn LED OFF");
		button.setTextColor(Color.RED);
		button = null;
    }
    
    public void SetFlashlightButtonEnabled(){
    	Button button = (Button) findViewById(R.id.btnLED);
    	button.setText("Turn LED ON");
		 button.setTextColor(Color.rgb(0, 128, 0));
		button = null;
    }
    
    public void SetStrobeButtonDisabled(){
    	Button button = (Button) findViewById(R.id.btnStartStrobe);
    	button.setText("Stop Strobe");
		button.setTextColor(Color.RED);
		button = null;
    }
    
    public void SetStrobeButtonEnabled(){
    	Button button = (Button) findViewById(R.id.btnStartStrobe);
    	button.setText("Start Strobe");
		 button.setTextColor(Color.rgb(0, 128, 0));
		button = null;
    }
    
    public void SetMorseCodeButtonDisabled(){
    	Button button = (Button) findViewById(R.id.btnMorseCode);
    	button.setText("Stop Morse Code");
		button.setTextColor(Color.RED);
		button = null;
    }
    
    public void SetMorseCodeButtonEnabled(){
    	Button button = (Button) findViewById(R.id.btnMorseCode);
    	button.setText("Send Morse Code");
		 button.setTextColor(Color.rgb(0, 128, 0));
		button = null;
    }
    
    public void SetScreenBrightness(int value){
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = (float) (value / 255.0);
		getWindow().setAttributes(lp);     
    	
		//android.os.IPowerManager powerService = android.os.IPowerManager.Stub.asInterface(sm.getService("power"));
		
		// ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		// connManager.setBackgroundDataSetting(true);
		// ContentResolver.setMasterSyncAutomatically(true);
		
    	/*Light myLight = new Light();
    	myLight.ChangeScreenBrightness(value);*/
    	
    	/*Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);*/
    }
    
    public void SetBrightnessBarInitialValue(int autobright){
    	SeekBar brightnessBar = (SeekBar)findViewById(R.id.seekBrightness);
    	
    	if (autobright == 0) {
    		brightnessBar.setEnabled(true);    		

    		CheckBox checkBox = (CheckBox) findViewById(R.id.chkDimScreen);
    		if (!checkBox.isChecked()){
    			brightnessBar.setEnabled(false);  
    			return;
    		}
    		
    		int progressOld = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
	    	int progressNew = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
	    	
	    	if (progressNew < MINIMUM_BACKLIGHT){
	    		// first time running or has not set slider before
	    		// use the current screen brightness
	    		
	    		if (progressOld > 0)
	        		brightnessBar.setProgress(progressOld);
	        	else
	        		brightnessBar.setProgress(MINIMUM_BACKLIGHT);
	    	}
	    	else{
	    		if (progressNew >= MINIMUM_BACKLIGHT)
	        		brightnessBar.setProgress(progressNew);
	        	else
	        		brightnessBar.setProgress(MINIMUM_BACKLIGHT);
	    	}
	    	
	    	brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
	            	if(fromUser){
		            	if (progress >= MINIMUM_BACKLIGHT){
		            		settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, progress);
		                }
		                else {
		             	    settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, MINIMUM_BACKLIGHT);
		             	    seekBar.setProgress(MINIMUM_BACKLIGHT);
		                }
	            	}
	            } 
	
	            public void onStartTrackingTouch(SeekBar seekBar) {} 
	            public void onStopTrackingTouch(SeekBar seekBar) {} 
	             
	    	}); 
    	}
    	else
    	{
    		brightnessBar.setProgress(MINIMUM_BACKLIGHT);
    		brightnessBar.setEnabled(false);    	
    		
    		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 
	            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
	            	if(fromUser){
		            	if (progress >= MINIMUM_BACKLIGHT){
		            		settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, progress);
		                }
		                else {
		             	    settingsFileManager.SaveIntToSettings(PREFS_SCREEN_BRIGHTNESS_NEW, MINIMUM_BACKLIGHT);
		             	    seekBar.setProgress(MINIMUM_BACKLIGHT);
		                }
	            	}
	            } 
	
	            public void onStartTrackingTouch(SeekBar seekBar) {} 
	            public void onStopTrackingTouch(SeekBar seekBar) {} 
	             
	    	}); 
    	}
    	brightnessBar = null;
    }
    
    public void SetDelayBarInitialValue(){
    	SeekBar delayBar = (SeekBar)findViewById(R.id.seekDelay);
    	delayBar.setProgress(settingsFileManager.GetIntFromSettings(PREFS_STROBE_DELAY));
    	delayBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
            	if (myStrobe != null){
             	   myStrobe.delay = progress;
             	  settingsFileManager.SaveIntToSettings(PREFS_STROBE_DELAY, progress);
             	  TextView label = (TextView) findViewById(R.id.lblStrobeDelay);
             	  label.setText(progress + " ms");
             	  label = null;
                }
                else {
             	   settingsFileManager.SaveIntToSettings(PREFS_STROBE_DELAY, progress);
             	  TextView label = (TextView) findViewById(R.id.lblStrobeDelay);
             	  label.setText(progress + " ms");
             	  label = null;
                }
            } 

            public void onStartTrackingTouch(SeekBar seekBar) {} 
            public void onStopTrackingTouch(SeekBar seekBar) {} 
             
       }); 
    	
    	TextView label = (TextView) findViewById(R.id.lblStrobeDelay);
  	    label.setText(settingsFileManager.GetIntFromSettings(PREFS_STROBE_DELAY) + " ms");
  	    label = null;
    	
    	delayBar = null;
    }
    
    public void SetDurationBarInitialValue(){
    	SeekBar durationBar = (SeekBar)findViewById(R.id.seekDuration);
    	durationBar.setProgress(settingsFileManager.GetIntFromSettings(PREFS_STROBE_DURATION));
    	durationBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
            	if (myStrobe != null){
             	   myStrobe.duration = progress;
             	  settingsFileManager.SaveIntToSettings(PREFS_STROBE_DURATION, progress);
             	 TextView label = (TextView) findViewById(R.id.lblStrobeDuration);
            	  label.setText(progress + " ms");
            	  label = null;
                }
                else {
             	   settingsFileManager.SaveIntToSettings(PREFS_STROBE_DURATION, progress);
             	  TextView label = (TextView) findViewById(R.id.lblStrobeDuration);
            	  label.setText(progress + " ms");
            	  label = null;
                }
            } 

            public void onStartTrackingTouch(SeekBar seekBar) {} 
            public void onStopTrackingTouch(SeekBar seekBar) {} 
             
       }); 
    	
    	TextView label = (TextView) findViewById(R.id.lblStrobeDuration);
  	    label.setText(settingsFileManager.GetIntFromSettings(PREFS_STROBE_DURATION) + " ms");
  	    label = null;
    	
    	durationBar = null;
    }
    
    public void ResetStrobe(){
    	SetStrobeButtonEnabled();
    	settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, false);
    	
    	if(myStrobe != null){
			myStrobe.keepOn = false;
		   	
		   	try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		   	
			myStrobe = null;
    	}
    }
    
    public void ResetLED(){
    	SetFlashlightButtonEnabled();
    	UpdateWidgetIcon(false, false, false);
    	
    	if(settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON)){
    		btnLED_Click(null);
    	}
    	
    	settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, false);
    }
    
    public void ResetMorseCode(){
    	SetMorseCodeButtonEnabled();
    	settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, false);
    	
    	if (myMorseCode != null){
	    	 myMorseCode.keepOn = false;
	 	   	
	 	   	 try {
	 			Thread.sleep(300);
	 		 } catch (InterruptedException e) {
	 			e.printStackTrace();
	 		 }
	 	   	
	 	   	 myMorseCode = null;
   	 	}
    }
    
    public void UpdateWidgetIcon(boolean isLEDOn, boolean isStrobeOn, boolean isMorsseOn){
    	final AppWidgetManager gm = AppWidgetManager.getInstance(m_context);
		int widgetIcon = settingsFileManager.GetIntFromSettings(PREFS_WIDGET_ICON);
		RemoteViews views = null;

		int layoutID = 0;

		if (isLEDOn) {
			if(widgetIcon == WIDGET_ICON_FLAME_WITH_FRAME){
				layoutID = R.layout.widget_flame_with_border_on;
			}
			else if(widgetIcon == WIDGET_ICON_FLAME_NO_FRAME){
				layoutID = R.layout.widget_flame_only_on;
			}
			else if (widgetIcon == WIDGET_ICON_TORCH){
				layoutID = R.layout.widget_torch_on;
			}
		} else {
			if(widgetIcon == WIDGET_ICON_FLAME_WITH_FRAME){
				layoutID = R.layout.widget_flame_with_border_off;
			}
			else if(widgetIcon == WIDGET_ICON_FLAME_NO_FRAME){
				layoutID = R.layout.widget_flame_only_off;
			}
			else if (widgetIcon == WIDGET_ICON_TORCH){
				layoutID = R.layout.widget_torch_off;
			}
		}

        views = new RemoteViews(m_context.getPackageName(), layoutID);
		 
        views.setOnClickPendingIntent(R.id.btn_led, getLaunchPendingIntent(m_context));	

        gm.updateAppWidget(THIS_APPWIDGET, views);
    }
    
    private static PendingIntent getLaunchPendingIntent(Context context) {
        Intent launchIntent = new Intent("com.android.mototorchled.widget.CLICK");
        //launchIntent.setClass(context, FlashlightWidget.class);
        //launchIntent.setClass(context, context.getClass());
        //launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //launchIntent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 /* no requestCode */, launchIntent, 0 /* no flags */);
        return pi;
    }
    
    private void ShowNexusOneAgreement(){
    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    	builder1.setMessage("*****   DISCLAIMER   **** \n\nYou are running Android 2.2.  The application will behave " +
							"a little differently than in 2.0/2.1.\n\nYou will no longer be able to use the camera while the LED " +
							"is on.  Google added the ability to turn on the flash with video in the camera application, so it's not needed in " +
							"this application anymore.\n\n" +
							"You will also not be able to close the application with the LEDs still on. ")
    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   settingsFileManager.SaveBooleanToSettings(PREFS_NEXUS_ONE_POPUP, true);
	    	        	   dialog.cancel();
	    	           }
	    	       });
    	AlertDialog alert1 = builder1.create();
    	alert1.show();
    }
    
    private void ShowAgreement(){
    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    	builder1.setMessage("*****   DISCLAIMER   **** \n\nThis application uses code that is not part of the " +
							"the Android 2.0 SDK.  Upon agreeing to this disclaimer, you are agreeing that " +
							"the developer is not responsible for any harm caused by the use of this application.\n\n" +
							"TORCH MODE:  This application has not been tested for long-term effects of it's use.  There is a " +
							"possibility of damage to the LEDs of the device.  While rare, it may be possible.\n\n" +
							"STROBE:  Using the Strobe feature could possibly cause some people to have seizures." +
							"  Use at your own risk.")
    	       .setCancelable(false)
    	       .setPositiveButton("I Agree", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   settingsFileManager.SaveBooleanToSettings(PREFS_AGREEMENT, true);
    	        	   dialog.cancel();
    	        	   
    	        	   try {
						int autobright = Settings.System.getInt(m_context.getContentResolver(), "screen_brightness_mode");
						if (autobright == 1){
							ShowAutoBrightMessage();
							settingsFileManager.SaveBooleanToSettings(PREFS_DIMSCREEN, false);
							settingsFileManager.SaveBooleanToSettings(PREFS_TURN_OFF_SCREEN, false);
						}
					} catch (SettingNotFoundException e) {
						e.printStackTrace();
					}
    	           }
    	       })
    	       .setNegativeButton("I Disagree", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   settingsFileManager.SaveBooleanToSettings(PREFS_AGREEMENT, false);
    	        	   MotoTorch.this.finish();
    	           }
    	       });
    	AlertDialog alert1 = builder1.create();
    	alert1.show();
    }
    
    private void ShowAutoBrightMessage(){
		if (!settingsFileManager.GetBooleanFromSettings(PREFS_AUTO_BRIGHTNESS_POPUP)){
	    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	    	builder1.setMessage("You have Automatic Brightness enabled.  The " +
	    			"\"Torch\" settings will be disabled.\n\n" +
	    			"With Automatic Brightness on, the screen will get very bright " +
	    			"when you turn the LEDs on.  This is due to the sensors seeing the light " +
	    			"from the LEDs and brightening the screen.  This is how Motorola/Google designed " +
	    			"the Automatic Brightness to work.\n\n" +
	    			"To disable Automatic Brightness, go to Settings --> Sound & display --> " +
	    			"Brightness --> Automatic Brightness checkbox")
	    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   settingsFileManager.SaveBooleanToSettings(PREFS_AUTO_BRIGHTNESS_POPUP, true);
	    	        	   dialog.cancel();
	    	           }
	    	       });
	    	AlertDialog alert1 = builder1.create();
	    	alert1.show();
		}
    }
    
    private void ShowLEDAlreadyOnMessage(){
    	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    	builder1.setMessage("The LED is currently on, probably from the widget.  Because you are running Android 2.2, " +
							"you can't be allowed to open the application while the LED is on.\n\n" +
							"Please turn off the LED using the widget, then reopen the application.")
    	       .setCancelable(false)
	    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   dialog.cancel();
	    	        	   MotoTorch.this.finish();
	    	           }
	    	       });
    	AlertDialog alert1 = builder1.create();
    	alert1.show();
    }
    
    private void TurnOffTorch(){
    	ResetLED();
    }

}