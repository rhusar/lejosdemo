
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

    public static void main(String[] args) {
        System.out.println("Program started.");

        Pilot pilot = new TachoPilot((float) 5.6F, 17, Motor.A, Motor.B, false);
        sn = new SimpleNavigator(pilot);
        sn.setMoveSpeed(5F);

        LightScannerThread scannerThread = new LightScannerThread();
        scannerThread.setDaemon(true);
        scannerThread.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            // Ok.
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }

    static class LightScannerThread extends Thread {

        int lightSamplesAvg = 0;

        @Override
        public void run() {

            LightSensor ls = new LightSensor(SensorPort.S1, true);
            int high = ls.readValue();
            ls.setHigh(high);

            int k = 0, nvalue = 0;
            boolean initializingPhase = true;

            while (true) {

                nvalue = ls.readNormalizedValue();

                if (k <= 10) {
                    k++;
                    lightSamplesAvg += nvalue;
                } else if (k == 11) {
                    k++;
                    lightSamplesAvg /= 10;
                    System.out.println("ligh avg: " + lightSamplesAvg);

                    sn.forward();
                }

                // Am I on black again?
                if (nvalue < lightSamplesAvg * 0.9) {
                    // I am on black!

                    if (initializingPhase == true) {
                        // move to middle and rotate 9 degrees right
                        initializingPhase = false;

                        sn.rotate(15);
                    } else {
                        // im good, keep moving!

                        sn.forward();
                    }


                } else {
                    // I am not on black!!!

                    if (initializingPhase) {
                        // nothing, keep forward
                    } else {
                        sn.stop();
                        sn.rotate(15);
                    }

                }

                //System.out.println(ls.readNormalizedValue());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // Ok.
                }
            }
        }
    }
}
