package engine;

import patmal.course.enigma.loader.schema.BTEEnigma;
import patmal.course.enigma.loader.schema.BTEPositioning;
import patmal.course.enigma.loader.schema.BTEReflect;
import patmal.course.enigma.loader.schema.BTEReflector;
import patmal.course.enigma.loader.schema.BTERotor;

import dto.AutomaticCodeDTO;
import dto.MachineSpecificationDTO;
import loadManager.LoadManager;
import loadManager.LoadManagerImpl;
import machine.code.Code;
import machine.code.CodeImpl;
import machine.keyboard.KeyboardImpl;
import machine.machine.Machine;
import machine.machine.MachineImpl;
import machine.reflector.Reflector;
import machine.reflector.ReflectorImpl;
import machine.rotor.Rotor;
import machine.rotor.RotorImpl;
import repository.Repository;
import repository.RepositoryImpl;
import statistics.CodeUsageRecord;
import statistics.StatisticsManager;
import statistics.StatisticsManagerImpl;

import java.io.*;
import java.util.*;

import static engine.Utils.intToRoman;
import static engine.Utils.romanToInt;

public class EngineImpl implements Engine {

    private static final int requiredRotorsCount = 3;
    private Machine machine;
    private String currentAbc;
    private final LoadManager loadManager;
    private StatisticsManager statisticsManager;
    private Repository repository;

    public EngineImpl() {
        this.machine = null;
        this.currentAbc = null;
        this.repository = null;
        this.loadManager = new LoadManagerImpl();
        this.statisticsManager = new StatisticsManagerImpl();
    }

    @Override
    public void loadXml(String filePath) {

        BTEEnigma dto = loadManager.load(filePath);
        validateConfiguration(dto);

        this.repository = buildRepository(dto);
        this.machine = null;
        this.statisticsManager = new StatisticsManagerImpl();
    }

    @Override
    public MachineSpecificationDTO getMachineSpecification() {
        ensureRepositoryLoaded();

        int totalRotors = repository.getAvailableRotorsCount();
        int totalReflectors = repository.getAvailableReflectorsCount();
        long totalMessages = statisticsManager.getTotalProcessedMessages();
        String originalCodeStr = null;
        CodeUsageRecord lastSession = statisticsManager.getLastSession();

        if (lastSession != null) {
            originalCodeStr = formatCodeFromSnapshot(
                    lastSession.getRotorIdsRightToLeft(),
                    lastSession.getInitialPositionsRightToLeft(),
                    lastSession.getNotchOffsetsRightToLeft(),
                    lastSession.getReflectorIdNumeric()
            );
        }

        String currentCodeStr = null;
        if (machine != null) {
            currentCodeStr = formatCurrentCode();
        }

        return new MachineSpecificationDTO(
                totalRotors,
                totalReflectors,
                totalMessages,
                originalCodeStr,
                currentCodeStr
        );
    }

    private String formatCodeFromSnapshot(List<Integer> rotorIdsRightToLeft, String initialPositionsRightToLeft, List<Integer> notchOffsetsRightToLeft, int reflectorIdNumeric) {

        if (rotorIdsRightToLeft == null || initialPositionsRightToLeft == null) {
            return null;
        }

        int rotorsCount = rotorIdsRightToLeft.size();
        StringBuilder sb = new StringBuilder();

        sb.append('<');
        for (int i = rotorsCount - 1; i >= 0; i--) {
            sb.append(rotorIdsRightToLeft.get(i));
            if (i > 0) {
                sb.append(',');
            }
        }
        sb.append('>');

        sb.append('<');
        for (int i = rotorsCount - 1; i >= 0; i--) {
            char posChar = initialPositionsRightToLeft.charAt(i);
            int offset = notchOffsetsRightToLeft.get(i);

            sb.append(posChar)
                    .append('(')
                    .append(offset)
                    .append(')');

            if (i > 0) {
                sb.append(',');
            }
        }
        sb.append('>');

        String reflectorRoman = intToRoman(reflectorIdNumeric);
        sb.append('<')
                .append(reflectorRoman)
                .append('>');

        return sb.toString();
    }

