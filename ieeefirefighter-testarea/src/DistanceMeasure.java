import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class DistanceMeasure {
	private UltrasonicSensor ultrasonicSensor;
	
	public static void main(String[] args) {
		new DistanceMeasure();
	}
	
	public DistanceMeasure(){
		ultrasonicSensor=new UltrasonicSensor(SensorPort.S1);
		
		while(true){
			System.out.println("Distance: "+ultrasonicSensor.getDistance());
		}
	}
}
