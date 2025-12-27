package patmal.course.enigma.machine.rotor;

public interface Rotor {
    int encodeForward(int input);
    int encodeBackward(int input);
    boolean advance();
    int getPosition();
    int getNotchIndex();
    void setPosition(int index);
    int getId();
    String getRightSequence();
}
