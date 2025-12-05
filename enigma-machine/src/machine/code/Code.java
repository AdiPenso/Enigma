package machine.code;

import machine.reflector.Reflector;
import machine.rotor.Rotor;
//import machine.rotor.RotorPosition;

import java.util.List;
import java.util.Map;

public interface Code {
    List<Rotor> getRotors();
    Reflector getReflector();

}
