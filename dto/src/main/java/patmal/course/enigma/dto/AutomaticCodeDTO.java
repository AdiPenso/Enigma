package patmal.course.enigma.dto;

import java.util.List;

public class AutomaticCodeDTO {
    private final List<Integer> rotorIdsRightToLeft;
    private final String initialPositionsRightToLeft;
    private final int reflectorIdNumeric;

    public AutomaticCodeDTO(List<Integer> rotorIdsRightToLeft,
                            String initialPositionsRightToLeft,
                            int reflectorIdNumeric) {
        this.rotorIdsRightToLeft = rotorIdsRightToLeft;
        this.initialPositionsRightToLeft = initialPositionsRightToLeft;
        this.reflectorIdNumeric = reflectorIdNumeric;
    }

    public List<Integer> getRotorIdsRightToLeft() {
        return rotorIdsRightToLeft;
    }

    public String getInitialPositionsRightToLeft() {
        return initialPositionsRightToLeft;
    }

    public int getReflectorIdNumeric() {
        return reflectorIdNumeric;
    }
}
