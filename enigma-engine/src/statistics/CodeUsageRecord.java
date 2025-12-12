package statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CodeUsageRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Integer> rotorIdsRightToLeft;
    private final String initialPositionsRightToLeft;
    private final int reflectorIdNumeric;
    private final List<Integer> notchOffsetsRightToLeft;
    private final List<String> outputMessages = new ArrayList<>();
    private final List<String> inputMessages = new ArrayList<>();
    private final List<Long> durationsNano = new ArrayList<>();

    public CodeUsageRecord(List<Integer> rotorIdsRightToLeft,
                           String initialPositionsRightToLeft,
                           List<Integer> notchOffsetsRightToLeft,
                           int reflectorIdNumeric) {
        this.rotorIdsRightToLeft = new ArrayList<>(rotorIdsRightToLeft);
        this.initialPositionsRightToLeft = initialPositionsRightToLeft;
        this.notchOffsetsRightToLeft = new ArrayList<>(notchOffsetsRightToLeft);
        this.reflectorIdNumeric = reflectorIdNumeric;
    }

    public List<Integer> getRotorIdsRightToLeft() {
        return rotorIdsRightToLeft;
    }

    public String getInitialPositionsRightToLeft() {
        return initialPositionsRightToLeft;
    }

    public List<Integer> getNotchOffsetsRightToLeft() {
        return notchOffsetsRightToLeft;
    }

    public int getReflectorIdNumeric() {
        return reflectorIdNumeric;
    }

    public List<String> getInputMessages() {
        return inputMessages;
    }

    public List<String> getOutputMessages() {
        return outputMessages;
    }

    public List<Long> getDurationsNano() {
        return durationsNano;
    }

    public void addProcessedMessage(String input, String output, long durationNano) {
        inputMessages.add(input);
        outputMessages.add(output);
        durationsNano.add(durationNano);
    }
}
