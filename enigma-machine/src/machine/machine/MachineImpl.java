package machine.machine;

import machine.code.Code;
import machine.keyboard.Keyboard;
import machine.rotor.Rotor;
//import machine.rotor.RotorPosition;

import java.util.List;

public class MachineImpl implements Machine {
    private Code code;
    private final Keyboard keyboard;

    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void setCode(Code code){
        this.code = code;
    }

    @Override
    public char processChar(char input) {
        int intermediate = keyboard.processChar(input);
        List<Rotor> rotors = code.getRotors();

        advance(rotors);

        intermediate = forward(rotors, intermediate);

        intermediate = code.getReflector().reflect(intermediate);

        intermediate = backward(rotors, intermediate);

        return keyboard.lightALamp(intermediate);
    }

    private int backward(List<Rotor> rotors, int intermediate) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).encodeBackward(intermediate);
        }
        return intermediate;
    }

    private int forward(List<Rotor> rotors, int intermediate) {
        for (int i = 0; i < rotors.size(); i++) {
           // System.out.println("Rotor " + i + " position before encoding: " + rotors.get(i).getPosition() + "intermidiate: "+ intermediate);
            intermediate = rotors.get(i).encodeForward(intermediate);
        }

        return intermediate;
    }

    private void advance(List<Rotor> rotors) {
        int rotorIndex = 0;
        boolean shouldAdvance = false;

        do {
            shouldAdvance = rotors.get(rotorIndex).advance();
            rotorIndex++;
        }while(shouldAdvance && rotorIndex < rotors.size());
    }

    @Override
    public Code getCode() {
        return this.code;
    }
}
