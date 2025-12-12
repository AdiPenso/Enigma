package statistics;

import java.util.List;

public interface StatisticsManager {
    void recordNewCodeConfiguration(List<Integer> rotorIdsRightToLeft,
                                    List<Integer> notchOffsetsRightToLeft,
                                    String initialPositionsRightToLeft,
                                    int reflectorIdNumeric);
    void recordProcessedMessage(String input, String output, long durationNano);
    long getTotalProcessedMessages();
    List<CodeUsageRecord> getAllSessions();
    CodeUsageRecord getLastSession();
}
