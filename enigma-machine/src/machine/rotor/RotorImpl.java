package machine.rotor;

import java.util.HashMap;
import java.util.Map;

public class RotorImpl implements Rotor {

    private int position = 0;
    private final int size;
    private final int id;
    private int notchIndex;
    private final int[] forwardMap;
    private final int[] backwardMap;
    private final String alphabet;
    private final String rightSequence;
    private final String leftSequence;


    public RotorImpl(String alphabet,
                     String rightSequence,
                     String leftSequence,
                     //int position,
                     int notchPositionBase1,
                     int id) {

        this.id = id;

        //TODO validation for position
        //this.position = position;

        if (alphabet == null || rightSequence == null || leftSequence == null) {
            throw new IllegalArgumentException("Alphabet, rightSequence and leftSequence must not be null");
        }

        this.alphabet = alphabet.trim().toUpperCase();
        this.rightSequence = rightSequence.trim().toUpperCase();
        this.leftSequence = leftSequence.trim().toUpperCase();

        this.size = this.alphabet.length();

        if (size == 0) {
            throw new IllegalArgumentException("Alphabet must be non-empty");
        }

        if (this.rightSequence.length() != size || this.leftSequence.length() != size) {
            throw new IllegalArgumentException("Right/left sequences must have same length as alphabet");
        }

        if (notchPositionBase1 < 1 || notchPositionBase1 > size) {
            throw new IllegalArgumentException("Notch position must be between 1 and " + size);
        }

        this.notchIndex = notchPositionBase1 - 1;

//        validateUniqueChars(this.alphabet, "alphabet");
//        validatePermutation(this.alphabet, this.rightSequence, "rightSequence");
//        validatePermutation(this.alphabet, this.leftSequence, "leftSequence");

        this.forwardMap = new int[size];
        this.backwardMap = new int[size];

        buildMappings();
    }


    @Override
    public int encodeForward(int input) {
        //System.out.println(input);
        checkIndex(input);
        int shiftedIn = (input + position) % size;
        int mapped = forwardMap[shiftedIn];
        //System.out.println((mapped - position + size) % size);
        return (mapped - position + size) % size;
    }

    @Override
    public int encodeBackward(int input) {
        checkIndex(input);
        int shiftedIn = (input + position) % size;
        int mapped = backwardMap[shiftedIn];
        return (mapped - position + size) % size;
    }

    @Override
    public boolean advance() {
        int previousPosition = position;
        position = (position + 1) % size;
        notchIndex = Math.floorMod(notchIndex - 1, size);
       //TODO potential bugs
        //return previousPosition == notchIndex;
        return notchIndex == 0;
    }

//    @Override
//    public String getPosition() {
//        return "" + this.position;
//    }

    @Override
    public int getPosition() {
        return this.position;
    }



    @Override
    public int getNotchIndex() {
        return this.notchIndex;
    }


    private void buildMappings() {
        Map<Character, Integer> rightIndexByChar = new HashMap<>();
        Map<Character, Integer> leftIndexByChar = new HashMap<>();

        for (int i = 0; i < size; i++) {
            rightIndexByChar.put(rightSequence.charAt(i), i);
            leftIndexByChar.put(leftSequence.charAt(i), i);
        }

        for (int i = 0; i < size; i++) {
            char ch = alphabet.charAt(i);

            Integer rightIndex = rightIndexByChar.get(ch);
            Integer leftIndex = leftIndexByChar.get(ch);

            if (rightIndex == null || leftIndex == null) {
                throw new IllegalStateException(
                        "Character '" + ch + "' must appear exactly once in both right and left sequences");
            }

            forwardMap[rightIndex] = leftIndex;
            backwardMap[leftIndex] = rightIndex;
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Index out of range: " + index);
        }
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
        this.notchIndex = Math.floorMod(notchIndex - position, size);//TODO potential bug
    }

    @Override
    public int getId() {
        return this.id;
    }

    /// new changes
    public String getAlphabet() {
        return alphabet;
    }

    @Override
    public String getRightSequence() {
        return rightSequence;
    }

    public String getLeftSequence() {
        return leftSequence;
    }

    /**
     * The original notch position in base-1 *when the rotor is at position 0*.
     */
    public int getOriginalNotchBase1() {
        // בעת יצירת הרוטור: notchIndex = notchBase1 - 1, position = 0
        // אז פה אנחנו מניחים שהאובייקט המשמש כתבנית (prototype) נשאר בסטייט ההתחלתי.
        return notchIndex + 1;
    }


//TODO this is validation functions

//    private void validateUniqueChars(String s, String name) {
//        Set<Character> seen = new HashSet<>();
//        for (char c : s.toCharArray()) {
//            if (!seen.add(c)) {
//                throw new IllegalArgumentException("Duplicate character '" + c + "' in " + name);
//            }
//        }
//    }
//    private void validatePermutation(String alphabet, String sequence, String name) {
//        Set<Character> expected = new HashSet<>();
//        for (char c : alphabet.toCharArray()) {
//            expected.add(c);
//        }
//
//        Set<Character> seen = new HashSet<>();
//        for (char c : sequence.toCharArray()) {
//            if (!expected.contains(c)) {
//                throw new IllegalArgumentException(
//                        "Character '" + c + "' in " + name + " is not part of alphabet");
//            }
//            if (!seen.add(c)) {
//                throw new IllegalArgumentException(
//                        "Character '" + c + "' appears more than once in " + name);
//            }
//        }
//
//        if (seen.size() != expected.size()) {
//            throw new IllegalArgumentException(
//                    name + " is not a full permutation of the alphabet");
//        }
//    }

}
