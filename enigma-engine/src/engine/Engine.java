package engine;

import dto.AutomaticCodeDTO;
import dto.MachineSpecificationDTO;

import java.io.IOException;
import java.util.List;

public interface Engine {
    void loadXml(String filePath);
    MachineSpecificationDTO getMachineSpecification();
    void setManualCodeConfiguration(List<Integer> rotorIdsLeftToRight,
                                           String initialPositionsLeftToRight,
                                           int reflectorIdDecimal);
    AutomaticCodeDTO codeAutomatic();
    String processText(String text);
    void resetToLastCode();
    String getHistoryAndStatistics();
    void ensureMachineLoaded();
    void saveSystemStateToFile(String fileName) throws IOException;
    void loadSystemStateFromFile(String fileName) throws IOException;
}
