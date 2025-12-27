package patmal.course.enigma.machine.code;

import patmal.course.enigma.machine.reflector.Reflector;
import patmal.course.enigma.machine.rotor.Rotor;

import java.io.Serializable;
import java.util.List;

public class CodeImpl implements Code, Serializable {
    private static final long serialVersionUID = 1L;
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
