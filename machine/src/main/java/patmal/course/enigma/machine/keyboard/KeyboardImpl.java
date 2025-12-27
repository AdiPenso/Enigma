package patmal.course.enigma.machine.keyboard;

import java.io.Serializable;

public class KeyboardImpl implements Keyboard, Serializable {
    private static final long serialVersionUID = 1L;
    private final String alphabet;

    public KeyboardImpl(String alphabet) {
        this.alphabet = alphabet.toUpperCase();
    }

    @Override
    public int processChar(char input) {
        char upper = Character.toUpperCase(input);
        int index = alphabet.indexOf(upper);
        if (index == -1) {
            throw new IllegalArgumentException("Character not in alphabet: " + input);
        }
        return index;
    }

    @Override
    public char lightALamp(int input) {
        if (input < 0 || input >= alphabet.length()) {
            throw new IllegalArgumentException("Index out of range: " + input);
        }
        return alphabet.charAt(input);
    }
}
