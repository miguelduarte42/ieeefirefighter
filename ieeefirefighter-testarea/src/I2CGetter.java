import lejos.nxt.I2CSensor;
import lejos.nxt.SensorPort;

public class I2CGetter {
	private final byte TPA81_ADDR = (byte) 0x68;
	private I2CSensor tpa81;
	private SensorPort port=SensorPort.S2;

	public static void main(String[] args) {
		new I2CGetter();
	}

	public I2CGetter() {
		HeatSensor heatSensor=new HeatSensor(port);
		while (true) {
//			port.setPowerType(2);
//			tpa81 = new I2CSensor(port, TPA81_ADDR,
//					SensorPort.HIGH_SPEED, SensorPort.TYPE_LOWSPEED);
//
//			byte[] response = new byte[20];
//			int i2c_Response;
//
//			i2c_Response = tpa81.getData(0x0, response, 10);
			
			
			System.out.println("Response: " + heatSensor.getHeat());
			
			// for (int i = 0; i < response.length; i++) {
			// System.out.println(new Byte(response[i]).intValue());
			// }

			// for (int i = 1; i < buffer.length; i++) {
			// mean += new Byte(buffer[i]).intValue();
			// }
			// mean /= (buffer.length - 1);
			//
			// System.out.println("Mean: " + mean + " Amb: "
			// + new Byte(buffer[0]).intValue());
		}
	}
}
