package com.alford.MotoTorchLED;

import android.content.Context;
import android.hardware.Camera;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class Light {

	private static final String TAG = "MotoTorch LED - Light";
	
	private Camera camera;
	private Preview mPreview;
	
	public Light(Context mContext){
		
		if (Utilities.IsAndroid22OrHigher() && !Utilities.IsDROID()){
			
			Log.d(TAG, "Android version is 2.2 or higher");
			
			mPreview = new Preview(mContext);
			camera = mPreview.GetCameraObject();
			
			boolean isCameraNull = false;
			isCameraNull = (camera == null);
			
			Log.d(TAG, "Is Camera Null? " + isCameraNull);
		}
		else{
			Log.d(TAG, "Android version is 2.1 or lower");
		}
	}
	
	//public Light(Camera _camera){
	//	camera = _camera;
	//}
	
	public boolean IsFlashOn(){
		Log.d(TAG, "Determining if LEDs are on");
		
		boolean isOn = false;
				
		// Android version 7 or less has to use the Hardware jar class
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
			try {
				IHardwareService hardware = IHardwareService.Stub.asInterface(
		                ServiceManager.getService("hardware"));
				isOn = hardware.getFlashlightEnabled();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else{
			// version 8 is Froyo.
			// uses the Torch flashmode
			
			if (camera == null){
				return false;
			}
			
			try{
				Camera.Parameters parameters = camera.getParameters();
		  		
		  		if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)){
		  			isOn = true;
		  		}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
		if (isOn){
			Log.d(TAG, "LEDs are on.");
		}
		else{
			Log.d(TAG, "LEDs are off.");
		}
			
		return isOn;
	}
	
	public void TurnOnTheLight()
	{
		Log.d(TAG, "Turning on the LEDs.");
		
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
			// For Motorola Droid and Milestone running 2.1
			try {
				IHardwareService hardware = IHardwareService.Stub.asInterface(
		        ServiceManager.getService("hardware"));		
				hardware.setFlashlightEnabled(true);
			} catch (Exception e1) {
				e1.printStackTrace();
			}	
		}
		else{
			
			if (camera == null){
				return;
			}
			
			// for all 2.2 devices
			try{
		  		Camera.Parameters parameters = camera.getParameters();
		  		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		  		camera.setParameters(parameters);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		Log.d(TAG, "LEDs are now on.");
	}
	
	public void TurnOffTheLight()
	{
		Log.d(TAG, "Turning LEDs off.");
		
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
			// For Motorola Droid and Milestone running 2.1
			try {
				IHardwareService hardware = IHardwareService.Stub.asInterface(
		        ServiceManager.getService("hardware"));	
				hardware.setFlashlightEnabled(false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}	
		}
		else{
			
			if (camera == null){
				return;
			}
			
			// for all 2.2 devices
			try{
		  		Camera.Parameters parameters = camera.getParameters();
		  		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
		  		camera.setParameters(parameters);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
		Log.d(TAG, "LEDs are now off.");
	}
	
	public void ChangeScreenBrightness(int value){
		try {
			IHardwareService hardware = IHardwareService.Stub.asInterface(
	        ServiceManager.getService("hardware"));
			hardware.setScreenBacklight(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void ReleaseCamera(){
		Log.d(TAG, "Releasing camera.");
		
		try{
			camera.unlock();
			camera.release();
			//mPreview.DestroyCamera();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		Log.d(TAG, "Camera released.");
	}
}
