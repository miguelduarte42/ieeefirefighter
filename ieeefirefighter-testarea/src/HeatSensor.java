import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

/**
 * Class to read of the heat sensor
 * 
 * @author SOFT Robotics
 * @author Fredrik Löfgren
 * @author Sebastian Gustafsson
 * */
public class HeatSensor extends I2CSensor {

   static final byte ADDRESS = (byte) 0x68;

    // Data buffer for the IR values
   private byte[] data = new byte[1];

   public HeatSensor(I2CPort port, byte adress) {
      super(port, adress, I2CPort.STANDARD_MODE, I2CSensor.TYPE_LOWSPEED);
   }

   public HeatSensor(I2CPort port) {
      this(port, ADDRESS);
   }

   public void testSensor() {
      int ret;

      if ((ret = this.getData(0, data, 1)) < 0)// Can fetch data?
         throw new RuntimeException("Heatr: " + ret);
      else if (data[0] <= 0) // Correct data returned?
         throw new RuntimeException("Heatd: " + data[0]);
   }

   /** Return the most heated segment that the sensor sees just now */
   public int getHeat() {
      int max = 0;
      for (int pos = 0; pos < 8; pos++) {
         this.getData(pos + 0x02, data, 1); // read pixel command
         if (data[0] > max)
            max = data[0] & 0xFF;
      }
      return max;
   }

   /** Read internal temperature */
   public byte ReadAmbient() {
      this.getData(0x01, data, 1); // the i2c read command
      return data[0];
   }

   /** Read one pixel */
   public byte ReadPixel(int pos) {
      this.getData(pos + 0x02, data, 1); // read pixel command
      return data[0];
   }
}