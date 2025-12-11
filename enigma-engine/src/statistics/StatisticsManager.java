package statistics;

import java.util.List;

public interface StatisticsManager {

    // called when a new code is chosen (command 3 or 4)
    void recordNewCodeConfiguration(List<Integer> rotorIdsRightToLeft,
                                    List<Integer> notchOffsetsRightToLeft,
                                    String initialPositionsRightToLeft,
                                    int reflectorIdNumeric);

    // called every time a message is processed (command 5)
    void recordProcessedMessage(String input, String output, long durationNano);

    long getTotalProcessedMessages();

    List<CodeUsageRecord> getAllSessions();

    CodeUsageRecord getLastSession();
}
