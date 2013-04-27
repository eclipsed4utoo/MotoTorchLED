package com.alford.MotoTorchLED;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
  SurfaceHolder mHolder;
  Camera mCamera;
  
  private static final String TAG = "MotoTorch LED - Preview";

  public Preview(Context context) {
      super(context);
    
      // Install a SurfaceHolder.Callback so we get notified when the
      // underlying surface is created and destroyed.
      mHolder = getHolder();
      mHolder.addCallback(this);
      mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
      
      try {
    	  mCamera = Camera.open();
          mCamera.setPreviewDisplay(mHolder);
      } catch (IOException exception) {
          mCamera.release();
          mCamera = null;
          // TODO: add more exception handling logic here
      }
      
      Log.d(TAG, "Constructor completed");
  }

  public void surfaceCreated(SurfaceHolder holder) {
      // The Surface has been created, acquire the camera and tell it where
      // to draw.
	  Log.d(TAG, "Surface being created");
      try {
    	  mCamera = Camera.open();
          mCamera.setPreviewDisplay(holder);
      } catch (IOException exception) {
          mCamera.release();
          mCamera = null;
          // TODO: add more exception handling logic here
      }
      Log.d(TAG, "Surface created");
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
      // Surface will be destroyed when we return, so stop the preview.
      // Because the CameraDevice object is not a shared resource, it's very
      // important to release it when the activity is paused.
	  Log.d(TAG, "Surface being destroyed");
	  
	  if (mCamera != null){
		  mCamera.stopPreview();
	      mCamera = null;
	  }
	  
	  Log.d(TAG, "Surface being destroyed");
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      // Now that the size is known, set up the camera parameters and begin
      // the preview.
	  Log.d(TAG, "Surface changing");
	  
      Camera.Parameters parameters = mCamera.getParameters();
      parameters.setPreviewSize(w, h);
      mCamera.setParameters(parameters);
      mCamera.startPreview();
      
      Log.d(TAG, "Surface changed");
  }
  
  public void DestroyCamera(){
	  Log.d(TAG, "Destroying Camera Object");
	  
	  surfaceDestroyed(mHolder);
  }
  
  public Camera GetCameraObject(){
	  Log.d(TAG, "Getting Camera Object");
	  return mCamera;
  }

}