    private String formatCurrentCode() {
        ensureMachineLoaded();

        Code code = machine.getCode();
        List<Rotor> rotorsRightToLeft = code.getRotors();
        int reflectorId = code.getReflector().getId();
        int rotorsCount = rotorsRightToLeft.size();
        StringBuilder sb = new StringBuilder();

        sb.append('<');
        for (int i = rotorsCount - 1; i >= 0; i--) {
            sb.append(rotorsRightToLeft.get(i).getId());
            if (i > 0) {
                sb.append(',');
            }
        }
        sb.append('>');

        sb.append('<');
        for (int i = rotorsCount - 1; i >= 0; i--) {
            Rotor rotor = rotorsRightToLeft.get(i);
            char atWindowChar = currentAbc.charAt(rotor.getPosition());
            int notchOffset = rotor.getNotchIndex();

            sb.append(atWindowChar)
                    .append('(')
                    .append(notchOffset)
                    .append(')');

            if (i > 0) {
                sb.append(',');
            }
        }

        sb.append('>');
        sb.append('<').append(intToRoman(reflectorId)).append('>');

        return sb.toString();
    }

    private void validateConfiguration(BTEEnigma dto) {
        String abc = extractAndValidateAbc(dto);
        this.currentAbc = abc;

        validateRotors(dto.getBTERotors().getBTERotor(), abc);
        validateReflectors(dto.getBTEReflectors().getBTEReflector(), abc);
    }

    private String extractAndValidateAbc(BTEEnigma dto) {
        String rawAbc = dto.getABC();
        if (rawAbc == null) {
            throw new ConfigurationException("ABC element is missing from XML.");
        }

        String abc = rawAbc.trim();

        if (abc.isEmpty()) {
            throw new ConfigurationException("ABC must not be empty.");
        }

        if (abc.length() % 2 != 0) {
            throw new ConfigurationException("ABC length must be even. Found length: " + abc.length());
        }

        Set<Character> seen = new HashSet<>();
        for (char ch : abc.toCharArray()) {
            char upper = Character.toUpperCase(ch);
            if (!seen.add(upper)) {
                throw new ConfigurationException("ABC contains duplicate letter: " + upper);
            }
        }

        return abc;
    }

    private void validateRotors(List<BTERotor> rotors, String abc) {
        if (rotors == null || rotors.isEmpty()) {
            throw new ConfigurationException("Configuration must define at least 3 rotors, but none were found.");
        }

        if (rotors.size() < requiredRotorsCount) {
            throw new ConfigurationException("Configuration must define at least 3 rotors. Found: " + rotors.size());
        }

        Set<Integer> ids = new HashSet<>();
        int maxId = 0;
        for (BTERotor rotor : rotors) {
            int id = rotor.getId();
            if (!ids.add(id)) {
                throw new ConfigurationException("Duplicate rotor id: " + id);
            }
            if (id > maxId) {
                maxId = id;
            }
        }

        if (!ids.contains(1) || ids.size() != maxId) {
            throw new ConfigurationException("Rotor ids must form a continuous range from 1 to " + maxId +
                    ". Found ids: " + ids);
        }

        int abcLength = abc.length();


        for (BTERotor rotor : rotors) {
            int id = rotor.getId();
            List<BTEPositioning> positions = rotor.getBTEPositioning();

            if (positions.size() != abcLength) {
                throw new ConfigurationException("Rotor " + id + " must contain " + abcLength +
                        " mappings (as ABC length), but found " + positions.size());
            }

            int notch = rotor.getNotch();
            if (notch < 1 || notch > positions.size()) {
                throw new ConfigurationException("Rotor " + id + " has invalid notch: " + notch +
                        ". Must be in range [1, " + positions.size() + "].");
            }

            Set<Character> rightSeen = new HashSet<>();
            Set<Character> leftSeen = new HashSet<>();

            for (BTEPositioning pos : positions) {
                char right = Character.toUpperCase(pos.getRight().charAt(0));
                char left = Character.toUpperCase(pos.getLeft().charAt(0));

                if (abc.indexOf(right) == -1) {
                    throw new ConfigurationException("Rotor " + id + " has right letter '" + right +
                            "' which is not present in ABC.");
                }
                if (abc.indexOf(left) == -1) {
                    throw new ConfigurationException("Rotor " + id + " has left letter '" + left +
                            "' which is not present in ABC.");
                }

                if (!rightSeen.add(right)) {
                    throw new ConfigurationException("Rotor " + id + " has duplicate right mapping for letter '" +
                            right + "'.");
                }
                if (!leftSeen.add(left)) {
                    throw new ConfigurationException("Rotor " + id + " has duplicate left mapping for letter '" +
                            left + "'.");
                }
            }
        }
    }

