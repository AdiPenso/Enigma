package machine.playground;

import machine.machine.MessageProcessor;
import machine.rotor.Rotor;
import machine.rotor.RotorImpl;
import machine.code.Code;
import machine.code.CodeImpl;
import machine.keyboard.Keyboard;
import machine.keyboard.KeyboardImpl;
import machine.machine.Machine;
import machine.machine.MachineImpl;
import machine.reflector.Reflector;
import machine.reflector.ReflectorImpl;
//import machine.rotor.RotorPosition;

import java.util.List;

public class RotorPlayground {

    public static void main(String[] args) {
//        String alphabet = "ABCD";
//        String rightSeq = "ABCD";
//        String leftSeq = "CDBA";
//        int notchBase1 = 2;
//
//        Rotor rotor = new RotorImpl(alphabet, rightSeq, leftSeq, notchBase1, 0);
//
//        // בדיקה 1: בלי סיבוב
//        int inputIndex = 0;
//        int forward = rotor.encodeForward(inputIndex);
//        System.out.println("encodeForward(0) = " + forward);
//
//        int backward = rotor.encodeBackward(forward);
//        System.out.println("encodeBackward(" + forward + ") = " + backward);
//
//        // בדיקה 2: אחרי סיבוב אחד
//        boolean carry = rotor.advance();
//        System.out.println("after advance(), carry = " + carry);
//        int forwardAfterStep = rotor.encodeForward(0);
//        System.out.println("encodeForward(0) after step = " + forwardAfterStep);
            // 1. האלפבית – לפי <ABC> ABCDEF
            String alphabet = "ABCDEF";

            // 2. Keyboard – גנרי
            Keyboard keyboard = new KeyboardImpl(alphabet);

            // 3. רוטורים לפי ה-XML (right תמיד ABCDEF)
            String rightSeq = "ABCDEF";

            // Rotor 1: notch="4", left: F E D C B A
            Rotor rotor1 = new RotorImpl(
                    alphabet,
                    rightSeq,
                    "FEDCBA",
                    2,
                    2,
                    1
            );
            //RotorPosition pos1 = new RotorPosition(rotor1);

            // Rotor 2: notch="1", left: E B D F C A
            Rotor rotor2 = new RotorImpl(
                    alphabet,
                    rightSeq,
                    "EBDFCA",
                    2,
                    5,
                    2
            );
            //RotorPosition pos2 = new RotorPosition(rotor2);
        Rotor rotor3 = new RotorImpl(
                alphabet,
                rightSeq,
                "AFBECD",  // leftSeq3
                2,
                6,
                3
        );
            /*
             * 4. סדר הרוטורים ב-List:
             * MachineImpl.forward עושה:
             *   for (i=0; i< size; i++) rotors[i].encodeForward(...)
             * וב-backward הולך מהסוף להתחלה.
             *
             * לכן:
             * index 0 = הרוטור הימני (קרוב ל-Keyboard)
             * index last = השמאלי (קרוב ל-Reflector)
             *
             * נניח שהקונפיגורציה שנבחרה היא "1-3-2" מימין לשמאל:
             * rightmost: Rotor 1
             * middle   : Rotor 3
             * leftmost : Rotor 2
             */
            List<Rotor> rotorsOrder = List.of(
                    rotor1, // rightmost
                    rotor2, // middle
                    rotor3  // leftmost
            );

            // 5. Reflector I לפי ה-XML
            int[] reflectorMapping = buildReflectorI();
            Reflector reflector = new ReflectorImpl(0,reflectorMapping);

            // 6. Code = רשימת רוטורים + רפלקטור
            Code code = new CodeImpl(rotorsOrder, reflector);

            // 7. מכונה
            Machine machine = new MachineImpl(keyboard);
            machine.setCode(code);

        MessageProcessor processor = new MessageProcessor(machine);

        String plain = "ADEBCFEEDA";
        String cipher = processor.process(plain);

        System.out.println("Plain  : " + plain);
        System.out.println("Cipher : " + cipher);
            // 8. נצפין תו אחד – נגיד 'A'
//            char inputChar = 'A';
//            char outputChar = machine.processChar(inputChar);
//
//            System.out.println("Input char  : " + inputChar);
//            System.out.println("Output char : " + outputChar);
        }

        private static int[] buildReflectorI() {
            int[] map = new int[6];
            // 1<->4 => 0<->3
            map[0] = 3;
            map[3] = 0;
            // 2<->5 => 1<->4
            map[1] = 4;
            map[4] = 1;
            // 3<->6 => 2<->5
            map[2] = 5;
            map[5] = 2;
            return map;
    }
}
