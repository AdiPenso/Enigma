package patmal.course.enigma.machine.rotor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RotorImpl implements Rotor, Serializable {
    private static final long serialVersionUID = 1L;
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
                     int notchPositionBase1,
                     int id) {
        this.id = id;

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
        this.forwardMap = new int[size];
        this.backwardMap = new int[size];

        buildMappings();
    }

    @Override
    public int encodeForward(int input) {
        checkIndex(input);
        int shiftedIn = (input + position) % size;
        int mapped = forwardMap[shiftedIn];

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
        position = (position + 1) % size;
        notchIndex = Math.floorMod(notchIndex - 1, size);
        return notchIndex == 0;
    }

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
        this.notchIndex = Math.floorMod(notchIndex - position, size);
    }

    @Override
    public int getId() {
        return this.id;
    }

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

    public int getOriginalNotchBase1() {
        return notchIndex + 1;
    }
}