    private void validateReflectors(List<BTEReflector> reflectors, String abc) {
        if (reflectors == null || reflectors.isEmpty()) {
            throw new ConfigurationException("Configuration must define at least one reflector.");
        }

        Set<Integer> numericIds = new HashSet<>();

        int abcLength = abc.length();

        for (BTEReflector reflector : reflectors) {
            String romanId = reflector.getId();
            int numId = romanToInt(romanId);

            if (numId < 1 || numId > 5) {
                throw new ConfigurationException("Reflector id must be between I and V. Found: " + romanId);
            }

            if (!numericIds.add(numId)) {
                throw new ConfigurationException("Duplicate reflector id: " + romanId);
            }

            List<BTEReflect> mappings = reflector.getBTEReflect();

            if (mappings.size() != abcLength / 2) {
                throw new ConfigurationException("Reflector " + romanId + " must contain " + (abcLength / 2) +
                        " mappings, but found " + mappings.size());
            }

            Set<Integer> usedInputs = new HashSet<>();
            Set<Integer> usedOutputs = new HashSet<>();

            for (BTEReflect reflect : mappings) {
                int input = reflect.getInput();
                int output = reflect.getOutput();

                if (input == output) {
                    throw new ConfigurationException("Reflector " + romanId +
                            " has illegal mapping from " + input + " to itself.");
                }

                if (input < 1 || input > abcLength || output < 1 || output > abcLength) {
                    throw new ConfigurationException("Reflector " + romanId +
                            " has mapping out of ABC range: " + input + " -> " + output);
                }

                if (!usedInputs.add(input)) {
                    throw new ConfigurationException("Reflector " + romanId +
                            " uses input position " + input + " more than once.");
                }
                if (!usedOutputs.add(output)) {
                    throw new ConfigurationException("Reflector " + romanId +
                            " uses output position " + output + " more than once.");
                }
            }
        }

        int maxId = numericIds.stream().mapToInt(Integer::intValue).max().orElse(0);
        if (!numericIds.contains(1) || numericIds.size() != maxId) {
            throw new ConfigurationException("Reflector ids must form a continuous roman range starting from I. " +
                    "Found numeric ids: " + numericIds);
        }
    }

    private RepositoryImpl buildRepository(BTEEnigma dto) {
        String abc = this.currentAbc;
        Map<Integer, Rotor> rotorsById = new HashMap<>();

        for (BTERotor rotorDto : dto.getBTERotors().getBTERotor()) {
            int id = rotorDto.getId();
            String rightSeq = buildRightSequence(rotorDto);
            String leftSeq = buildLeftSequence(rotorDto);
            int notch = rotorDto.getNotch();
            Rotor rotor = new RotorImpl(abc, rightSeq, leftSeq, notch, id);

            rotorsById.put(id, rotor);
        }

        Map<Integer, Reflector> reflectorsById = new HashMap<>();

        for (BTEReflector reflectorDto : dto.getBTEReflectors().getBTEReflector()) {
            int numericId = romanToInt(reflectorDto.getId());
            Map<Integer, Integer> mapping = buildReflectorMapping(reflectorDto);
            Reflector reflector = new ReflectorImpl(numericId, mapping);

            reflectorsById.put(numericId, reflector);
        }

        return new RepositoryImpl(abc, rotorsById, reflectorsById);
    }

