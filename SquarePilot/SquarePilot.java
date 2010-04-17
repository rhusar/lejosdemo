
import lejos.nxt.*;
import lejos.robotics.navigation.*;

/**
 * Vehicle moves in a square.
 * 
 * @author Radoslav Husar
 */
public class SquarePilot {

    public static final int SIDE_LENGTH = 50;

    public static void main(String[] args) {

        Pilot pilot = new TachoPilot((float) 5.6F, 17, Motor.A, Motor.B, false);

        SimpleNavigator sn = new SimpleNavigator(pilot);

        sn.goTo(0, SIDE_LENGTH);
        sn.goTo(SIDE_LENGTH, SIDE_LENGTH);
        sn.goTo(SIDE_LENGTH, 0);
        sn.goTo(0, 0);
    }
}
