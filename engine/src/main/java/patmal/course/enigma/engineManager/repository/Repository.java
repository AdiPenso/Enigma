package patmal.course.enigma.engineManager.repository;

import patmal.course.enigma.machine.reflector.Reflector;
import patmal.course.enigma.machine.rotor.Rotor;

import java.util.Map;

public interface Repository {
    void store(String abc,
               Map<Integer, Rotor> rotorsById,
               Map<Integer, Reflector> reflectorsById);


    String getAbc();
    Rotor getRotor(int rotorId);
    Reflector getReflector(int reflectorIdNumeric);
    Map<Integer, Rotor> getRotorsById();
    Map<Integer, Reflector> getReflectorsById();
    int getAvailableRotorsCount();
    int getAvailableReflectorsCount();
    Rotor createFreshRotor(int rotorId);
}
