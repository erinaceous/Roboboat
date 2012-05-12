package uk.ac.aber.dcs.roboboat;

import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraThread
implements Camera.ShutterCallback, Camera.PictureCallback {
	public byte[] image;
	private ControlActivity context;
	private Camera camera;
	private Camera.Parameters params;
	private CameraThread ref;
	public boolean takingPicture = false;
	
	CameraThread(ControlActivity context) {
		this.context = context;
		camera = Camera.open();
		params = camera.getParameters();
		params.setJpegQuality(50);
		params.setPictureSize(640,480);
		params.setColorEffect(Camera.Parameters.EFFECT_MONO);
		camera.setParameters(params);
		ref = this;
	}
	
	private Runnable cameraTask = new Runnable() {
		private Handler handler = new Handler();
		
		@Override
		public void run() {
			takePicture();
			handler.postDelayed(this, 5000);
		}
		
	};
	
	public void start() {
		this.cameraTask.run();
	}
	
	public void takePicture() {
		if(takingPicture) return;
		if(camera == null || this.context == null) return;
		SurfaceHolder holder =
			((SurfaceView)this.context.findViewById(R.id.cameraSurface)).getHolder();
		if(holder == null) return;
		//camera = Camera.open();
		//camera.setParameters(params);
		takingPicture = true;
		camera.startPreview();
		camera.takePicture(null, null, this);
		camera.stopPreview();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		this.image = data;
		takingPicture = false;
		//camera.release();
	}

	@Override
	public void onShutter() {
		
	}
}
