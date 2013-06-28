import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.ColorSensor.Color;
import lejos.nxt.LCD;
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
	private long whiteStripeThreshold = 500;
	private boolean finishedMission = false;
	private boolean returning = false;

	int currentRoom = 0;
	int temperatureThreshold = 7;

	public SideSensors() {
		try {
			bh = new BluetoothHandler();
		} catch (Exception e) {
			while(true)
				System.out.println("SHIT SHIT SHIT!");
		}

		getTemperatures(0);
		System.out.println("Start!");
		Sound.beep();
	}

	public void mainLoop() {
		
		startTime = System.currentTimeMillis();
		//		returning = true;
		while(!finishedMission) {
			if(numberOfRooms < 4 || returning){
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
				//TODO TIRAR DAQUI
				returning = true;
				if(didntCrossLine()){
					if(numberOfRooms < 4)
						hugLeft();
					else
						hugRight();
				}else{
					Sound.beep();
					stopWalk=false;
					currentRoom--;
					ignoreRoom();
					Sound.beep();

					stopWalk=false;
				}
			}
		}
		updateWheelSpeeds(0, 0);
	}

	private void ignoreRoom() {
		int lSpeed = -720;
		int rSpeed = -720;

		updateWheelSpeeds(lSpeed, rSpeed);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		lSpeed = 720;
		rSpeed = -720;

		updateWheelSpeeds(lSpeed, rSpeed);
		try {
			Thread.sleep(650);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(currentRoom == 3) {

			long st = System.currentTimeMillis();

			while(System.currentTimeMillis() - st < 2000)
				hugRight();
			Sound.beep();
		} 


		//		while(didntCrossLine()){
		//			hugRight();
		//		}


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

			if(whiteTime > whiteStripeThreshold) {
				finishedMission = true;
			} else if(!white()){
				seenWhite=false;
				stopWalk = true;
				updateWheelSpeeds(0, 0);
				return false;
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
			if(angle > 0){
				rSpeed = 720 - (angle * 8);
			}else if(angle < 0){
				lSpeed = 720 - (angle * 8);
			}
			
			System.out.println("ANGLE: "+angle);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {e.printStackTrace();}
			
			updateWheelSpeeds(lSpeed, rSpeed);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {e.printStackTrace();}
			
			updateWheelSpeeds(0, 0);
			
			int range = 30;
			do{
				angle = candleAngle(-range,range);
				range += 10;
			}
			while(angle == null);
			turnTachos(angle);
			
			while(maxTemperature() < 150){ 
				if(candleAccurateIndex() < 3){
					updateWheelSpeeds(15, 75);
				}else{
					updateWheelSpeeds(75, 15);
				}
			}
			System.out.println(maxTemperature());
			updateWheelSpeeds(0, 0);
			Sound.beepSequence();
			turnFan(true);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			turnFan(false);
			while(true);
			
		}else {
			ignoredSearchedRoom();
		}
	}

	private void ignoredSearchedRoom() {
		int lSpeed = -720;
		int rSpeed = -720;

		updateWheelSpeeds(lSpeed, rSpeed);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(currentRoom == 3) {

			lSpeed = -720;
			rSpeed = 720;

			updateWheelSpeeds(lSpeed, rSpeed);
			try {
				Thread.sleep(650);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			long st = System.currentTimeMillis();

			while(System.currentTimeMillis() - st < 2500)
				hugLeft();
			Sound.beep();
		} else {
			lSpeed = 720;
			rSpeed = -720;

			if(currentRoom == 4) {
				lSpeed = -720;
				rSpeed = 720;
			}

			updateWheelSpeeds(lSpeed, rSpeed);
			try {
				Thread.sleep(650);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(currentRoom == 4) {
				long st = System.currentTimeMillis();

				while(System.currentTimeMillis() - st < 2500)
					hugLeft();
				Sound.beep();
			}

		}
	}

	private boolean white() {
		Color c = color.getColor();
		return c.getRed() > 200 && c.getGreen() > 200 && c.getBlue() > 200;
	}

	private void hugRight() {

		int lSpeed = 720;
		int rSpeed = 720;

		int rightDistance = sRight.getDistance();
		int leftDistance = sLeft.getDistance();
		int frontDistance = sFront.getDistance();
		//		LCD.drawString(""+leftDistance+" _____", 0, 0);
		//		LCD.drawString(""+frontDistance+" _____", 0, 2);
		//		LCD.drawString(""+rightDistance+" _____", 0, 4);

		//		saveNewVal(rightDistance);
		//		rightDistance = getSensorAverage();
		//		if(frontDistance < 27 && rightDistance < 27 && rightDistance > 20) {
		//			lSpeed = -720;

		if (frontDistance < 15) {
			//if we find an unexpected obstacle, such as the dog or a wall,
			//turn at full speed to the left.
			lSpeed = -720;			
		}else if (rightDistance > 15 && rightDistance < 20) {

			rSpeed /= 5-(20 - rightDistance);
			//				rSpeed = 250;
			//				rSpeed -= (rightDistance)*3;

			//				if(leftDistance < 30)
			//					rSpeed = -720;

		} else if(rightDistance >= 20) {
			//if there is no wall to the right, find one by sharply turning right
			rSpeed = 300;


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
		//		LCD.drawString(""+leftDistance+" _____", 0, 0);
		//		LCD.drawString(""+frontDistance+" _____", 0, 2);
		//		LCD.drawString(""+rightDistance+" _____", 0, 4);

		//		saveNewVal(rightDistance);
		//		rightDistance = getSensorAverage();
		//		if(frontDistance < 27 && rightDistance < 27 && rightDistance > 20) {
		//			lSpeed = -720;

		if (frontDistance < 15) {
			//if we find an unexpected obstacle, such as the dog or a wall,
			//turn at full speed to the left.
			rSpeed = -720;			
		}else if (leftDistance > 15 && leftDistance < 20) {

			rSpeed /= 5-(20 - leftDistance);
			//				rSpeed = 250;
			//				rSpeed -= (rightDistance)*3;

			//				if(leftDistance < 30)
			//					rSpeed = -720;

		} else if(leftDistance >= 20) {
			//if there is no wall to the right, find one by sharply turning right
			lSpeed = 350;

		} else if (leftDistance < 15) {
			//if we are too close to the right wall, turn slightly to the left
			rSpeed /= 15 - leftDistance;
		}
		updateWheelSpeeds(lSpeed, rSpeed);
	}
	
	private Integer candleAngle(){
		return candleAngle(-120, 120);
	}

	private Integer candleAngle(int startAngle, int finishAngle) {
		
		startAngle*=-1;
		finishAngle*=-1;
		
		radar.rotateTo(startAngle);
		
		int increment = 10;
		int max = 0;
		int angle = 0;
		
		int ambient = (getTemperatures(0)[8] + getTemperatures(1)[8]) / 2;
		
		boolean foundCandle = false;
		
		for(int i = startAngle ; i > finishAngle && !foundCandle ; i-=increment) {
			int[] temps = getTemperatures(0);
			int[] temps2 = getTemperatures(1);
			
			for(int t : temps)
				if(t > max) {
					max = t;
					angle = i*-1;
				}
			for(int t : temps2)
				if(t > max) {
					max = t;
					angle = i*-1;
				}

			radar.rotate(-increment);
			
			foundCandle = max > ambient+temperatureThreshold;
		}
		radar.rotateTo(0);
		
		if(foundCandle)
			return angle;
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
			}
		for(int i = 0; i < temps2.length - 1; i++)
			if(temps2[i] > max) {
				place = i;
			}
		return place;
	}
	
	private int maxTemperature(){
		int max = 0;
		int[] temps = getTemperatures(0);
		int[] temps2 = getTemperatures(1);
		
		for(int t : temps){
			if(t > max){
				max = t;
			}
		}
		for(int y : temps2){
			if(y > max){
				max = y;
			}
		}
		return max;
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

	private void turnTachos(int turn_degree){
		int leftTacho = mLeft.getTachoCount();
		int rightTacho = mRight.getTachoCount();
		if(turn_degree < 0){
			updateWheelSpeeds(-720, 720);
			while(mRight.getTachoCount() < rightTacho + (tacho_scale * -turn_degree));
		}else{
			updateWheelSpeeds(720, -720);	
			while(mLeft.getTachoCount() < leftTacho + (tacho_scale * turn_degree));
		}
		updateWheelSpeeds(0, 0);
	}

	public static void main(String[] args) {
		new SideSensors().mainLoop();
	}
}