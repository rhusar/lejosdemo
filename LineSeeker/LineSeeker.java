
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

    // Contants
    public static final float BW_RATIO = 0.90F;
    public static final int BW_SAMPLES = 2;
    public static final int BW_INTERVAL = 10; // in ms
    public static final int SCAN_RIGHT_MOD = -1;
    public static final int SCAN_SMALL_DEG = 10;
    public static final int SCAN_FULL_DEG = 90;
    // Shared variables
    private static SimpleNavigator sn;
    private static boolean onblack = false;
    //private static SoundController sound;

    public static void main(String[] args) throws Exception {
        System.out.println("Program started.");

        Pilot pilot = new TachoPilot((float) 5.6F, 17, Motor.A, Motor.B, false);
        sn = new SimpleNavigator(pilot);
        sn.setMoveSpeed(5F);

        // Start sound thread
//        sound = new SoundController();
//        sound.setDaemon(true);
//        sound.start();

        LightScannerThread scannerThread = new LightScannerThread();
        scannerThread.setDaemon(true);
        scannerThread.start();

        // Wait to calibrate (startup 500, calibration 1000)
        Thread.sleep(1500);

        // Find first black line
        sn.forward();

        while (!onblack) {
            // Keep forwarding until you reach the line.
        }

        sn.setMoveSpeed(10F);
        sn.setTurnSpeed(10F);

        // Initialization DONE!

        // Variables for optimizing where to search
        boolean lastSearchRight = false;
        boolean fullscan = false;

        while (true) {
            while (onblack) {
                // Keep forwarding! You are doing good
                sn.forward();
            }

            // I am not on black, what now?

            // Keep turning right for few degrees, until black or not moving.
            sn.rotate((lastSearchRight ? 1 : SCAN_RIGHT_MOD) * (fullscan ? SCAN_FULL_DEG : SCAN_SMALL_DEG), true);
            lastSearchRight = !lastSearchRight;

            while (sn.isMoving() && !onblack) {
                // Keep scanning...
            }

            if (onblack) {
                // Yes, I found black! Just stop rotating now.
                sn.stop();

                // Fullscanning turn off.
                fullscan = false;

                // ...and keep forwarding to beyond!
                sn.forward();
            } else {
                // I stopped moving, but I am not on black still.... :(

                // Let me try the other way.
                sn.rotate((lastSearchRight ? 1 : SCAN_RIGHT_MOD) * (fullscan ? SCAN_FULL_DEG : SCAN_SMALL_DEG) * 2, true);

                while (sn.isMoving() && !onblack) {
                    // Keep moving...
                }

                if (onblack) {
                    // Yes, I found black! Just stop now
                    sn.stop();

                    // Fullscanning turn off.
                    fullscan = false;

                    // ...and keep forwarding to beyond!
                    sn.forward();
                } else {
                    // I stopped moving, but I am not on black still and this was a second attempt.

                    // Was this a full scan? If no, turn it on for next progress.
                    if (!fullscan) {
                        fullscan = true;
                        System.out.println("Fullscan ON");
                    } else {
                        // I failed even after full scan
                        System.out.println("ERROR! Path not found.");

                        while (true != false) {
                            // Wait until device reset.
                        }
                    }
                }
            }

            // Any weird thing that just wont happen to make Java happy.
            if (sn.getAngle() == -123451) {
                break;
            }
        }

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }

    /**
     * This thread will calibrate (first 1 second) and then will only change 
     * boolean value whether sensor/robot is on black line or not.
     */
    static class LightScannerThread extends Thread {

        @Override
        public void run() {

            LightSensor ls = new LightSensor(SensorPort.S1, true);
            int high = ls.readValue();
            ls.setHigh(high);

            float lightSamplesAvg = 0;

            // This sleep is necessary for the light to start up and get a
            // proper default sample average to compare to.
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // OK
            }

            // Calibrate from 10 samples on white
            for (int i = 0; i < 10; i++) {
                lightSamplesAvg += ls.getNormalizedLightValue();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // OK
                }
            }

            // Calculate average
            lightSamplesAvg /= 10;
            System.out.println("WhiteLightAvg: " + lightSamplesAvg);

            // Start processing values

            int i = 0, currentValue = 0;
            float previousAvg = 0;
            int[] previousValues = new int[BW_SAMPLES];

            while (true) {

                currentValue = ls.readNormalizedValue();

                // Calculate current average
                previousAvg = 0;
                for (int j : previousValues) {
                    previousAvg += j;
                }
                previousAvg /= BW_SAMPLES;

                //System.out.println(previousAvg + "/" + currentValue + "/" + lightSamplesAvg);

                // Am I on black again?
                if (previousAvg < (lightSamplesAvg * BW_RATIO)) {

                    // I am on black
                    if (!onblack) {
                        System.out.println("OFF->ON");
                    }
                    onblack = true;
                } else {

                    // I am not on black!!!
                    if (onblack) {
                        System.out.println("ON->OFF");
                    }

                    onblack = false;
                }

                previousValues[i] = currentValue;
                i++;

                if (i == BW_SAMPLES) {
                    // Reset counter
                    i = 0;
                }

                try {
                    Thread.sleep(BW_INTERVAL);
                } catch (InterruptedException ex) {
                    // OK
                }
            }
        }
    }
//    static class SoundController extends Thread {
//
//        @Override
//        public void run() {
//
//            while (true) {
//                try {
//                    wait();
//                } catch (InterruptedException ex) {
//                    // Nothing.
//                }
//
//                for (int i = 3; i < 6; i++) {
//                    Sound.playTone(i * 100, 100);
//                    try {
//                        sleep(100);
//                    } catch (InterruptedException ex) {
//                        // n.
//                    }
//                }
//            }
//
//        }
//    }
}
