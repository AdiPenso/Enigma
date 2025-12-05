package machine.reflector;

public class ReflectorImpl implements Reflector {

    private final int[] mapping;

    public ReflectorImpl(int[] mapping) {
        this.mapping = mapping.clone();
    }

    @Override
    public int reflect(int input) {
        if (input < 0 || input >= mapping.length) {
            throw new IllegalArgumentException("Index out of range: " + input);
        }
        return mapping[input];
    }

}
