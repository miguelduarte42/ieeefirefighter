import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;

public class SideSensors {

	/**
	 * TODO
	 * *
	 * -Voltar ao inicio de maneira diferente dependendo da sala da vela
	 * -Bumper 
	 *
	 */
	NXTRegulatedMotor mRight = new NXTRegulatedMotor(MotorPort.A);
	NXTRegulatedMotor mLeft = new NXTRegulatedMotor(MotorPort.B);
	NXTRegulatedMotor radar = new NXTRegulatedMotor(MotorPort.C);
	UltrasonicSensor sLeft = new UltrasonicSensor(SensorPort.S1);
	UltrasonicSensor sFront = new UltrasonicSensor(SensorPort.S2);
	UltrasonicSensor sRight = new UltrasonicSensor(SensorPort.S3);
	ColorSensor color = new ColorSensor(SensorPort.S4);
	BluetoothHandler bh;

	int averages[] = new int[5];
	int currentIndex = 0;
	int numberOfRooms = 0;
	double tacho_scale = 5.3;

	long ignoreWhiteTime = 5*1000;
	long startTime;

	boolean ignoreLine = false;

	private boolean seenWhite = false;
	private boolean stopWalk = false;
	private long detectWhiteTime = 0;
	private long whiteStripeThreshold = 400;
	private boolean finishedMission = false;
	private boolean returning = false;

	int currentRoom = 0;
	int temperatureThreshold = 7;
	
	private StopThread stopThread;
	private boolean started = false;
	private boolean movingToCandle = false;
	private boolean sheep = false;

	public SideSensors() {
		
		
//		int a = 1;
//		
//		while(a==1) {
//			Color c = color.getColor();
//			int threshold = 180;
//			System.out.println(c.getRed()+" "+c.getGreen()+" "+c.getBlue());
//		}
		
		try {
			bh = new BluetoothHandler();
		} catch (Exception e) {
			while(true)
				System.out.println("SHIT SHIT SHIT!");
		}

		getTemperatures(0);
		System.out.println("Start!");
		stopThread = new StopThread();
		stopThread.start();
		Sound.beep();
	}

	public void mainLoop() {
		
		while(bh.getButtons()[0] != '1');
		
		started = true;
		
		//make sure the sensors are working!
		for(int i = 0 ; i < 10 ; i++) {
			sLeft.getDistance();
			sRight.getDistance();
			sFront.getDistance();
			sleep(20);
		}
		
		if(sLeft.getDistance() < 40 && sRight.getDistance() < 40)
			sheep = true;
		
		if(sFront.getDistance() < 40 && sRight.getDistance() > 40)
			sheep = true;
		
		if(sheep)
			Sound.beepSequence();
		
//		int a = 1;
//		
//		while(a == 1);
		
		if(sRight.getDistance() > 30)
			turnTachos(90,true);

		startTime = System.currentTimeMillis();
		
		while(!finishedMission) {
			if(numberOfRooms < 5 && !returning){
				if(didntCrossLine()){
					hugRight();
				}else{
					currentRoom++;
					stopWalk=false;
					
					searchRoom();

					numberOfRooms++;
					stopWalk=false;
				}
			}else{
				if(didntCrossLine()){
					if(numberOfRooms <= 2)
						hugLeft();
					else
						hugRight();
				}else{
					Sound.beep();
					stopWalk=false;
					if(numberOfRooms <= 2)
						currentRoom--;
					ignoredSearchedRoom();
					Sound.beep();
					stopWalk=false;
				}
			}
		}
		updateWheelSpeeds(0, 0);
	}

	private boolean didntCrossLine() {

		if(System.currentTimeMillis() - startTime <= ignoreWhiteTime)
			return true;

		if(stopWalk == false && seenWhite == false){
			if(white()) {
				seenWhite = true;
				detectWhiteTime = System.currentTimeMillis();
			}
		}else if(stopWalk == false && seenWhite == true){

			long whiteTime = System.currentTimeMillis() - detectWhiteTime;

			if(whiteTime > whiteStripeThreshold && returning) {
				finishedMission = true;
			} else if(!white()){
				System.out.println(whiteTime);
				if(!returning && whiteTime > whiteStripeThreshold) {
					seenWhite = false;
					stopWalk = false;
					return true;
				}else {
					seenWhite=false;
					stopWalk = true;
					updateWheelSpeeds(0, 0);
					return false;
				}
			}
		}
		
		return !stopWalk;
	}

