
import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

/**
 * This class logs the distance from the attached UtraSonic distance sensor
 * connected to sensor port S2.
 * 
 * @author Radoslav Husar
 * @version 2010-04-19
 */
public class DistanceLogger {

    public static void main(String[] args) {
        System.out.println(" Sensor logger started.");

        // Get Ultrasonic Sensor at sensor port S2
        UltrasonicSensor ultraSonicSensor = new UltrasonicSensor(SensorPort.S2);

        // Try getting some device data
        System.out.println("ProductID: " + ultraSonicSensor.getProductID());
        System.out.println("SensorType: " + ultraSonicSensor.getSensorType());
        System.out.println("Version: " + ultraSonicSensor.getVersion());
        System.out.println("Units: " + ultraSonicSensor.getUnits());

        // Let me give you some time to read the output
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            // OK
        }

        // Get and print the data obtained
        for (int i = 0; i < 100; i++) {

            System.out.println("Distance: " + ultraSonicSensor.getDistance());

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                // OK
            }
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }
}
