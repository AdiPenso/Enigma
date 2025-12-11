package machine.machine;

import machine.code.Code;

public interface Machine {
    void setCode(Code code);
    char processChar(char input);

    Code getCode();
}
