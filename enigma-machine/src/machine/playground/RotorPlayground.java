package machine.playground;

import machine.rotor.Rotor;
import machine.rotor.RotorImpl;

public class RotorPlayground {

    public static void main(String[] args) {
        String alphabet = "ABCD";
        String rightSeq = "ABCD";
        String leftSeq = "CDBA";
        int notchBase1 = 2;

        Rotor rotor = new RotorImpl(alphabet, rightSeq, leftSeq, notchBase1, 0);

        // בדיקה 1: בלי סיבוב
        int inputIndex = 0;
        int forward = rotor.encodeForward(inputIndex);
        System.out.println("encodeForward(0) = " + forward);

        int backward = rotor.encodeBackward(forward);
        System.out.println("encodeBackward(" + forward + ") = " + backward);

        // בדיקה 2: אחרי סיבוב אחד
        boolean carry = rotor.advance();
        System.out.println("after advance(), carry = " + carry);
        int forwardAfterStep = rotor.encodeForward(0);
        System.out.println("encodeForward(0) after step = " + forwardAfterStep);
    }
}
