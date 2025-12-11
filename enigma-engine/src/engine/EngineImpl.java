package engine;

import dto.AutomaticCodeDTO;
import dto.MachineSpecificationDTO;
import generated.BTEEnigma;
import generated.BTERotor;
import generated.BTEPositioning;
import generated.BTEReflector;
import generated.BTEReflect;

import loadManager.LoadManager;
import loadManager.LoadManagerImpl;
import machine.code.Code;
import machine.code.CodeImpl;
import machine.keyboard.KeyboardImpl;
import machine.machine.Machine;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
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

import java.io.File;
import java.util.*;

import static engine.Utils.intToRoman;
import static engine.Utils.romanToInt;

public class EngineImpl implements Engine {

    private static final int requiredRotorsCount = 3; // TODO make configurable if needed
    private Machine machine;
    private String currentAbc;        // TODO the ABC should be in Machine or temporary variable ? cached ABC string of the last valid configuration
    private LoadManager loadManager; //TODO implement LoadManager class
    private StatisticsManager statisticsManager; //TODO implement StatisticsManager class
    private Repository repository; //TODO implement Repository class

    //was added to check automatic coding
//    private List<Integer> lastRotorIdsRightToLeft;
//    private String lastInitialPositionsRightToLeft;
//    private int lastReflectorId;

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

        // 4. build a new Machine from the configuration
        Repository newRepository = buildRepository(dto);

        // 5. only now, after everything succeeded, replace the current machine
        this.repository = newRepository;

