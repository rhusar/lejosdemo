
import lejos.nxt.*;
import lejos.nxt.addon.RCXMotor;

/**
 * Just playing around with robot to follow the sound.
 */
public class SoundSeeker {

    public static final int SAMPLES_TO_COMPARE = 7;
    public static final int MIN_SAMPLE_COUNT_TO_SWITCH = 4;

    public static void main(String[] args) {
        System.out.println("SoundSeeker Started.");

        SoundSensor soundSensor = new SoundSensor(SensorPort.S1);
        soundSensor.setDBA(false);

        RCXMotor rightMotor = new RCXMotor(MotorPort.A);
        RCXMotor leftMotor = new RCXMotor(MotorPort.B);

        rightMotor.setPower(90);
        leftMotor.setPower(90);

        rightMotor.forward();
        leftMotor.backward();

        int[] previousSamples = new int[SAMPLES_TO_COMPARE];
        int j, i;
        int nosierSamples = 0;
        int currentSample;
        int maxAverageNoise = 0;
        int currentAverageNoise = 0;

        for (i = 0; i < 200; i++) {
            currentSample = soundSensor.readValue();
            System.out.print("SND:" + currentSample + ";");

            nosierSamples = 0;
            currentAverageNoise = 0;
            for (j = 0; j < SAMPLES_TO_COMPARE; j++) {
                if (previousSamples[j] >= currentSample) {
                    nosierSamples++;
                    currentAverageNoise += previousSamples[j];
                }
            }
            currentAverageNoise /= SAMPLES_TO_COMPARE;

            System.out.print("MRT: " + nosierSamples + "");

            // This needs to be improved a lot.
            //wasMoreThan == LAST_VALUES_COMPARE &&
            if ((currentAverageNoise >= maxAverageNoise - 1)) {
                // go forward
                rightMotor.forward();
                leftMotor.forward();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    // Do nothing.
                }

                rightMotor.forward();
                leftMotor.backward();

            } else if (nosierSamples > MIN_SAMPLE_COUNT_TO_SWITCH) {
                // keep rotating this way!
            } else {
                // change rotating way
                if (rightMotor.isForward()) {
                    rightMotor.backward();
                } else {
                    rightMotor.forward();
                }

                if (leftMotor.isForward()) {
                    leftMotor.backward();
                } else {
                    leftMotor.forward();
                }
            }

            System.out.println();
            previousSamples[i % SAMPLES_TO_COMPARE] = currentSample;
            maxAverageNoise = Math.max(currentAverageNoise, maxAverageNoise);

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
