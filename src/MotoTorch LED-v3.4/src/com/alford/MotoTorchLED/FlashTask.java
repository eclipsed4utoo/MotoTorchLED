package com.alford.MotoTorchLED;

import android.hardware.Camera;
import android.os.AsyncTask;

public class FlashTask extends AsyncTask<Void, Void, Void> {
    public boolean flashOn;
    public Camera camera;
    protected void onPostExecute() {
        
    }

	private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera cameraArg) {
        	//if (flashOn){
        	//	camera.autoFocus(autoFocusCallback);
        	//}
        	//Toast.makeText(m_context, "Is focused? " + success, Toast.LENGTH_SHORT).show();
        }
    };
    
	@Override
	protected Void doInBackground(Void... params) {
		flashOn = true;
		while(flashOn){
			camera.autoFocus(autoFocusCallback);
		}
		return null;
	}
}