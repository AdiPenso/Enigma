package patmal.course.enigma.engineManager.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatisticsManagerImpl implements StatisticsManager, Serializable {
    private static final long serialVersionUID = 1L;
    private final List<CodeUsageRecord> sessions = new ArrayList<>();
    private long totalMessages = 0;

    @Override
    public void recordNewCodeConfiguration(List<Integer> rotorIdsRightToLeft,
                                           List<Integer> notchOffsetsRightToLeft,
                                           String initialPositionsRightToLeft,
                                           int reflectorIdNumeric) {
        CodeUsageRecord newRecord = new CodeUsageRecord(
                rotorIdsRightToLeft,
                initialPositionsRightToLeft,
                notchOffsetsRightToLeft,
                reflectorIdNumeric
        );
        sessions.add(newRecord);
    }

    @Override
    public void recordProcessedMessage(String input, String output, long durationNano) {
        if (sessions.isEmpty()) {
            throw new IllegalStateException("No code configuration was defined before processing messages.");
        }

        CodeUsageRecord currentSession = sessions.getLast();
        currentSession.addProcessedMessage(input, output, durationNano);
        totalMessages++;
    }

    @Override
    public long getTotalProcessedMessages() {
        return totalMessages;
    }

    @Override
    public List<CodeUsageRecord> getAllSessions() {
        return sessions;
    }

    @Override
    public CodeUsageRecord getLastSession() {
        if (sessions.isEmpty()) {
            return null;
        }

        return sessions.getLast();
    }
}
