package machine.machine;

import machine.code.Code;
import machine.keyboard.Keyboard;
import machine.rotor.RotorPosition;

import java.util.List;

public class MachineImpl implements Machine {
    private Code code;
    private final Keyboard keyboard;

    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void setCode(Code code){

    }
    @Override
    public char processChar(char input) {
        int intermediate = keyboard.processChar(input);
        List<RotorPosition> rotorsPositions = code.getRotorsPositions();

        advance(rotorsPositions);

        intermediate = forward(rotorsPositions, intermediate);

        intermediate = code.getReflector().reflect(intermediate);

        intermediate = backward(rotorsPositions, intermediate);

        return keyboard.lightALamp(intermediate);
    }

    private int backward(List<RotorPosition> rotorsPositions, int intermediate) {
        for (int i = rotorsPositions.size() - 1; i >= 0; i--) {
            intermediate = rotorsPositions.get(i).rotor.encodeBackward(intermediate);
        }
        return intermediate;
    }

    private int forward(List<RotorPosition> rotorsPositions, int intermediate) {
        for (int i = 0; i < rotorsPositions.size(); i++) {
            intermediate = rotorsPositions.get(i).rotor.encodeForward(intermediate);
        }
        return intermediate;
    }

    private void advance(List<RotorPosition> rotorsPositions) {
        int rotorIndex = 0;
        boolean shouldAdvance = false;

        do {
            shouldAdvance = rotorsPositions.get(rotorIndex).rotor.advance();
            rotorIndex++;
        }while(shouldAdvance && rotorIndex < rotorsPositions.size());
    }
}
