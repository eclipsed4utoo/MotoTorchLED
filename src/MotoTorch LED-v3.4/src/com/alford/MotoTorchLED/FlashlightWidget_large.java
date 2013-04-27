package com.alford.MotoTorchLED;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.Settings;
import android.widget.RemoteViews;

public class FlashlightWidget_large extends AppWidgetProvider {
	
	// Setting names
	public static final String PREFS_DIMSCREEN = "dimScreen";
	public static final String PREFS_TURN_OFF_SCREEN = "turnOffScreen";
	public static final String PREFS_SCREEN_TIMEOUT = "screenTimeout";
	public static final String PREFS_MORSE_CODE_TEXT = "morseCodeText";
	public static final String PREFS_STROBE_DURATION = "strobeDuration";
	public static final String PREFS_IS_LED_ON = "isLEDOn";	
	public static final String PREFS_IS_STROBE_ON = "isStrobeOn";
	public static final String PREFS_IS_MORSE_CODE_ON = "isMorseOn";
	public static final String PREFS_RUN_CONTINUOUSLY = "runContinuously";
	public static final String PREFS_SCREEN_BRIGHTNESS_OLD = "screenBrightnessOld";
	public static final String PREFS_SCREEN_BRIGHTNESS_NEW = "screenBrightnessNew";
	
	private static final int MINIMUM_BACKLIGHT = 5;	
	
	private static final int BUTTON_LED = 0;
    private static final int BUTTON_STROBE = 1;
    //private static final int BUTTON_MORSE = 2;

	Context m_context;
	SettingsFileManager settingsFileManager;
	Light myLight = null;
	
	static final ComponentName THIS_APPWIDGET = new ComponentName("com.alford.MotoTorchLED", "com.alford.MotoTorchLED.FlashlightWidget_large");

    /**
     * Receives and processes a button pressed intent or state change.
     *
     * @param context
     * @param intent  Indicates the pressed button.
     */
	@Override 
    public void onReceive(Context context, Intent intent) 
	{ 
		super.onReceive(context, intent); 
         
        if(intent.getAction().equals("com.android.myapp.widget.CLICK")) { 
         
		//if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
			Uri data = intent.getData();
			int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
             
			m_context = context;
			
			settingsFileManager = new SettingsFileManager(m_context);
             
			boolean dimScreen = settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN);
			boolean turnScreenOff = settingsFileManager.GetBooleanFromSettings(PREFS_TURN_OFF_SCREEN);
             
			boolean isStrobeOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
			boolean isMorseOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
			boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON);
            
			//buttonId = BUTTON_STROBE;
			
			if (buttonId == BUTTON_LED) {
             
				if (myLight == null){
					Camera c = Camera.open();
					myLight = new Light(context);
				}
	   	     
				if (isLEDOn)
				{
					myLight.TurnOffTheLight();
		   	   		 
					isLEDOn = false;
			   		
					if(dimScreen) {
			        	int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
			        	if(brightness >= MINIMUM_BACKLIGHT){
			        		SetScreenBrightness(brightness, m_context);
			        	}
			        	else{
			        		SetScreenBrightness(MINIMUM_BACKLIGHT, m_context);
			        	}	
						
					}
					
					if(turnScreenOff){
						int timeout = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_TIMEOUT);
						Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
			        }
				}
				else
				{
					myLight.TurnOnTheLight();
					
					isLEDOn = true;

			   		if(dimScreen){
			        	int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_NEW);
			        	
			        	if(brightness >= MINIMUM_BACKLIGHT){
			        		SetScreenBrightness(brightness, context);
			        	}
			        	else{
			        		SetScreenBrightness(MINIMUM_BACKLIGHT, context);
			        	}
			        }
					
					if(turnScreenOff){
						Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);
					}
				}       
            } else if (buttonId == BUTTON_STROBE) {
				if (isStrobeOn) {
					isStrobeOn = false;
				}
				else
				{
					isStrobeOn = true;
				}
            }

			settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, isLEDOn);
			settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, isStrobeOn);
			settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, isMorseOn);

             
			updateWidget(context, isLEDOn, isStrobeOn, isMorseOn);
             
			//performUpdate(context, null, myLight);
		} 
    }  

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		settingsFileManager = new SettingsFileManager(context);
    	
		boolean isStrobeOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
		boolean isMorseOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
		boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON);

		//updateWidget(context, isLEDOn, isStrobeOn, isMorseOn);
		
		

    	// Update each requested appWidgetId
        RemoteViews view = buildUpdate(context, -1 , isLEDOn, isStrobeOn, isMorseOn);

        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], view);
        }
    }

	@Override
	public void onEnabled(Context context){
		super.onEnabled(context);
	}
	
    /**
     * Load image for given widget and build {@link RemoteViews} for it.
     */
    static RemoteViews buildUpdate(Context context, int appWidgetId, boolean isLEDOn, boolean isStrobeOn, boolean isMorseOn) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_flashlight_large);
        views.setOnClickPendingIntent(R.id.btn_led, getLaunchPendingIntent(context, appWidgetId, BUTTON_LED));
        views.setOnClickPendingIntent(R.id.btn_strobe, getLaunchPendingIntent(context, appWidgetId, BUTTON_STROBE));

        
        updateButtons(views, context, isLEDOn, isStrobeOn, isMorseOn);
        return views;
    }

    /**
     * Updates the widget when something changes, or when a button is pushed.
     *
     * @param context
     */
    public static void updateWidget(Context context, boolean isLEDOn, boolean isStrobeOn, boolean isMorseOn) {
        RemoteViews views = buildUpdate(context, -1, isLEDOn, isStrobeOn, isMorseOn);
        // Update specific list of appWidgetIds if given, otherwise default to all
        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(THIS_APPWIDGET, views);
    }

    /**
     * Updates the buttons based on the underlying states of wifi, etc.
     *
     * @param views   The RemoteViews to update.
     * @param context
     */
    private static void updateButtons(RemoteViews views, Context context, boolean isLEDOn, boolean isStrobeOn, boolean isMorseOn) {

        if (isLEDOn) {
            views.setImageViewResource(R.id.img_led, R.drawable.mototorch_led_on);
        } else {
            views.setImageViewResource(R.id.img_led, R.drawable.mototorch_led_off);
        }

        if (isStrobeOn) {
            views.setImageViewResource(R.id.img_strobe, R.drawable.mototorch_strobe_on);
        } else {
            views.setImageViewResource(R.id.img_strobe, R.drawable.mototorch_strobe_off);
        }
    }

    /**
     * Creates PendingIntent to notify the widget of a button click.
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    private static PendingIntent getLaunchPendingIntent(Context context, int appWidgetId,
            int buttonId) {
        Intent launchIntent = new Intent("com.android.myapp.widget.CLICK");
        launchIntent.setClass(context, FlashlightWidget_large.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 /* no requestCode */, launchIntent, 0 /* no flags */);
        return pi;
    }

    public void SetScreenBrightness(int value, Context context){
    	
    	android.provider.Settings.System.putInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, value);  


    }
      
	
}
