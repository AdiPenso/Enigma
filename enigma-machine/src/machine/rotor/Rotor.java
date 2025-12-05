package machine.rotor;

public interface Rotor {

    int encodeForward(int input);
    int encodeBackward(int input);
    boolean advance();

    String getPosition();

//    void step();
//    boolean atNotch();
}