    private String buildRightSequence(BTERotor rotorDto) {
        StringBuilder sb = new StringBuilder();

        for (BTEPositioning pos : rotorDto.getBTEPositioning()) {
            char right = Character.toUpperCase(pos.getRight().charAt(0));

            sb.append(right);
        }

        return sb.toString();
    }

    private String buildLeftSequence(BTERotor rotorDto) {
        StringBuilder sb = new StringBuilder();

        for (BTEPositioning pos : rotorDto.getBTEPositioning()) {
            char left = Character.toUpperCase(pos.getLeft().charAt(0));

            sb.append(left);
        }

        return sb.toString();
    }

    private Map<Integer, Integer> buildReflectorMapping(BTEReflector reflectorDto) {
        Map<Integer, Integer> mapping = new HashMap<>();

        for (BTEReflect reflect : reflectorDto.getBTEReflect()) {
            int input = reflect.getInput();
            int output = reflect.getOutput();
            int inIndex = input - 1;
            int outIndex = output - 1;

            mapping.put(inIndex, outIndex);
            mapping.put(outIndex, inIndex);
        }

        return mapping;
    }

    private void ensureMachineCreated() {
        if (machine == null) {
            machine = new MachineImpl(new KeyboardImpl(repository.getAbc()));
        }
    }

    @Override
    public AutomaticCodeDTO codeAutomatic() {
        ensureRepositoryLoaded();

        String abc = repository.getAbc();
        Random random = new Random();
        List<Integer> allRotorIds = new ArrayList<>(repository.getRotorsById().keySet());

        Collections.shuffle(allRotorIds, random);
        List<Integer> chosenRotorIds = allRotorIds.subList(0, requiredRotorsCount);
        StringBuilder positions = new StringBuilder(requiredRotorsCount);

        for (int i = 0; i < requiredRotorsCount; i++) {
            positions.append(abc.charAt(random.nextInt(abc.length())));
        }

        List<Integer> allReflectors = new ArrayList<>(repository.getReflectorsById().keySet());
        int reflectorId = allReflectors.get(random.nextInt(allReflectors.size()));

        buildCodeAndSet(chosenRotorIds, positions.toString(), reflectorId);

        return new AutomaticCodeDTO(
                new ArrayList<>(chosenRotorIds),
                positions.toString(),
                reflectorId
        );
    }

    @Override
    public String processText(String message) {
        ensureRepositoryLoaded();
        ensureMachineLoaded();

        if (message == null) {
            throw new ConfigurationException("Input message must not be null.");
        }

        message = message.toUpperCase();
        String abc = repository.getAbc();

        for (char c : message.toCharArray()) {
            if (abc.indexOf(c) == -1) {
                throw new ConfigurationException(
                        "Character '" + c + "' is not part of the machine alphabet. " +
                                "Please use only characters from: " + abc);
            }
        }

        long start = System.nanoTime();
        StringBuilder processedMessage = new StringBuilder();
        for (char c : message.toCharArray()) {
            char processedChar = machine.processChar(c);
            processedMessage.append(processedChar);
        }

        long end = System.nanoTime();
        long durationNanos = end - start;
        String output = processedMessage.toString();

        statisticsManager.recordProcessedMessage(message, output, durationNanos);

        return output;
    }