	public void searchRoom(){
		int lSpeed = 720;
		int rSpeed = 720;

		Integer angle = candleAngle();

		if(angle != null) {
			Sound.beepSequence();
			flashLed();
			if(angle > 0){
				rSpeed = 400;
			}else if(angle < 0){
				lSpeed = 400;
			}

			updateWheelSpeeds(lSpeed, rSpeed);
			sleep(650);
			
			if(Math.abs(angle) >= 60) {
				//extreme cases where the candle is in the corner next to the door
				sleep(1000);
			}
			
			updateWheelSpeeds(0, 0);
			
			angle = candleAngle(-30,30);
			
			if(angle == null)
				angle = candleAngle();
			
			if(angle == null)
				angle = candleAngle();
			
			if(angle == null)
				//crazy situation where the candle is detected by mistake
				ignoredSearchedRoom();
			else{
				
				turnLed(true);
				turnTachos(angle);
				
				movingToCandle = true;
				
				while(sFront.getDistance() > 15){
					int index = candleAccurateIndex();
					if(index <= 3){
						updateWheelSpeeds(2*300, 3*60);
					}else{
						updateWheelSpeeds(3*60, 2*300);
					}
				}
				updateWheelSpeeds(0, 0);
				System.out.println(maxTemperature());
				
				radar.rotateTo(0);
		
				Sound.beepSequence();
				
				turnFan(true);
	
				while(maxTemperature() >= 40)
					sleep(1000);
				sleep(1000);
				
				radar.rotateTo(0);
				updateWheelSpeeds(0, 0);
				
				turnFan(false);
				turnLed(false);
				
				movingToCandle = false;
				returning = true;
	
				updateWheelSpeeds(-300, -300);
				sleep(650);
				turnTachos(-90);
				
				while(didntCrossLine())
					hugRight();
			}
			
		}else {
			ignoredSearchedRoom();
		}
	}

	private void ignoredSearchedRoom() {
		int lSpeed = -720;
		int rSpeed = -720;

		updateWheelSpeeds(lSpeed, rSpeed);
		//change this value for small corridors!!

		if(currentRoom == 1) {
			sleep(1000);
			turnTachos(90);
		} else if(currentRoom == 2) {
			sleep(1000);
			turnTachos(-90);
		} else if(currentRoom == 3) {
			sleep(1000);
			turnTachos(-50);

			long st = System.currentTimeMillis();
			
			while(System.currentTimeMillis() - st < 1250 || sRight.getDistance() > 30)
				updateWheelSpeeds(720, 720);
				//hugLeft();
			Sound.beep();
		} else  if(currentRoom >= 4) {
			sleep(1000);
			updateWheelSpeeds(-300, -300);
			sleep(750);
			updateWheelSpeeds(0, 0);
			turnTachos(-90);
			long st = System.currentTimeMillis();
			while(System.currentTimeMillis() - st < 1250 || sRight.getDistance() > 30)
				hugLeft();
			Sound.beep();
			long time = System.currentTimeMillis();
			while(System.currentTimeMillis() - time < 750)
				hugLeft();
		} else {
			sleep(1000);
			turnTachos(-90);
		}
	}

	private boolean white() {
		Color c = color.getColor();
		int threshold = 170;
		return c.getRed() > threshold && c.getGreen() > threshold && c.getBlue() > threshold;
	}

	private void hugRight() {

		int lSpeed = 720;
		int rSpeed = 720;

		int rightDistance = sRight.getDistance();
		int leftDistance = sLeft.getDistance();
		int frontDistance = sFront.getDistance();
		
		int frontDistanceThreshold = 15;
		int rightDistanceThreshold = 20;
		
		if(sheep && ((currentRoom == 3 && returning) || currentRoom == 4) && sLeft.getDistance() > 30) {
			frontDistanceThreshold = 25;
			rightDistanceThreshold = 30;
		}
			
		if (frontDistance < frontDistanceThreshold) {
			//if we find an unexpected obstacle, such as the dog or a wall,
			//turn at full speed to the left.
			lSpeed = -720;		
		}else if (rightDistance > 15 && rightDistance < 20) {
			rSpeed /= 5-(20 - rightDistance);
		} else if(rightDistance >= rightDistanceThreshold) {
			//if there is no wall to the right, find one by sharply turning right
			rSpeed = 200;//250
		} else if (rightDistance < 15) {
			//if we are too close to the right wall, turn slightly to the left
			lSpeed /= 15 - rightDistance;
		}

		updateWheelSpeeds(lSpeed, rSpeed);
	}

	private void hugLeft() {

		int lSpeed = 720;
		int rSpeed = 720;

		int rightDistance = sRight.getDistance();
		int leftDistance = sLeft.getDistance();
		int frontDistance = sFront.getDistance();
		
		int frontDistanceThreshold = 15;

		if (frontDistance < frontDistanceThreshold) {
			//if we find an unexpected obstacle, such as the dog or a wall,
			//turn at full speed to the left.
			rSpeed = -720;			
		}else if (leftDistance > 15 && leftDistance < 20) {
			rSpeed /= 5-(20 - leftDistance);
		} else if(leftDistance >= 20) {
			//if there is no wall to the right, find one by sharply turning right
			lSpeed = 200;//250

		} else if (leftDistance < 15) {
			//if we are too close to the right wall, turn slightly to the left
			rSpeed /= 15 - leftDistance;
		}
		updateWheelSpeeds(lSpeed, rSpeed);
	}


