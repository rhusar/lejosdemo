
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
    public static final float BW_RATIO = 0.85F;
    public static final int MOVE_RIGHT_MOD = -1;
    // Shared variables
    private static SimpleNavigator sn;
    private static boolean onblack = false;

    public static void main(String[] args) throws Exception {
        System.out.println("Program started.");

        Pilot pilot = new TachoPilot((float) 5.6F, 17, Motor.A, Motor.B, false);
        sn = new SimpleNavigator(pilot);
        sn.setMoveSpeed(5F);

        LightScannerThread scannerThread = new LightScannerThread();
        scannerThread.setDaemon(true);
        scannerThread.start();

        // Wait to calibrate
        Thread.sleep(1500);

        // Find first black line
        sn.forward();

        while (!onblack) {
            // Keep forwarding until you reach the line.
        }

        sn.setMoveSpeed(5F);
        sn.setTurnSpeed(15F);

        // Initialization DONE!

        // Variables for optimizing where to search
        boolean lastSearchRight = false;

        while (true) {
            while (onblack) {
                // Keep forwarding! You are doing good
                sn.forward();
            }

            // I am not on black, what now?

            // Keep turning right for few degrees, until black or not moving.
            sn.rotate((lastSearchRight ? 1 : MOVE_RIGHT_MOD) * 15, true);
            lastSearchRight = !lastSearchRight;

            while (sn.isMoving() && !onblack) {
                // Keep scanning...
            }

            if (onblack) {
                // Yes, I found black! Just stop rotating now.
                sn.stop();

                // ...and keep forwarding to beyond!
                sn.forward();
            } else {
                // I stopped moving, but I am not on black still.... :(

                // Let me try the other way.
                sn.rotate((lastSearchRight ? 1 : MOVE_RIGHT_MOD) * 30, true);

                while (sn.isMoving() && !onblack) {
                    // Keep moving...
                }

                if (onblack) {
                    // Yes, I found black! Just stop now
                    sn.stop();

                    // ...and keep forwarding to beyond!
                    sn.forward();
                } else {
                    // I stopped moving, but I am not on black still and this was a second attempt.
                    // I failed...

                    System.out.println("ERROR! Path not found (or more than 15 degrees turn found).");

                    while (true != false) {
                        // Wait until device reset.
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
                    if (((prevValue + nvalue) / 2) < lightSamplesAvg * BW_RATIO) {

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
                }

                prevValue = nvalue;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    // Ok.
                }

            }
        }
    }
}