    @Override
    public void resetToLastCode() {

        ensureRepositoryLoaded();
        CodeUsageRecord lastSession = statisticsManager.getLastSession();

        if (lastSession == null) {
            throw new ConfigurationException("No code was defined yet – cannot reset.");
        }

        List<Integer> rotorIdsRightToLeft = lastSession.getRotorIdsRightToLeft();
        String initialPositionsRightToLeft = lastSession.getInitialPositionsRightToLeft();
        int reflectorIdNumeric = lastSession.getReflectorIdNumeric();

        applyCodeToMachine(
                new ArrayList<>(rotorIdsRightToLeft),
                initialPositionsRightToLeft,
                reflectorIdNumeric,
                false
        );
    }

    @Override
    public String getHistoryAndStatistics() {
        ensureRepositoryLoaded();

        List<CodeUsageRecord> sessions = statisticsManager.getAllSessions();

        if (sessions.isEmpty()) {
            return "No codes were defined yet – history is empty.";
        }

        StringBuilder sb = new StringBuilder();
        int sessionIndex = 1;

        for (CodeUsageRecord session : sessions) {
            String codeConfigStr = formatCodeFromSnapshot(
                    session.getRotorIdsRightToLeft(),
                    session.getInitialPositionsRightToLeft(),
                    session.getNotchOffsetsRightToLeft(),
                    session.getReflectorIdNumeric()
            );

            sb.append(sessionIndex)
                    .append(". ")
                    .append(codeConfigStr)
                    .append(System.lineSeparator());

            List<String> inputs = session.getInputMessages();
            List<String> outputs = session.getOutputMessages();
            List<Long> durations = session.getDurationsNano();

            int messageIndex = 1;
            for (int i = 0; i < inputs.size(); i++) {
                sb.append(messageIndex)
                        .append(". <")
                        .append(inputs.get(i))
                        .append("> --> <")
                        .append(outputs.get(i))
                        .append("> (")
                        .append(durations.get(i))
                        .append(" nano-seconds)")
                        .append(System.lineSeparator());

                messageIndex++;
            }

            sb.append(System.lineSeparator());
            sessionIndex++;
        }

        return sb.toString();
    }

    private void ensureRepositoryLoaded() {
        if (repository == null) {
            throw new ConfigurationException(
                    "No configuration loaded yet. Please load an XML file first (command 1).");
        }
    }

    @Override
    public void ensureMachineLoaded() {
        if (machine == null) {
            throw new ConfigurationException(
                    "No code configuration defined yet. Please use command 3 or 4 before processing.");
        }
    }

