
import lejos.nxt.*;
/**
 * This simple class is logging every 100 ms amount od DBs read from the sound
 * sensor connected to sensor port 1.
 *
 * @author Radoslav Husar
 */
public class SoundLogger {

    public static void main(String[] args) {
        System.out.println("Sound test started.");

        for (int i = 0; i < 100; i++) {
            SoundSensor soundSensor = new SoundSensor(SensorPort.S1);
            soundSensor.setDBA(false);
            System.out.println("DB read: " + soundSensor.readValue());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // Do nothing.
            }
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }
}