        this.machine = null; //TODO not sure that it's correct
        this.statisticsManager = new StatisticsManagerImpl();
    }


    @Override
    public MachineSpecificationDTO getMachineSpecification() {
        // According to the spec, command 2 is allowed only after a valid XML file
//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet. Please load an XML file first.");
//        }
        ensureRepositoryLoaded();

        int totalRotors = repository.getAvailableRotorsCount();
        int totalReflectors = repository.getAvailableReflectorsCount();
        long totalMessages = statisticsManager.getTotalProcessedMessages();

        // Original code configuration: last code defined by commands 3/4, if any
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
            return null; // no code defined yet
        }

        //String abc = repository.getAbc();
        int rotorsCount = rotorIdsRightToLeft.size();
        StringBuilder sb = new StringBuilder();

        // 1. <rotorIds> – LEFT to RIGHT
        sb.append('<');
        for (int i = rotorsCount - 1; i >= 0; i--) {
            sb.append(rotorIdsRightToLeft.get(i));
            if (i > 0) {
                sb.append(',');
            }
        }
        sb.append('>');

        // 2. <positionsWithOffsets> – also LEFT to RIGHT
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

        // 3. <reflectorRoman> – using the inverse of romanToInt
        String reflectorRoman = intToRoman(reflectorIdNumeric);
        sb.append('<')
                .append(reflectorRoman)
                .append('>');

        return sb.toString();
    }

    private String formatCurrentCode() {
        ensureMachineLoaded();

        Code code = machine.getCode();
        List<Rotor> rotorsRightToLeft = code.getRotors(); // נסי להוסיף getRotors() ל-Code
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
        // 3.1 ABC
        String abc = extractAndValidateAbc(dto);
        this.currentAbc = abc; // store for later use

        // 3.2 Rotors
        validateRotors(dto.getBTERotors().getBTERotor(), abc);

        // 3.3 Reflectors
        validateReflectors(dto.getBTEReflectors().getBTEReflector(), abc);
    }

    private String extractAndValidateAbc(BTEEnigma dto) {
        String rawAbc = dto.getABC();
        if (rawAbc == null) {
            throw new ConfigurationException("ABC element is missing from XML.");
        }

        String abc = rawAbc.trim(); // remove spaces at start/end as required

        if (abc.isEmpty()) {
            throw new ConfigurationException("ABC must not be empty.");
        }

        if (abc.length() % 2 != 0) {
            throw new ConfigurationException("ABC length must be even. Found length: " + abc.length());
        }

        // Optional but recommended: ensure all letters are unique
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

        if (rotors.size() < 3) {
            throw new ConfigurationException("Configuration must define at least 3 rotors. Found: " + rotors.size());
        }

        // 1. unique ids and continuous range starting from 1
        Set<Integer> ids = new HashSet<>();
        int maxId = 0;

        for (BTERotor rotor : rotors) {
            int id = rotor.getId(); // assuming getId() gives int, adjust if needed
            if (!ids.add(id)) {
                throw new ConfigurationException("Duplicate rotor id: " + id);
            }
            if (id > maxId) {
                maxId = id;
            }
        }

        // Check continuous range 1..maxId (no holes, must contain 1)
        if (!ids.contains(1) || ids.size() != maxId) {
            throw new ConfigurationException("Rotor ids must form a continuous range from 1 to " + maxId +
                    ". Found ids: " + ids);
        }

        int abcLength = abc.length();

        // 2. per rotor validation
        for (BTERotor rotor : rotors) {
            int id = rotor.getId();
            List<BTEPositioning> positions = rotor.getBTEPositioning();

            if (positions.size() != abcLength) {
                throw new ConfigurationException("Rotor " + id + " must contain " + abcLength +
                        " mappings (as ABC length), but found " + positions.size());
            }

            // 2.a notch in range [1, rotorSize]
            int notch = rotor.getNotch();
            if (notch < 1 || notch > positions.size()) {
                throw new ConfigurationException("Rotor " + id + " has invalid notch: " + notch +
                        ". Must be in range [1, " + positions.size() + "].");
            }

            // 2.b no duplicate letters in right/left and all letters must belong to ABC
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
            String romanId = reflector.getId(); // e.g. "I", "II"
            int numId = romanToInt(romanId);

            if (numId < 1 || numId > 5) {
                throw new ConfigurationException("Reflector id must be between I and V. Found: " + romanId);
            }

            if (!numericIds.add(numId)) {
                throw new ConfigurationException("Duplicate reflector id: " + romanId);
            }

            List<BTEReflect> mappings = reflector.getBTEReflect();

            // amount of mappings must be ABC length / 2
            if (mappings.size() != abcLength / 2) {
                throw new ConfigurationException("Reflector " + romanId + " must contain " + (abcLength / 2) +
                        " mappings, but found " + mappings.size());
            }

            // We also want to ensure no mapping i->i and input/output within range
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

                // ensure inputs/outputs do not repeat (optional but very reasonable)
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

        // check reflector ids are continuous from I upwards (1..max without holes)
        int maxId = numericIds.stream().mapToInt(Integer::intValue).max().orElse(0);
        if (!numericIds.contains(1) || numericIds.size() != maxId) {
            throw new ConfigurationException("Reflector ids must form a continuous roman range starting from I. " +
                    "Found numeric ids: " + numericIds);
        }
    }

    private RepositoryImpl buildRepository(BTEEnigma dto) {
        String abc = this.currentAbc;

        // ---- build rotors map: id -> Rotor ----
        Map<Integer, Rotor> rotorsById = new HashMap<>();
        for (BTERotor rotorDto : dto.getBTERotors().getBTERotor()) {
            int id = rotorDto.getId();
            String rightSeq = buildRightSequence(rotorDto);
            String leftSeq = buildLeftSequence(rotorDto);
            int notch = rotorDto.getNotch(); // 1-based index

            // RotorImpl(String alphabet, String rightSeq, String leftSeq, int notchBase1, int initialPosition)
            Rotor rotor = new RotorImpl(abc, rightSeq, leftSeq, notch, id);
            rotorsById.put(id, rotor);
        }

        // ---- build reflectors map: numericId -> Reflector ----
        Map<Integer, Reflector> reflectorsById = new HashMap<>();
        for (BTEReflector reflectorDto : dto.getBTEReflectors().getBTEReflector()) {
            int numericId = romanToInt(reflectorDto.getId());
            Map<Integer, Integer> mapping = buildReflectorMapping(reflectorDto, abc.length());

            Reflector reflector = new ReflectorImpl(numericId, mapping);
            reflectorsById.put(numericId, reflector);
        }

        // InMemoryRepository ctor internally calls store(...)
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
        // Similarly, build a string of left-letters in the same order
        StringBuilder sb = new StringBuilder();

        for (BTEPositioning pos : rotorDto.getBTEPositioning()) {
            char left = Character.toUpperCase(pos.getLeft().charAt(0));
            sb.append(left);
        }

        return sb.toString();
    }

    private Map<Integer, Integer> buildReflectorMapping(BTEReflector reflectorDto, int abcLength) {
        Map<Integer, Integer> mapping = new HashMap<>();

        for (BTEReflect reflect : reflectorDto.getBTEReflect()) {
            int input = reflect.getInput();   // 1-based
            int output = reflect.getOutput(); // 1-based

            int inIndex = input - 1;   // convert to 0-based
            int outIndex = output - 1; // convert to 0-based

            mapping.put(inIndex, outIndex);
            mapping.put(outIndex, inIndex); // reflectors are symmetric
        }

        return mapping;
    }

    // TODO optional- not sure if needed
    public Repository getRepository() {
        return repository;
    }

    private void buildCodeAndSet(List<Integer> rotorIdsRightToLeft, String initialPositionsRightToLeft, int reflectorIdNumeric) {

        String abc = repository.getAbc();
        int rotorsCount = rotorIdsRightToLeft.size();

        List<Rotor> rotors = new ArrayList<>(rotorsCount);
        List<Integer> notchOffsetsRightToLeft = new ArrayList<>(rotorsCount);

        for (int i = 0; i < rotorsCount; i++) {
            int rotorId = rotorIdsRightToLeft.get(i);
            Rotor rotor = repository.getRotor(rotorId);

            char pos = initialPositionsRightToLeft.charAt(i);
            int index = abc.indexOf(pos);

            rotor.setPosition(index);
            rotors.add(rotor);
            notchOffsetsRightToLeft.add(rotor.getNotchIndex());
        }

        Reflector reflector = repository.getReflector(reflectorIdNumeric);

        Code code = new CodeImpl(rotors, reflector);
        ensureMachineCreated();
        machine.setCode(code);

        statisticsManager.recordNewCodeConfiguration(
                rotorIdsRightToLeft,
                notchOffsetsRightToLeft,
                initialPositionsRightToLeft,
                reflectorIdNumeric
        );

    }

    private void ensureMachineCreated() { //TODO change name if needed (there is another function called ensureMachineLoaded)
        if (machine == null) {
            machine = new MachineImpl(new KeyboardImpl(repository.getAbc()));
        }
    }

    @Override
    public AutomaticCodeDTO codeAutomatic() {

//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet. Please load an XML file first.");
//        }
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

    // was added to check automatic coding
    @Override
    public String processText(String message) {

//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet. Please load an XML file first.");
//        }

        ensureRepositoryLoaded();
        ensureMachineLoaded();
        // 2. make sure a code configuration (manual/automatic) was set
//        if (machine == null) {
//            // in your design, machine is created inside buildCodeAndSet,
//            // so if machine == null there is no active code
//            throw new ConfigurationException("No code configuration defined yet. Please use commands 3 or 4 before processing text.");
//        }

        if (message == null) {
            throw new ConfigurationException("Input message must not be null.");
        }

        String abc = repository.getAbc();

        // 3. validate all characters are in the machine alphabet
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

    // was added to check automatic coding
    @Override
    public void resetToLastCode() { //TODO chet writes alone, check if it's ok
//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet. Please load an XML file first.");
//        }
        ensureRepositoryLoaded();

        CodeUsageRecord lastSession = statisticsManager.getLastSession();

        if (lastSession == null) {
            throw new ConfigurationException("No code was defined yet – cannot reset.");
        }

        List<Integer> rotorIdsRightToLeft = lastSession.getRotorIdsRightToLeft();
        String initialPositionsRightToLeft = lastSession.getInitialPositionsRightToLeft();
        int reflectorIdNumeric = lastSession.getReflectorIdNumeric();

        buildCodeAndSet(
                new ArrayList<>(rotorIdsRightToLeft),
                initialPositionsRightToLeft,
                reflectorIdNumeric
        );
    }

    @Override
    public String getHistoryAndStatistics() {
//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet – cannot show history and statistics.");
//        }
        ensureRepositoryLoaded();

        List<CodeUsageRecord> sessions = statisticsManager.getAllSessions();

        if (sessions.isEmpty()) {
            return "No codes were defined yet – history is empty.";
        }

        StringBuilder sb = new StringBuilder();
        int sessionIndex = 1;

        for (CodeUsageRecord session : sessions) {
            // א. תצורת הקוד המקורית בפורמט של פקודה 2.4
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

            // ב. כל המחרוזות + זמן ריצה
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

            sb.append(System.lineSeparator()); // רווח בין קודים שונים
            sessionIndex++;
        }

        return sb.toString();
    }

//    @Override
//    public void ensureMachineLoaded() {
//        if (repository == null) {
//            throw new ConfigurationException(
//                    "No configuration loaded yet. Please load an XML file first (command 1).");
//        }
//
//        if (machine == null) {
//            // machine נוצר רק כשמגדירים קוד דרך 3/4 (buildCodeAndSet)
//            throw new ConfigurationException(
//                    "No code configuration defined yet. Please use command 3 or 4 before processing.");
//        }
//    }

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

        // 1. Make sure configuration was loaded
//        if (repository == null) {
//            throw new ConfigurationException("No configuration loaded yet. Please load an XML file first.");
//        }
        ensureRepositoryLoaded();

        if (rotorIdsLeftToRight == null || rotorIdsLeftToRight.isEmpty()) {
            throw new ConfigurationException("You must choose at least one rotor.");
        }

        if (rotorIdsLeftToRight.size() != requiredRotorsCount) {
             throw new ConfigurationException(
                     "You must choose exactly " + requiredRotorsCount + " rotors.");
         }

        int rotorsCount = rotorIdsLeftToRight.size();

        // 2. Validate uniqueness of rotor IDs
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

        // 4. Validate initial positions length and alphabet
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

        // 5. Validate reflector ID range: 1..totalReflectors
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

        String initialPositionsRightToLeft = positionsTrimmed;

        // 7. Delegate to existing helper that builds Code, sets machine, and records statistics
        buildCodeAndSet(rotorIdsRightToLeft, initialPositionsRightToLeft, reflectorIdDecimal);
    }



}


