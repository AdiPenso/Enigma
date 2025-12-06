package engine;

public interface Engine {
    void loadXml(String filePath);
    void showMachineData(); //TODO need to return MachineDTO (Object)
    void codeManual(/* parameters to set the code manually */);
    void codeAutomatic();
    String process(String input); // TODO need to move messageProcessor from Machine package to here
    void statistics();

}
