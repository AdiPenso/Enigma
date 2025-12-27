package patmal.course.enigma.machine.code;

import patmal.course.enigma.machine.reflector.Reflector;
import patmal.course.enigma.machine.rotor.Rotor;
import java.util.List;

public interface Code {
    List<Rotor> getRotors();
    Reflector getReflector();
}
