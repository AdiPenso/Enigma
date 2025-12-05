package machine.code;

import machine.reflector.Reflector;
import machine.rotor.Rotor;
//import machine.rotor.RotorPosition;

import java.util.List;

public class CodeImpl implements Code {

//    private final List<RotorPosition> rotorsPositions;
//    private final Reflector reflector;
//
//    public CodeImpl(List<RotorPosition> rotorsPositions, Reflector reflector) {
//        this.rotorsPositions = rotorsPositions;
//        this.reflector = reflector;
//    }
//
//    @Override
//    public List<RotorPosition> getRotorsPositions() {
//        return rotorsPositions;
//    }

    private final List<Rotor> rotors;
    private final Reflector reflector;

    public CodeImpl(List<Rotor> rotors, Reflector reflector) {
        this.rotors = rotors;
        this.reflector = reflector;
    }

    @Override
    public List<Rotor> getRotors() {
        return rotors;
    }

    @Override
    public Reflector getReflector() {
        return reflector;
    }
}
