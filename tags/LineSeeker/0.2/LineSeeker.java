/* Copyright 2010 Radoslav, Jiri, Miha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.SimpleNavigator;
import lejos.robotics.navigation.TachoPilot;

/**
 *
 * @author Radoslav Husar
 */
public class LineSeeker {

    // Contants
    public static final float BW_RATIO = 0.90F; // percentage of white
    public static final int BW_SAMPLES = 2; // count of samples to be used to compare
    public static final int BW_INTERVAL = 5; // in ms
    public static final int DIST_INTERVAL = 50; // distance scanning interval in ms
    public static final int DIST_TARGET = 15; // satisfactory distance to the ball
    public static final int SCAN_RIGHT_MOD = -1; // modificator; -1 right, +1 left
    public static final int SCAN_SMALL_DEG = 20;
    public static final int SCAN_FULL_DEG = 90;
    // Shared variables
    private static boolean onblack = false;
    private static int distance = 255;
    //private static SoundController sound;

    public static void main(String[] args) throws Exception {
        System.out.println("Program started.");

        Pilot pilot = new TachoPilot((float) 5.6F, 9.4F, Motor.A, Motor.B, false);
        SimpleNavigator sn = new SimpleNavigator(pilot);

        sn.setMoveSpeed(10F);
        sn.setTurnSpeed(20F);

        // Light scanner thread
        LightScannerThread scannerThread = new LightScannerThread();
        scannerThread.setDaemon(true);
        scannerThread.start();

        // UltraSonic/distance scanner thread
        DistanceScannerThread distanceThread = new DistanceScannerThread();
        distanceThread.setDaemon(true);
        distanceThread.start();

//        TODO: Make thread work.
//        Start sound thread
//        sound = new SoundController();
//        sound.setDaemon(true);
//        sound.start();

        // Wait to calibrate (startup 500, calibration 1000)
        Thread.sleep(1500);

        // Find first black line
        sn.forward();

        while (!onblack) {
            // Keep forwarding until you reach the line.
        }

        // Initialization phase DONE!

        // Variables for optimizing search
        boolean lastSearchRight = false;
        boolean fullscan = false;

        while (true) {
            while (onblack) {
                // Keep forwarding! You are doing good

                // FIXME: no need to call forward() so many times again
                sn.forward();

                // TODO: add steer() logic

                // Is the read ball in sight? Then just make the noise...
                if (distance < DIST_TARGET) {
                    // I just reached the target distance!
                    System.out.println("I just found the ball.");

                    // Make some annoying sound
                    for (int i = 4; i < 8; i++) {
                        Sound.playTone(i * 100, 200);

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            // OK
                        }
                    }


                    // TODO: implement logic if the ball is *really* found
                }

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
     * This thread will wait for the sensor to start (0.5 second), then
     * calibrate (first 1 second) and then will only change 
     * boolean value whether sensor/robot is on black line or not.
     *
     * @author Radoslav Husar
     * @version 2010-04-19
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

            // inital value is we are on white

            for (int j = 0; j < BW_SAMPLES; j++) {
                previousValues[j] = (int) lightSamplesAvg;
            }

            while (true) {

                currentValue = ls.readNormalizedValue();

                // Calculate current average
                previousAvg = 0;
                for (int j : previousValues) {
                    previousAvg += j;
                }
                previousAvg /= BW_SAMPLES;

                // Uncomment to show the log in format: previous average/current value/sample average
                // System.out.println(previousAvg + "/" + currentValue + "/" + lightSamplesAvg);

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

    /**
     * This thread will be reporting distance from the UltraSonic sensor
     * connected to the sensor port S2 and save it in a shared variable for
     * the main class to access and work with it.
     *
     * @author Radoslav Husar
     * @version 2010-04-20
     */
    static class DistanceScannerThread extends Thread {

        @Override
        public void run() {

            // Get Ultrasonic Sensor at sensor port S2
            UltrasonicSensor ultraSonicSensor = new UltrasonicSensor(SensorPort.S2);

            while (true) {
                distance = ultraSonicSensor.getDistance();

                try {
                    Thread.sleep(DIST_INTERVAL);
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
