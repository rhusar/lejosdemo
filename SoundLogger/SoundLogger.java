
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

        SoundSensor soundSensor = new SoundSensor(SensorPort.S1);
        soundSensor.setDBA(false);

        for (int i = 0; i < 200; i++) {
            System.out.println("DBs: " + soundSensor.readValue());

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
