
import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.SimpleNavigator;
import lejos.robotics.navigation.TachoPilot;

/**
 *
 * @author Radoslav Husar
 */
public class LineSeeker {

    private static SimpleNavigator sn;
    private static boolean onblack = false;

    public static void main(String[] args) {
        System.out.println("Program started.");

        Pilot pilot = new TachoPilot((float) 5.6F, 17, Motor.A, Motor.B, false);
        sn = new SimpleNavigator(pilot);
        sn.setMoveSpeed(5F);

        LightScannerThread scannerThread = new LightScannerThread();
        scannerThread.setDaemon(true);
        scannerThread.start();

        for (int i = 0; i < 100; i++) {

            System.out.println("OnBlack? " + (onblack ? "YES" : "NO"));

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                // Ok.
            }
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }

    /**
     * This thread will calibrate (first second) and then will only change bool
     * value whether I am on back or not.
     */
    static class LightScannerThread extends Thread {

        int lightSamplesAvg = 0;
        boolean calibrating = true;

        @Override
        public void run() {

            LightSensor ls = new LightSensor(SensorPort.S1, true);
            int high = ls.readValue();
            ls.setHigh(high);

            int k = 0, nvalue = 0, prevValue = 0;

            while (true) {

                nvalue = ls.readNormalizedValue();

                if (calibrating) {
                    if (k <= 10) {
                        k++;
                        lightSamplesAvg += nvalue;
                    } else if (k == 11) {
                        k++;
                        lightSamplesAvg /= 10;
                        System.out.println("Light AVG: " + lightSamplesAvg);

                        calibrating = false;
                    }
                } else {
                    // Am I on black again?
                    if (((prevValue + nvalue) / 2) < lightSamplesAvg * 0.9) {
                        // I am on black
                        onblack = true;
                    } else {
                        // I am not on black!!!
                        onblack = false;
                    }
                }
                
                prevValue = nvalue;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // Ok.
                }

            }
        }
    }
}
