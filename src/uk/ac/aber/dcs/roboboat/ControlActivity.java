package uk.ac.aber.dcs.roboboat;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.SeekBar;

public class ControlActivity extends IOIOActivity
implements OnSeekBarChangeListener {
	public static BoatControl controller;
	public static PhoneSensors phoneSensors;
	private static HTTPServer httpServer;
	private CameraThread cameraThread;
	private Handler handler = new Handler();
	private String status = "";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((SeekBar)findViewById(R.id.sailDirection))
        	.setOnSeekBarChangeListener(this);
        phoneSensors = new PhoneSensors(this.getApplicationContext());
        httpServer = HTTPServer.getInstance();
        httpServer.context = this;
        httpServer.exec();
        
        //cameraThread = new CameraThread(this);
        //cameraThread.start();
        
        log("HTTP Server started at "+httpServer.getLocalIp());
        handler.postAtFrontOfQueue(logTask);
        ((TextView) findViewById(R.id.console)).setMovementMethod(
        		new ScrollingMovementMethod()
        );
    }
    
    /*@Override
    public void onStop() {
    	httpServer.isRunning = false;
    }*/
    
    private Runnable logTask = new Runnable() {
    	@Override
    	public void run() {
    		JSONObject json = new JSONObject();
    		try{
    		json.accumulate("phoneBattery",phoneSensors.battery);
    		if(phoneSensors.location != null) {
    			JSONArray location = new JSONArray();
    			location.put(phoneSensors.location.getLongitude());
    			location.put(phoneSensors.location.getLatitude());
    			json.accumulate("location",location);
    		}
    		if(phoneSensors.orientation != null) {
    			JSONArray orientation = new JSONArray();
    			for(float f : phoneSensors.orientation) {
    				orientation.put(f);
    			}
    			json.accumulate("orientation",orientation);
    		}
    		if(phoneSensors.apr != null) {
    			JSONArray apr = new JSONArray();
    			for(float f : phoneSensors.apr) {
    				apr.put(f);
    			}
    			json.accumulate("apr",apr);
    		}
    		if(controller != null)
    			json.accumulate("wind",controller.getWindDirection());
    		} catch(JSONException e) {
    			log("JSONException: "+e.getMessage());
    		}
    		httpServer.message = json.toString();
    		
    		// TODO: Talk to Alex about getting camera to work?
    		/*if(cameraThread != null && !cameraThread.takingPicture)
    			httpServer.camera = cameraThread.image;*/
    		handler.postDelayed(this, 1000);
    	}
    };
    
    @Override
    protected IOIOLooper createIOIOLooper() {
    	if(controller == null)
    		controller = BoatControl.getInstance();
    		controller.context = this;
    	return controller;
    }
    
    public void log(String msg) {
    	TextView console = (TextView) findViewById(R.id.console);
    	console.append("\n"+msg);
    	if(console != null) {
    		final Layout layout = console.getLayout();
    		if(layout != null) {
    			int scrollDelta =
    				(layout.getLineBottom(console.getLineCount()-1) -
    				console.getScrollY() - console.getHeight());
    			if(scrollDelta > 0)
    				console.scrollBy(0, scrollDelta);
    		}
    	}
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		progress -= 90;
		if(controller != null) {
			controller.setServoAngle(controller.SERVO_SAIL, progress);
		}
		
	}

	public void forceQuit(View v) {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
}