    @Override
    public void setManualCodeConfiguration(List<Integer> rotorIdsLeftToRight,
                                           String initialPositionsLeftToRight,
                                           int reflectorIdDecimal) {
        ensureRepositoryLoaded();

        if (rotorIdsLeftToRight == null || rotorIdsLeftToRight.isEmpty()) {
            throw new ConfigurationException("You must choose at least one rotor.");
        }

        if (rotorIdsLeftToRight.size() != requiredRotorsCount) {
             throw new ConfigurationException(
                     "You must choose exactly " + requiredRotorsCount + " rotors.");
         }

        int rotorsCount = rotorIdsLeftToRight.size();
        Set<Integer> uniqueRotors = new HashSet<>(rotorIdsLeftToRight);

        if (uniqueRotors.size() != rotorsCount) {
            throw new ConfigurationException("Each rotor ID must appear at most once (no duplicates allowed).");
        }

        int totalRotors = repository.getAvailableRotorsCount();
        for (int rotorId : rotorIdsLeftToRight) {
            if (rotorId < 1 || rotorId > totalRotors) {
                throw new ConfigurationException(
                        "Rotor ID " + rotorId + " does not exist in the configuration. " +
                                "Valid IDs are between 1 and " + totalRotors + ".");
            }
        }

        if (initialPositionsLeftToRight == null) {
            throw new ConfigurationException("Initial positions string must not be null.");
        }

        String positionsTrimmed = initialPositionsLeftToRight.trim();

        if (positionsTrimmed.length() != rotorsCount) {
            throw new ConfigurationException(
                    "Number of initial positions (" + positionsTrimmed.length()
                            + ") must match the number of chosen rotors (" + rotorsCount + ").");
        }

        String abc = repository.getAbc();
        for (int i = 0; i < positionsTrimmed.length(); i++) {
            char ch = positionsTrimmed.charAt(i);
            if (abc.indexOf(ch) == -1) {
                throw new ConfigurationException(
                        "Initial position '" + ch + "' is not part of the machine alphabet.");
            }
        }

        int totalReflectors = repository.getAvailableReflectorsCount();
        if (reflectorIdDecimal < 1 || reflectorIdDecimal > totalReflectors) {
            throw new ConfigurationException(
                    "Reflector index must be between 1 and " + totalReflectors
                            + " (got " + reflectorIdDecimal + ").");
        }

        List<Integer> rotorIdsRightToLeft = new ArrayList<>(rotorsCount);
        for (int i = rotorsCount - 1; i >= 0; i--) {
            rotorIdsRightToLeft.add(rotorIdsLeftToRight.get(i));
        }

        String initialPositionsRightToLeft = new StringBuilder(positionsTrimmed).reverse().toString();

        buildCodeAndSet(rotorIdsRightToLeft, initialPositionsRightToLeft, reflectorIdDecimal);
    }

    private void applyCodeToMachine(List<Integer> rotorIdsRightToLeft,
                                    String initialPositionsRightToLeft,
                                    int reflectorIdNumeric,
                                    boolean recordStatistics) {

        int rotorsCount = rotorIdsRightToLeft.size();

        List<Rotor> rotors = new ArrayList<>(rotorsCount);
        List<Integer> notchOffsetsRightToLeft = new ArrayList<>(rotorsCount);

        for (int i = 0; i < rotorsCount; i++) {
            int rotorId = rotorIdsRightToLeft.get(i);

            Rotor rotor = repository.createFreshRotor(rotorId);

            char pos = initialPositionsRightToLeft.charAt(i);
            int index = repository.getRotor(rotorId).getRightSequence().indexOf(pos);
            rotor.setPosition(index);
            rotors.add(rotor);
            notchOffsetsRightToLeft.add(rotor.getNotchIndex());
        }

        Reflector reflector = repository.getReflector(reflectorIdNumeric);

        Code code = new CodeImpl(rotors, reflector);
        ensureMachineCreated();
        machine.setCode(code);

        if (recordStatistics) {
            statisticsManager.recordNewCodeConfiguration(
                    rotorIdsRightToLeft,
                    notchOffsetsRightToLeft,
                    initialPositionsRightToLeft,
                    reflectorIdNumeric
            );
        }
    }

    private void buildCodeAndSet(List<Integer> rotorIdsRightToLeft,
                                 String initialPositionsRightToLeft,
                                 int reflectorIdNumeric) {
        applyCodeToMachine(rotorIdsRightToLeft, initialPositionsRightToLeft, reflectorIdNumeric, true);
    }

    @Override
    public void saveSystemStateToFile(String fileName) throws IOException {
        ensureRepositoryLoaded();
        ensureMachineLoaded();

        if (statisticsManager == null) {
            throw new ConfigurationException("Statistics are not initialized yet.");
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(machine);
            out.writeObject(statisticsManager);
            out.writeObject(repository);
            out.writeObject(currentAbc);
        }
    }

    @Override
    public void loadSystemStateFromFile(String fileName) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            this.machine = (Machine) in.readObject();
            this.statisticsManager = (StatisticsManager) in.readObject();
            this.repository = (Repository) in.readObject();
            this.currentAbc = (String) in.readObject();

        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to load system state from file: " + e.getMessage(), e);
        }
    }
}


