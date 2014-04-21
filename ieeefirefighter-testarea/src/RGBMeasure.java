import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;

public class RGBMeasure {
	private ColorSensor colorSensor;

	public static void main(String[] args) {
		new RGBMeasure();
	}

	public RGBMeasure() {
		colorSensor = new ColorSensor(SensorPort.S1);

		String red;
		String green;
		String blue;

		while (true) {
			if (colorSensor.getColor().getRed() < 100) {
				red = "0" + colorSensor.getColor().getRed();
			} else {
				red = "" + colorSensor.getColor().getRed();
			}

			if (colorSensor.getColor().getGreen() < 100) {
				green = "0" + colorSensor.getColor().getGreen();
			} else {
				green = "" + colorSensor.getColor().getGreen();
			}

			if (colorSensor.getColor().getBlue() < 100) {
				blue = "0" + colorSensor.getColor().getBlue();
			} else {
				blue = "" + colorSensor.getColor().getBlue();
			}
			
			System.out.println("R-" + red + " G-" + green + " B-" + blue);
		}
	}
}
