package patmal.course.enigma.engineManager.repository;

import patmal.course.enigma.engineManager.engine.ConfigurationException;
import patmal.course.enigma.machine.reflector.Reflector;
import patmal.course.enigma.machine.rotor.Rotor;
import patmal.course.enigma.machine.rotor.RotorImpl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepositoryImpl implements Repository, Serializable {
    private static final long serialVersionUID = 1L;
    private String abc;
    private Map<Integer, Rotor> rotorsById = Collections.emptyMap();
    private Map<Integer, Reflector> reflectorsById = Collections.emptyMap();

    public RepositoryImpl(String abc,
                              Map<Integer, Rotor> rotorsById,
                              Map<Integer, Reflector> reflectorsById) {
        store(abc, rotorsById, reflectorsById);
    }

    @Override
    public void store(String abc,
                      Map<Integer, Rotor> rotorsById,
                      Map<Integer, Reflector> reflectorsById) {

        this.abc = abc;
        this.rotorsById = Collections.unmodifiableMap(new HashMap<>(rotorsById));
        this.reflectorsById = Collections.unmodifiableMap(new HashMap<>(reflectorsById));
    }

    @Override
    public String getAbc() { return abc;}

    @Override
    public Map<Integer, Rotor> getRotorsById() {
        return rotorsById;
    }

    public Map<Integer, Reflector> getReflectorsById() {
        return reflectorsById;
    }

    @Override
    public Rotor getRotor(int id) {
        return rotorsById.get(id);
    }

    @Override
    public Reflector getReflector(int id) {
        return reflectorsById.get(id);
    }

    @Override
    public int getAvailableRotorsCount() {
        return rotorsById.size();
    }

    @Override
    public int getAvailableReflectorsCount() {
        return reflectorsById.size();
    }

    @Override
    public Rotor createFreshRotor(int rotorId) {
        Rotor prototype = rotorsById.get(rotorId);
        if (prototype == null) {
            throw new ConfigurationException("Rotor id " + rotorId + " does not exist.");
        }

        if (!(prototype instanceof RotorImpl)) {
            throw new IllegalStateException("Repository expected RotorImpl as prototype.");
        }

        RotorImpl proto = (RotorImpl) prototype;

        return new RotorImpl(
                proto.getAlphabet(),
                proto.getRightSequence(),
                proto.getLeftSequence(),
                proto.getOriginalNotchBase1(),
                proto.getId()
        );
    }

}