	private Integer candleAngle(int startAngle, int finishAngle) {
		
		startAngle*=-1;
		finishAngle*=-1;
		
		radar.rotateTo(startAngle);
		
		int increment = 10;
		int max = 0;
		int angle = 0;
		int index = 0;
		System.out.println("before");
		int ambient = (getTemperatures(0)[8] + getTemperatures(1)[8]) / 2;
		System.out.println("after");
		boolean foundCandle = false;
		
		for(int i = startAngle ; i > finishAngle && !foundCandle ; i-=increment) {
			int[] temps = getTemperatures(0);
			int[] temps2 = getTemperatures(1);
			
			for(int j = 0 ; j < temps.length ; j++) {
				int t = temps[j];
				if(t > max) {
					max = t;
					angle = i*-1;
					index = j;
				}
			}
			for(int j = 0 ; j < temps2.length ; j++) {
				int t = temps[j];
				if(t > max) {
					max = t;
					angle = i*-1;
					index = j;
				}
			}
			radar.rotate(-increment);
		}
		radar.rotateTo(0);
		
		if(max > ambient+temperatureThreshold) {
			System.out.println("index "+index);
			if(index > 4) {
				//adjust 5 degrees for each index position
				angle-= (7-index)*5;
			} else if(index < 3) {
				angle+= (3-index)*5;
			}
			return angle;
		}
		return null;
	}
	
	private int candleAccurateIndex(){
		int place = 0;
		int max = 0;
		int[] temps = getTemperatures(0);
		int[] temps2 = getTemperatures(1);

		for(int i = 0; i < temps.length - 1; i++)
			if(temps[i] > max) {
				place = i;
				max = temps[i];
			}
		for(int i = 0; i < temps2.length - 1; i++)
			if(temps2[i] > max) {
				place = i;
				max = temps2[i];
			}
		return place;
	}
	
	private void updateWheelSpeeds(int lSpeed, int rSpeed) {
		mLeft.setSpeed(Math.abs(lSpeed));
		mRight.setSpeed(Math.abs(rSpeed));
		mLeft.forward();
		mRight.forward();

		if (lSpeed < 0)
			mLeft.backward();
		if (rSpeed < 0)
			mRight.backward();
	}
	
	private void turnTachos(int turn_degree, boolean slow) {
		int leftTacho = mLeft.getTachoCount();
		int rightTacho = mRight.getTachoCount();
		
		int speed = 720/2;
		
		if(slow)
			speed/=2;
		
		if(turn_degree < 0){
			updateWheelSpeeds(-speed, speed);
			while(mRight.getTachoCount() < rightTacho + (tacho_scale * -turn_degree));
		}else{
			updateWheelSpeeds(speed, -speed);	
			while(mLeft.getTachoCount() < leftTacho + (tacho_scale * turn_degree));
		}
		updateWheelSpeeds(0, 0);
	}

	private void turnTachos(int turn_degree){
		turnTachos(turn_degree, false);
	}
	
	private int maxTemperature(int sensorNumber) {
		int max = 0;
		int[] temps = getTemperatures(sensorNumber);
		
		for(int t : temps){
			if(t > max){
				max = t;
			}
		}
		return max;
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Integer candleAngle(){
		return candleAngle(-70, 120);
	}
	
	private int maxTemperature(){
		return Math.max(maxTemperature(0),maxTemperature(1));
	}

	private int[] getTemperatures(int sensorNumber) {
		return bh.getTemperatures(sensorNumber);
	}

	private void turnFan(boolean on) {
		bh.turnFan(on);
	}

	private void turnLed(boolean on) {
		bh.turnLed(on);
	}
	
	private void flashLed() {
		bh.flashLed();
	}

	public static void main(String[] args) {
		new SideSensors().mainLoop();
	}
	class StopThread extends Thread{
		@Override
		public void run() {
			super.run();
			
			long buttonTime = System.currentTimeMillis();
			long buttonTimeThreshold = 2000;
			
			while(true) {
				if(started && !movingToCandle && System.currentTimeMillis() - buttonTime > buttonTimeThreshold) {
					if(sFront.getDistance() > 20) {
						buttonTime = System.currentTimeMillis();
						if(bh.getButtons()[1] == '1')
							finishedMission = true;
					}
				}
			}
		}
	}
}