package uk.ac.aber.dcs.roboboat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import android.os.Handler;
import android.util.Log;

public class HTTPServer extends Thread {
	public final static String infoPage =
			"<b>TODO: Implement this.</b>";
	
	public ControlActivity context;
	private Socket clientSocket;
	private ServerSocket serverSocket;
	private BufferedReader input;
	private OutputStream output;
	final static Handler handler = new Handler();
	private String host;
	private int port = 5000;
	public String message = "{error: \"Not properly initialized\"}";
	public byte[] camera;
	
	public boolean isRunning = false;
	public static HTTPServer singleton;
	
	public static HTTPServer getInstance() {
		if(singleton == null)
			singleton = new HTTPServer();
		return singleton;
	}
	
	public void exec() {
		if(this.isRunning) return;
		this.start();
	}
	
	public void run() {
		try{
			host = getLocalIp();
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			
			isRunning = true;
			while(isRunning) {
				clientSocket = serverSocket.accept();
				input = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream(), "ISO-8859-2"));
				output = clientSocket.getOutputStream();
				
				String sAll = getStringFromInput(input);
				final String header = sAll.split("\n")[0];
				
				if(header.equals("GET / HTTP/1.1")) {
					send("text/plain",message);
				} else if(header.equals("GET /info HTTP/1.1")) {
					send("text/html",infoPage);
				} else if(header.equals("GET /camera HTTP/1.1")) {
					if(camera != null) send("image/jpeg",camera);
					else send("image/jpeg","");
				} else if(header.startsWith("GET /steer/sail/", 0)) {
					String[] split = header.split(" ")[1].split("/");
					for(int i=0; i<split.length; i++) {
						if(split.length == 4 &&
								context != null  &&
								context.controller != null) {
							send("text/plain","OK");
							context.controller.setServoAngle(
								context.controller.SERVO_SAIL,
								Integer.parseInt(split[3]));
						} else {
							send("text/plain", "ERROR");
						}
					}
				}
				closeInputOutput();
			}
		} catch(Exception e) {
			log("server loop error - "+e.toString());
		}
		isRunning = false;
	}
	
	public void send(String contentType, String s) {
		send(contentType, s.getBytes());
	}
	
	public void send(String contentType, byte[] b) {
		String header =
			"HTTP/1.1 200 OK\n" +
			"Connection: close\n" +
			"Content-type: "+contentType+"; charset=utf-8\n"+
			"Content-Length: "+b.length+"\n"+
			"\n";
		
		try{
			output.write(header.getBytes());
			output.write(b);
		} catch(Exception e) {
			log("Send exception - "+e.toString());
		}
	}
	
	public void closeInputOutput() {
		try{
			input.close();
			output.close();
			clientSocket.close();
		} catch(Exception e) {
			log("error when closing sockets - "+e.toString());
		}
	}
	
	public static String getLocalIp() {
		try{
			for(Enumeration<NetworkInterface> en =
					NetworkInterface.getNetworkInterfaces();
					en.hasMoreElements();) {
				
				NetworkInterface intf = en.nextElement();
				for(Enumeration<InetAddress> enumIpAddr =
					intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					
					InetAddress inetAddress = enumIpAddr.nextElement();
					if(!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch(SocketException e) {
			Log.e("boatHTTP","getLocalIp failed - "+e.toString());
		}
		return null;
	}
	
	public static String getStringFromInput(BufferedReader input) {
		StringBuilder sb = new StringBuilder();
		String tmp;
		try{
			while(!(tmp = input.readLine()).equals("")) {
				sb.append(tmp+"\n");
			}
		} catch(IOException e) {
			return "";
		}
		return sb.toString();
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
}
