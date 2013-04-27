package com.alford.MotoTorchLED;

import android.os.Build;

public class Utilities {
	
	public static boolean IsDROID(){
		// "droid" is used by ChevyNo1's Simply Stunning ROMs
		// "Moto Droid" is used by JRummy's LithiumMod ROM
		// "Droid" is used by most ROMs and the official Motorola build
		// "DROID2" is used by the official Motorola build for the DROID 2.
		// added additional handling for future DROID 2 ROMs that might change
		//    the name.
		// No idea what the model is for the DROIDX but it seems to fail now with
		//   2.2.  So added logic for it.  Had to try to encompass all possible 
		//   spellings.
		return (Build.MODEL.equals("Droid") || 
				Build.MODEL.equals("droid") ||
				Build.MODEL.equals("Moto Droid") || 
				Build.MODEL.equals("DROID") ||
				Build.MODEL.equals("Droid2") ||
				Build.MODEL.equals("Droid 2") ||
				Build.MODEL.equals("droid2") ||
				Build.MODEL.equals("droid 2") ||
				Build.MODEL.equals("DROID2") ||
				Build.MODEL.equals("DROID 2") ||
				Build.MODEL.equals("Moto Droid2") ||
				Build.MODEL.equals("Moto Droid 2") ||
				Build.MODEL.equals("DroidX") ||
				Build.MODEL.equals("DROIDX") ||
				Build.MODEL.equals("droidx") ||
				Build.MODEL.equals("droidX") ||
				Build.MODEL.equals("Droid X") ||
				Build.MODEL.equals("DROID X"));
	}
	
	public static boolean IsAndroid22OrHigher(){
		int version = Build.VERSION.SDK_INT;
		return (version >= 8);
	}
	
	//public static int getAndroidVersion()
	//{
	//	return Build.VERSION.SDK_INT;
	//}
	
	//public static String getModelOfPhone(){
	//	String data;
		
	//	data = "Board: " + Build.BOARD;
		//data += "Board: " + Build.;
	//	data += "Board: " + Build.BOARD;
	//	data += "Board: " + Build.BOARD;
		
	//	return data;
	//}
}
