package machine.code;

import machine.reflector.Reflector;
import machine.rotor.Rotor;
import java.util.List;

public class CodeImpl implements Code {
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
