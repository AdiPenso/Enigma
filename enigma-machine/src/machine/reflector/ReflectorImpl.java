package machine.reflector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReflectorImpl implements Reflector {
//TODO validation tests
    private final Map<Integer, Integer> mapping;
    int id;

    public ReflectorImpl(int id, Map<Integer, Integer> mapping) {
        this.id = id;
        this.mapping = Collections.unmodifiableMap(new HashMap<>(mapping));
    }

    @Override
    public int reflect(int input) {
        Integer output = mapping.get(input);
        if (output == null) {
            throw new IllegalArgumentException(
                    "Reflector " + id + ": no mapping found for input index " + input
            );
        }
        return output;
    }

    //TODO remove if not needed
    public int getId() {
        return id;
    }

}
