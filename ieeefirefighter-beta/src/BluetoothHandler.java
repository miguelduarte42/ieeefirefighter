import java.io.*;
import javax.bluetooth.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;

/**
 * This class is responsible for all Bluetooth communication between the brick
 * and a computer. It allows the sending and receiving of messages.
 */
public class BluetoothHandler {

	/** indicates if the bluetooth communication is active **/
	private boolean connectionAvailable = false;

	private InputStream inputStream;
	private OutputStream outputStream;

	public BluetoothHandler() throws Exception {

		LCD.clearDisplay();

		System.out.println("Connecting to BT!");
		RemoteDevice btrd = Bluetooth.getKnownDevice("BlueBee");
		BTConnection connection = Bluetooth.connect(btrd.getBluetoothAddress(), NXTConnection.RAW);
		// BTConnection connection =
		// Bluetooth.connect(Bluetooth.getKnownDevice("BlueBee"));
		// BTConnection connection = Bluetooth.waitForConnection();

		if (connection == null) {
			LCD.drawString("No BT connection", 0, 1);
			throw new Exception();
		}

		System.out.println("Connected to BT!");
		connectionAvailable = true;

		inputStream = connection.openInputStream();
		outputStream = connection.openOutputStream();
//		 LCD.drawString("Opened Streams", 0, 2);
//		 LCD.clear();
//		 int i=0;
//		 boolean b = true;
//		 
//		 while(true) {
//			System.out.println("vou enviar");
//			if(b)
//				outputStream.write((int)'a');
//			else
//				outputStream.write((int)'b');
//			outputStream.flush();
//			b = !b;
//		 	System.out.println("enviei "+b);
//		 	System.out.println("Recebi: "+inputStream.read());
////		 	System.out.println("oi");
//		 	Thread.sleep(500);
//		 }
		

//		this.dataReceiver = new DataReceiver(inputStream);
//		LCD.drawString("DR", 0, 3);
//		this.dataReceiver.start();
//		LCD.drawString("start!", 0, 4);
	}
	
	public int[] getTemperatures(int sensorNumber) {
		
		int[] values = new int[9];
		
		try {
			char sensor = (char)('a'+sensorNumber);
			
			outputStream.write((int)sensor);
			outputStream.flush();
			byte[] b = new byte[9]; 
//			inputStream.read(b, 0, 9);
			
			for(int i = 0 ; i < 9 ; i++)
				values[i] = inputStream.read();
		} catch(Exception e) {
			Sound.beep();
			System.out.println(e.getMessage());
		}
		
		return values;
	}
	
	public void turnLed(boolean on) {
		
		char led = on ? 'L' : 'l';
		
		try {
			outputStream.write((int)led);
			outputStream.flush();
		} catch(Exception e) {
			Sound.beep();
			System.out.println(e.getMessage());
		}
	}
	
	public void turnFan(boolean on) {
		
		char fan = on ? 'R' : 'r';
		
		try {
			outputStream.write((int)fan);
			outputStream.flush();
		} catch(Exception e) {
			Sound.beep();
			System.out.println(e.getMessage());
		}
	}
	
	public int[] getButtons() {
		
		int[] values = new int[4];
		
		try {
			outputStream.write((int)'s');
			outputStream.flush();
//			byte[] b = new byte[4]; 
//			inputStream.read(b, 0, 4);
//			
//			for(int i = 0 ; i < 4 ; i++)
//				values[i] = b[i];
			values[0] = inputStream.read();
			values[1] = inputStream.read();
			values[2] = inputStream.read();
			values[3] = inputStream.read();
			
		} catch(Exception e) {
			Sound.beep();
			System.out.println(e.getMessage());
		}
		
		return values;
	}

	public boolean getStatus() {
		return connectionAvailable;
	}

	public static void main(String[] args) {
		try {
			BluetoothHandler bh = new BluetoothHandler();
			
//			bh.turnLed(true);
//			System.out.println(1);
//			Thread.sleep(2000);
//			
//			bh.turnLed(false);
//			System.out.println(2);
//			Thread.sleep(2000);
//			
//			bh.turnFan(true);
//			System.out.println(3);
//			Thread.sleep(2000);
//			
//			bh.turnFan(false);
//			System.out.println(4);
//			Thread.sleep(2000);
			
			while(true) {
				System.out.println("press");
				Button.waitForAnyPress();
				System.out.println("pressed");
				
				int[] buttons = bh.getButtons();
				for(int i : buttons)
					System.out.print((char)i+" ");
				System.out.println();
				System.out.println("__");
				Button.waitForAnyPress();
				
				int[] sensor1 = bh.getTemperatures(0);
				for(int i : sensor1)
					System.out.println(i);
				System.out.println();
				Thread.sleep(1000);
				
			}
			
//			while(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
