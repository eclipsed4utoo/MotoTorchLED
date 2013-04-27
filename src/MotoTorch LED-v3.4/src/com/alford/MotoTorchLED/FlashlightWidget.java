package com.alford.MotoTorchLED;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.provider.Settings;
import android.widget.RemoteViews;
import android.widget.Toast;

public class FlashlightWidget extends AppWidgetProvider {
	
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
	public static final String PREFS_WIDGET_ICON = "widgetIcon";
	public static final String PREFS_START_ON_CAMERA = "startOnCamera";
	public static final String PREFS_PROCESS_ID = "processID";
	
	private static final int MINIMUM_BACKLIGHT = 5;	
	
	private static final int BUTTON_LED = 0;
    private static final int BUTTON_STROBE = 1;
    //private static final int BUTTON_MORSE = 2;
    
    private static final int WIDGET_ICON_TORCH = 1;
	private static final int WIDGET_ICON_FLAME_NO_FRAME = 2;
	private static final int WIDGET_ICON_FLAME_WITH_FRAME = 3;

	static final ComponentName THIS_APPWIDGET = new ComponentName("com.alford.MotoTorchLED", "com.alford.MotoTorchLED.FlashlightWidget");
	
	Context m_context;
	static Light myLight = null;
	//static Camera camera = null;
	static SettingsFileManager settingsFileManager;
	

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
         
		if(intent.getAction().equals("com.android.mototorchled.widget.CLICK")) { 
         
		//if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
			//Uri data = intent.getData();
			//int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            
			int buttonId = BUTTON_LED;
			
			m_context = context;
			
			settingsFileManager = new SettingsFileManager(m_context);
             
			boolean dimScreen = settingsFileManager.GetBooleanFromSettings(PREFS_DIMSCREEN);
			boolean turnScreenOff = settingsFileManager.GetBooleanFromSettings(PREFS_TURN_OFF_SCREEN);
             
			boolean isStrobeOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
			boolean isMorseOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
			boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON);
            
			//buttonId = BUTTON_STROBE;
			
			if (buttonId == BUTTON_LED) {
				
				//if (Utilities.IsAndroid22OrHigher() && !Utilities.IsDROID()){
				//	if (camera == null){
				//		try{
				//			camera = Camera.open();
				//		}
				//		catch(Exception e){
				//			e.printStackTrace();
				//			settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, false);
				//			Toast.makeText(context, "Unable to aquire Camera", Toast.LENGTH_LONG).show();
				//			return;
				//		}
				//	}
				//}
				
				if (myLight == null){
					//Toast.makeText(context, "Creating Light instance", Toast.LENGTH_SHORT).show();
					myLight = new Light(context);
				}
	   	     
				if (isLEDOn)
				{
					//Toast.makeText(context, "Turning Off LEDs", Toast.LENGTH_SHORT).show();
					myLight.TurnOffTheLight();
		   	   		 
					isLEDOn = false;
			   		
					if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID())
					{
						if(dimScreen) {
				        	int brightness = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_BRIGHTNESS_OLD);
				        	if(brightness >= MINIMUM_BACKLIGHT){
				        		SetScreenBrightness(brightness, m_context);
				        	}
				        	else{
				        		SetScreenBrightness(MINIMUM_BACKLIGHT, m_context);
				        	}	
						}
					}
					
					if(turnScreenOff){
						int timeout = settingsFileManager.GetIntFromSettings(PREFS_SCREEN_TIMEOUT);
						Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
			        }
					
