package dto;

public class MachineSpecificationDTO {
    private final int totalRotorsCount;
    private final int totalReflectorsCount;
    private final long totalProcessedMessages;
    private final String originalCodeConfiguration;
    private final String currentCodeConfiguration;

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
