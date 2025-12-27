package machine.code;

import machine.reflector.Reflector;
import machine.rotor.Rotor;
import java.util.List;

public interface Code {
    List<Rotor> getRotors();
    Reflector getReflector();
}
