package uk.ac.aber.dcs.roboboat;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

public class BoatControl extends BaseIOIOLooper {
	public static final BoatControl instance = new BoatControl();
	public static BoatControl getInstance() {
		return instance;
	}
	
	public PwmOutput SERVO_RUDDER;
	public PwmOutput SERVO_SAIL;
	public static final int WIND_SENSOR = 6;
	public static final Spec.Mode SPEC_MODE = Spec.Mode.NORMAL;
	
	/* Wind sensor PWM ranges (relative to sensor not world) */
	public static final double ZERO_DEGREES = 3.105000068899244E-4;
	
	public ControlActivity context;
	private DigitalOutput led;
	private boolean led_ = true;
	private PwmOutput[] servos;
	private int angle = 0;
	private double windDirection = 0;
	private int servo_sail_angle;
	private int servo_rudder_angle;
	private PulseInput windPulse;
	private int[] angles;
	private double windCalibrated = 0;

	BoatControl(ControlActivity c) {
		super();
		this.context = c;
		
		servos = new PwmOutput[2];
		angles = new int[2];
		angles[0] = 1500; angles[1] = 1500;
	}
	
	BoatControl() {
		this(null);
	}
	
	@Override
	public void setup() throws ConnectionLostException {
		try{
		log("IOIO Connected,  configuring");
		led = ioio_.openDigitalOutput(0, false);

		SERVO_SAIL =
			ioio_.openPwmOutput(new Spec(7, SPEC_MODE), 50);
		SERVO_RUDDER =
			ioio_.openPwmOutput(new Spec(10, SPEC_MODE), 50);
		
		windPulse = ioio_.openPulseInput(WIND_SENSOR, PulseMode.NEGATIVE);
		//windPulse = ioio_.openDigitalInput(WIND_SENSOR);
		//windCalibrated = calibrateWindSensor();
		
		log("Sail Servo output on pin 10");
		log("Rudder Servo output on pin 7");
		log("Wind Sensor input on pin "+WIND_SENSOR);
		log("-----------------------------");
		log("Thunderbirds are go!");
		} catch(Exception e) {
			log("IOIO Not configured correctly!");
			log("Exception: "+e.getMessage());
		}
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		led_ = !led_;
		led.write(led_);
		
		//SERVO_SAIL.setPulseWidth(servo_sail_angle);
		//SERVO_RUDDER.setPulseWidth(servo_rudder_angle);
		//windDirection = windPulse.getDuration();
		windDirection = readWindSensor();
	}

	@Override
	public void disconnected() {
		log("IOIO Board disconnected");

	}

	@Override
	public void incompatible() {
		log("IOIO Board incompatible with API");

	}
	
	/**
	 * Log a message to the console on the app interface.
	 * Obviously only works if the app is visible, otherwise nothing is logged.
	 * @param msg Message to log
	 */
	public void log(final String msg) {
		final ControlActivity c = this.context;
		if(c == null) return;
		c.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				c.log(msg);
			}
		});
	}
	
	public double getWindDirection() {
		return this.windDirection;
	}
	
	/**
	 * Move a servo to the specified angle in degrees.
	 * Angle is from -90 (full left) to +90 (full right), with 0 as centered.
	 * <b>Does not move immediately;</b> servo will change direction during the
	 * next main loop iteration (BoatControl.loop()).
	 * @param servo Servo pin number
	 * @param angle Angle (in degrees) to turn servo to
	 */
	public void setServoAngle(PwmOutput servo, int angle) {
		setServoAngleRaw(servo, angleInMicros(angle));
	}
	
	/**
	 * Move a servo to specified angle by giving it a new pulse width.
	 * Range is 1000 to 2000 (microseconds).
	 * <b>Also does not move immediately;</b> servo will change direction during
	 * the next main loop iteration (BoatControl.loop()).
	 * @param servo Servo pin number
	 * @param angle Angle (in microseconds) to turn servo to
	 */
	public void setServoAngleRaw(PwmOutput servo, int angle) {
		log("Servo "+servo+": angle = "+angle+"uS");
		if(servo != null) {
			try {
				servo.setPulseWidth(angle);
			} catch (ConnectionLostException e) {
				log("Servo ConnectionLost");
			}
		}
	}
	
	/**
	 * Convert an angle (in degrees) into a pulse width, to be used by
	 * common servos  (1000 microseconds = full left, 2000 = full right,
	 * 1500 = centered).
	 * <b>NOTE:</b> Clamps values in the range > -90 to < +90.
	 * @param degrees
	 * @return
	 */
	public static int angleInMicros(int degrees) {
		if(degrees < -90) return 1000;
		if(degrees > 90) return 1000;
		return (int)(((float)degrees+90) / 0.18) + 1000;
	}
	
	/* This is Colin's way */
	/* Slooooow on IOIO. Only works on PIC. */
	/*public double readWindSensorRaw() {
		int high_count=0, low_count=0;
		try{
			for(int i=0; i<20; i++) {
				while(windPulse.read() == false) {
					low_count++;
				}
				while(windPulse.read() == true) {
					high_count++;
				}
			}
		} catch(Exception e) { }
		double angle = 0.0;
		angle = high_count/((high_count+low_count)/360);
		
		if(angle > 359) angle = 359;
		if(angle < 0) angle = 0;
		return angle;
	}*/
	
	public double readWindSensor() {
		//return (readWindSensorRaw()+windCalibrated) % 360;
		try{
			return windPulse.getDuration();
		} catch(Exception e) {
			return 0.0;
		}
	}
	
	/*public double calibrateWindSensor() {
		return readWindSensorRaw();
	}*/

}
