package machine.machine;

public class MessageProcessor {
    private final Machine machine;

    public MessageProcessor(Machine machine) {
        this.machine = machine;
    }
    public String process(String message) {
        StringBuilder processedMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            char processedChar = machine.processChar(c);
            processedMessage.append(processedChar);
        }
        return processedMessage.toString();
    }
}
