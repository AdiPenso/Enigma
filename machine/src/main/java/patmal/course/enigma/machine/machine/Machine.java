package patmal.course.enigma.machine.machine;

import patmal.course.enigma.machine.code.Code;

public interface Machine {
    void setCode(Code code);
    char processChar(char input);
    Code getCode();
}
