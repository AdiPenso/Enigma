package engine;

import dto.AutomaticCodeDTO;
import dto.MachineSpecificationDTO;

import java.util.List;

public interface Engine {
    void loadXml(String filePath);
    MachineSpecificationDTO getMachineSpecification();
    public void setManualCodeConfiguration(List<Integer> rotorIdsLeftToRight,
                                           String initialPositionsLeftToRight,
                                           int reflectorIdDecimal);
    AutomaticCodeDTO codeAutomatic();
    //String process(String input); // TODO need to move messageProcessor from Machine package to here

    //was added for checking automatic coding
    String processText(String text);
    void resetToLastCode();

    String getHistoryAndStatistics();
    void ensureMachineLoaded();
}
