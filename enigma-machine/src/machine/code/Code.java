package machine.code;

import machine.reflector.Reflector;
import machine.rotor.RotorPosition;

import java.util.List;
import java.util.Map;

public interface Code {
    List<RotorPosition> getRotorsPositions();
    Reflector getReflector();

}
