
import lejos.nxt.Button;


/**
 * An ugly thread demo with lots of static-ity.
 *
 * @author Radoslav Husar
 */
public class ThreadLogger {

    private static boolean doStop = false;

    public static void main(String[] args) {
        System.out.println("Thread logger started.");

        ThreadA threadAInstance = new ThreadA();
        ThreadB threadBInstance = new ThreadB();

        threadAInstance.setDaemon(true);
        threadAInstance.start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            // Ok.
        }

        threadBInstance.setDaemon(true);
        threadBInstance.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            // Ok.
        }

        doStop = true;

        System.out.println("Press a button to exit.");
        Button.waitForPress();
    }

    static class ThreadA extends Thread {

        @Override
        public void run() {
            while (!doStop) {
                System.out.println("Hello from Thread A.");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    // Ok.
                }
            }
        }
    }

    static class ThreadB extends Thread {

        @Override
        public void run() {
            while (!doStop) {
                System.out.println("Hello from Thread B.");

                try {
                    Thread.sleep(750);
                } catch (InterruptedException ex) {
                    // Ok.
                }
            }
        }
    }
}
