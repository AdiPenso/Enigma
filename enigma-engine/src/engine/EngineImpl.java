package engine;

import generated.BTEEnigma;
import generated.BTERotor;
import generated.BTEPositioning;
import generated.BTEReflector;
import generated.BTEReflect;

import loadManager.LoadManager;
import loadManager.LoadManagerImpl;
import machine.code.Code;
import machine.machine.Machine;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import machine.reflector.Reflector;
import machine.reflector.ReflectorImpl;
import machine.rotor.Rotor;
import machine.rotor.RotorImpl;
import repository.Repository;
import repository.RepositoryImpl;

import java.io.File;
import java.util.*;

public class EngineImpl implements Engine {

    private Machine machine;
    private String currentAbc;        // TODO the ABC should be in Machine or temporary variable ? cached ABC string of the last valid configuration
    private LoadManager loadManager; //TODO implement LoadManager class
    //private StatisticsManager statisticsManager; //TODO implement StatisticsManager class
    private Repository repository; //TODO implement Repository class

    public EngineImpl() {
        this.machine = null;
        this.currentAbc = null;
        this.repository = null;
        this.loadManager = new LoadManagerImpl();
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
    }


    @Override
    public void showMachineData() {

    }

    @Override
    public void codeManual() {
        Code code = null;

        //construct the code object based on manual input

        machine.setCode(code);
    }

    @Override
    public void codeAutomatic() {
        Code code = null;

        //construct the code object based on automatic generation

        machine.setCode(code);
    }

    @Override
    public String process(String input) {
        //TODO check the difference between this function and MessageProcessor class
        char[] result = new char[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            result[i] = machine.processChar(c);
        }
        return new String(result);
    }

    @Override
    public void statistics() {

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

    private int romanToInt(String roman) {
        // We only need I..V according to the exercise
        return switch (roman) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> throw new ConfigurationException("Invalid roman numeral for reflector id: " + roman);
        };
    }

    private Repository buildRepository(BTEEnigma dto) {
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








}
