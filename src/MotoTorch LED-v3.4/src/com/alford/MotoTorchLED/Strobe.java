package com.alford.MotoTorchLED;

import java.util.Calendar;

import android.os.AsyncTask;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;

public class Strobe extends AsyncTask<Void, Void, Void> {
	
	public boolean keepOn = false;
	public int delay;
	public int duration;
	
	private Light myLight = null;
	
	@Override
	protected Void doInBackground(Void... arg0) {
		StartStrobe();
		return null;
	}
	
	public Strobe(Light _myLight){
		myLight = _myLight;
	}
	
	public void StartStrobe(){
		IHardwareService hardware = null;
		
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
			hardware = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));
		}
		
		while (keepOn){
			try {
				if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
					hardware.setFlashlightEnabled(true);
				}
				else{
					myLight.TurnOnTheLight();
				}
				
				PauseForDuration();
				
				if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
					hardware.setFlashlightEnabled(false);
				}
				else {
					myLight.TurnOffTheLight();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			PauseForDelay();
		}
	}
	
	private void PauseForDuration(){
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void PauseForDelay(){
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void StartStrobe(int duration){
		IHardwareService hardware = null;
		
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
			hardware = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));
		}
		
		Calendar calNow = Calendar.getInstance();
		Calendar calFuture = Calendar.getInstance();
		calFuture.add(Calendar.SECOND, duration);
		
		while(calNow.before(calFuture)){
			try {
				if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
					hardware.setFlashlightEnabled(true);
				}
				else{
					myLight.TurnOnTheLight();
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
					hardware.setFlashlightEnabled(false);
				}
				else {
					myLight.TurnOffTheLight();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			calNow = Calendar.getInstance();
		}		
	}
}
