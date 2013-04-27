package com.alford.MotoTorchLED;

import android.os.AsyncTask;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;

public class MorseCode extends AsyncTask<Void, Void, Void>{
	
	public String m_data;
	public boolean keepOn = true;
	public boolean runContinuously = false;
	
	private Light myLight = null;
	
	public MorseCode(String data, Light _myLight){
		m_data = data;
		myLight = _myLight;
	}

	public void ParseData() {
		char[] myCharArray = new char[m_data.length()];
		m_data.toUpperCase().getChars(0, m_data.length(), myCharArray, 0);
		IHardwareService hardware = null;
		
		if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				hardware = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));	
		}
		
		while(keepOn){
			for(int i = 0; i < myCharArray.length; i++){
				if (myCharArray[i] == ' '){
					PauseForSpace();
				}	
				else {
					PauseForNextLetter();
				}
				
				switch(myCharArray[i]){
					case 'A': 
						Dot(hardware);
						Dash(hardware);
						break;
					case 'B':
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case 'C':
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case 'D':
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case 'E':
						Dot(hardware);
						break;
					case 'F':
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case 'G':
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case 'H':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case 'I':
						Dot(hardware);
						Dot(hardware);
						break;
					case 'J':
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case 'K':
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case 'L':
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case 'M':
						Dash(hardware);
						Dash(hardware);
						break;
					case 'N':
						Dash(hardware);
						Dot(hardware);
						break;
					case 'O':
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case 'P':
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case 'Q':
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case 'R':
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case 'S':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case 'T':
						Dash(hardware);
						break;
					case 'U':
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case 'V':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case 'W':
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case 'X':
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case 'Y':
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case 'Z':
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '0':
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case '1':
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case '2':
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case '3':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case '4':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case '5':
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '6':
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '7':
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '8':
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '9':
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case '.':
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
					case ',':
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						break;
					case '?':
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '!':
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case ':':
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						break;
					case '"':
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case '\'':
						Dot(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dash(hardware);
						Dot(hardware);
						break;
					case '=':
						Dash(hardware);
						Dot(hardware);
						Dot(hardware);
						Dot(hardware);
						Dash(hardware);
						break;
				}
			}
			
			if(!runContinuously){
				keepOn = false;
			}
			else{
				PauseForSpace();
			}
		}
	}
	
	private void Dot(IHardwareService hardware){

		try {
			if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				hardware.setFlashlightEnabled(true);
			}
			else{
				myLight.TurnOnTheLight();
			}
				
			DurationForDot();
			
			if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				hardware.setFlashlightEnabled(false);
			}
			else {
				myLight.TurnOffTheLight();
			}
			
			DurationBetweenDashDot();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	private void Dash(IHardwareService hardware){

		try {
			if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				hardware.setFlashlightEnabled(true);
			}
			else{
				myLight.TurnOnTheLight();
			}
			
			DurationForDash();
			
			if (!Utilities.IsAndroid22OrHigher() || Utilities.IsDROID()){
				hardware.setFlashlightEnabled(false);
			}
			else {
				myLight.TurnOffTheLight();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		DurationBetweenDashDot();
	}

	private void PauseForSpace(){
		try {
			Thread.sleep(700);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void PauseForNextLetter(){
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void DurationForDash(){
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void DurationForDot(){
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void DurationBetweenDashDot(){
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		ParseData();	
		return null;
	}
	
}