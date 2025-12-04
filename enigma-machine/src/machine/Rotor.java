package machine;

import java.util.List;

public class Rotor {

    private final int id;
    private final int notch;
    private final int[] forwardMap;
    private final int[] backwardMap;
    private final Alphabet alphabet;

    private int position = 0;

    public Rotor(int id,
                 int notchBase1,
                 List<Character> rightLetters,
                 List<Character> leftLetters,
                 Alphabet alphabet) {

        this.id = id;
        this.alphabet = alphabet;
        this.notch = notchBase1 - 1;

        int n = alphabet.size();
        this.forwardMap = new int[n];
        this.backwardMap = new int[n];

        for (int i = 0; i < n; i++) {
            int fromIndex = alphabet.indexOf(rightLetters.get(i));
            int toIndex = alphabet.indexOf(leftLetters.get(i));

            forwardMap[fromIndex] = toIndex;
            backwardMap[toIndex] = fromIndex;
        }
    }

    public void step() {
        position = (position + 1) % alphabet.size();
    }

    public boolean isAtNotch() {
        return position == notch;
    }

    public void setPosition(int position) {
        if (position < 0 || position >= alphabet.size()) {
            throw new IllegalArgumentException("Illegal rotor position: " + position);
        }
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

//    public Alphabet getAlphabet() {
//        return alphabet;
//    }

    public int encodeForward(int inputIndex) {
        int n = alphabet.size();
        int shiftedIn = (inputIndex + position) % n;
        int mapped = forwardMap[shiftedIn];
        int shiftedOut = (mapped - position + n) % n;
        return shiftedOut;
    }

    public int encodeBackward(int inputIndex) {
        int n = alphabet.size();
        int shiftedIn = (inputIndex + position) % n;
        int mapped = backwardMap[shiftedIn];
        int shiftedOut = (mapped - position + n) % n;
        return shiftedOut;
    }

    @Override
    public String toString() {
        return "Rotor{" +
                "id=" + id +
                ", notch=" + notch +
                ", position=" + position +
                '}';
    }
}
