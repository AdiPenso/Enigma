package repository;

import machine.reflector.Reflector;
import machine.rotor.Rotor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepositoryImpl implements Repository {
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

        // למה לא רק final?
        // final מגן על הרפרנס, אבל לא מונע שינוי בתוכן המפה.
        // כאן אנחנו יוצרות עותק + עוטפות ב-unmodifiable כדי שאף אחד מבחוץ לא יהרוס את המאגר בטעות.
        this.rotorsById = Collections.unmodifiableMap(new HashMap<>(rotorsById));
        this.reflectorsById = Collections.unmodifiableMap(new HashMap<>(reflectorsById));
    }

    public String getAbc() {
        return abc;
    }

    public Map<Integer, Rotor> getRotorsById() {
        return rotorsById;
    }

    public Map<Integer, Reflector> getReflectorsById() {
        return reflectorsById;
    }

    public Rotor getRotor(int id) {
        return rotorsById.get(id);
    }

    public Reflector getReflector(int id) {
        return reflectorsById.get(id);
    }



}
