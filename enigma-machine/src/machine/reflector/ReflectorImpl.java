package machine.reflector;

public class ReflectorImpl implements Reflector {
//TODO validation tests
    private final int[] mapping;
    int id;

    public ReflectorImpl(int id, int[] mapping) {
        this.mapping = mapping.clone();
        this.id = id;
    }

    @Override
    public int reflect(int input) {
        if (input < 0 || input >= mapping.length) {
            throw new IllegalArgumentException("Index out of range: " + input);
        }
        return mapping[input];
    }

}
