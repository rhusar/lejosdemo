
import lejos.nxt.*;

/**
 * @author Radoslav Husar
 */
public class LightLogger {

    public static void main(String[] args) {
        System.out.println("Light Sensor logger started.");

        // Get light sensor at sensor port 1
        LightSensor lightSensor = new LightSensor(SensorPort.S1);

        // Turn the active light on
        lightSensor.setFloodlight(true);

        System.out.println("Floodlight ON");

        for (int i = 0; i < 200; i++) {

            System.out.println("Ligh: " + lightSensor.readValue());

            /*
            // Uncomment to try with Floodlight off.
            if (i == 100) {
            System.out.println("Floodlight OFF");
            lightSensor.setFloodlight(false);
            }
             */

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                // Do nothing.
            }
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }
}
