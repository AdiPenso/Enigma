package dto;

public class MachineSpecificationDTO {
    private final int totalRotorsCount;           // סה"כ רוטורים אפשריים לבחירה
    private final int totalReflectorsCount;       // סה"כ משקפים
    private final long totalProcessedMessages;    // כמה הודעות עובדו עד כה
    private final String originalCodeConfiguration; // קוד מקור בפורמט <...><...><...>
    private final String currentCodeConfiguration;  // קוד נוכחי בפורמט <...><...><...>

    public MachineSpecificationDTO(int totalRotorsCount,
                                   int totalReflectorsCount,
                                   long totalProcessedMessages,
                                   String originalCodeConfiguration,
                                   String currentCodeConfiguration) {
        this.totalRotorsCount = totalRotorsCount;
        this.totalReflectorsCount = totalReflectorsCount;
        this.totalProcessedMessages = totalProcessedMessages;
        this.originalCodeConfiguration = originalCodeConfiguration;
        this.currentCodeConfiguration = currentCodeConfiguration;
    }

    public int getTotalRotorsCount() {
        return totalRotorsCount;
    }

    public int getTotalReflectorsCount() {
        return totalReflectorsCount;
    }

    public long getTotalProcessedMessages() {
        return totalProcessedMessages;
    }

    public String getOriginalCodeConfiguration() {
        return originalCodeConfiguration;
    }

    public String getCurrentCodeConfiguration() {
        return currentCodeConfiguration;
    }
}
