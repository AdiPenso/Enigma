package repository;

import machine.reflector.Reflector;
import machine.rotor.Rotor;

import java.util.Map;

public interface Repository {
    public void store(String abc,
               Map<Integer, Rotor> rotorsById,
               Map<Integer, Reflector> reflectorsById);

}