					myLight.ReleaseCamera();
					myLight = null;
				}
				else
				{
					//Toast.makeText(context, "Turning On LEDs", Toast.LENGTH_SHORT).show();
					myLight.TurnOnTheLight();
					
					isLEDOn = true;
					if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID())
					{
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
				}       
            } else if (buttonId == BUTTON_STROBE) {
				if (isStrobeOn) {
					isStrobeOn = false;
				}
				else{
					isStrobeOn = true;
				}
            }

			settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, isLEDOn);
			settingsFileManager.SaveBooleanToSettings(PREFS_IS_STROBE_ON, isStrobeOn);
			settingsFileManager.SaveBooleanToSettings(PREFS_IS_MORSE_CODE_ON, isMorseOn);

			updateWidget(context, isLEDOn, isStrobeOn, isMorseOn);
			
			//performUpdate(context, null, myLight);
		} 
        /*else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")){
        	int packageID = intent.getIntExtra(Intent.EXTRA_UID, 0);
        	int myPackageID = settingsFileManager.GetIntFromSettings(PREFS_PROCESS_ID);
			boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			
			// make sure it was my package that was uninstalled
			if (packageID == myPackageID){
				// make sure the user is not just updating to a new version
				// 'isReplacing' will be true if the user is updating to new version
				if(!isReplacing){
					
				}
			}
        }*/
        /*else if(intent.getAction().equals("android.intent.action.CAMERA_BUTTON")) {
        	
        	//if (settingsFileManager.GetBooleanFromSettings(PREFS_START_ON_CAMERA)){
	        	abortBroadcast();
	        	
	        	Light myLight = new Light();
	        	myLight.TurnOnTheLight(1);
	        	
	        	settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, true);
        	//}
        }
        else if (intent.getAction().equals("com.android.camera.NEW_PICTURE")){
        	if (settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON)){
        		Light myLight = new Light();
            	myLight.TurnOnTheLight(1);
            	
            	settingsFileManager.SaveBooleanToSettings(PREFS_IS_LED_ON, true);
        	}
        }*/
    }  

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		settingsFileManager = new SettingsFileManager(context);
    	
		if (settingsFileManager.GetIntFromSettings(PREFS_WIDGET_ICON) == 0)
			settingsFileManager.SaveIntToSettings(PREFS_WIDGET_ICON, WIDGET_ICON_FLAME_WITH_FRAME);
    	
		boolean isStrobeOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_STROBE_ON);
		boolean isMorseOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_MORSE_CODE_ON);
		boolean isLEDOn = settingsFileManager.GetBooleanFromSettings(PREFS_IS_LED_ON);
		
    	// Update each requested appWidgetId
        RemoteViews view = buildUpdate(context, appWidgetIds , isLEDOn, isStrobeOn, isMorseOn);
        
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
    static RemoteViews buildUpdate(Context context, int[] appWidgetId, boolean isLEDOn, boolean isStrobeOn, boolean isMorseOn) {
    	
        RemoteViews views = null;
        int widgetIcon = settingsFileManager.GetIntFromSettings(PREFS_WIDGET_ICON);
        
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
        
        views = new RemoteViews(context.getPackageName(), layoutID);
        
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
        RemoteViews views = buildUpdate(context, null, isLEDOn, isStrobeOn, isMorseOn);
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

    	int widgetIcon = settingsFileManager.GetIntFromSettings(PREFS_WIDGET_ICON);

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
        
        views = new RemoteViews(context.getPackageName(), layoutID);
    }

    /**
     * Creates PendingIntent to notify the widget of a button click.
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    private static PendingIntent getLaunchPendingIntent(Context context, int[] appWidgetId, int buttonId) {
        Intent launchIntent = new Intent("com.android.mototorchled.widget.CLICK");
        //launchIntent.setClass(context, FlashlightWidget.class);
        //launchIntent.setClass(context, context.getClass());
        //launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //launchIntent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 /* no requestCode */, launchIntent, 0 /* no flags */);
        return pi;
    }

    public void SetScreenBrightness(int value, Context context){
    	/*Intent i = new Intent();
    	i.setComponent(new ComponentName("com.alford.MotoTorchLED","com.alford.MotoTorchLED.MotoTorch"));
    	context.startActivity(i);
    	*/
    	/*android.provider.Settings.System.putInt(context.getContentResolver(), 
    		     android.provider.Settings.System.SCREEN_BRIGHTNESS, value);*/
    }
      
	
}
