package console;

import dto.AutomaticCodeDTO;
import dto.MachineSpecificationDTO;
import engine.ConfigurationException;
import engine.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static engine.Utils.intToRoman;

public class ConsoleUI {
    private final Engine engine;
    private final Scanner scanner;


    public ConsoleUI(Engine engine) {
        this.engine = engine;
        scanner = new Scanner(System.in);
    }

     public void showMenu() {
        boolean exit = false;

        while (!exit) {
            printMainMenu();

            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1":
                    handleLoadMachineFromFile();
                    break;
                case "2":
                    handleShowMachineSpecification();
                    break;
                case "3":
                    handleManualCodeConfiguration();
                    break;
                case "4":
                    handleAutomaticCodeConfiguration();
                    break;
                case "5":
                    handleProcessInput();
                    break;
                case "6":
                    handleResetCode();
                    break;
                case "7":
                    handleHistoryAndStatistics();
                    break;
                case "8":
                    System.out.println("Exiting. Goodbye!");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1–8.");
            }

            if (!exit) {
                System.out.println();
                System.out.println("Press ENTER to return to the main menu...");
                scanner.nextLine();
            }
        }
    }

    private void printMainMenu() {
        System.out.println("====================================");
        System.out.println("        ENIGMA MACHINE - MENU       ");
        System.out.println("====================================");
        System.out.println("1. Load machine configuration from XML file");
        System.out.println("2. Show machine specifications");
        System.out.println("3. Set manual code configuration");
        System.out.println("4. Set automatic (random) code configuration");
        System.out.println("5. Process input string");
        System.out.println("6. Reset current code to original");
        System.out.println("7. Show history and statistics");
        System.out.println("8. Exit");
        System.out.print  ("Please choose an option (1-8): ");
    }

    private void handleLoadMachineFromFile() {
        System.out.print("Please enter full XML file path: ");
        String xmlPath = scanner.nextLine().trim();

        try {
            engine.loadXml(xmlPath);

            System.out.println("XML configuration loaded successfully.");
            System.out.println("You may now view machine specs (2) or configure a code (3/4).");

        } catch (ConfigurationException e) {
            System.out.println("Failed to load machine configuration.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please correct the issue or try a different file.");

        } catch (Exception e) {
            System.out.println("An unexpected error occurred while loading the XML file.");
            System.out.println("Details: " + e.getMessage());
            System.out.println("The system remains unchanged. You may continue.");
        }
    }

    private void handleShowMachineSpecification() {
        try {

            MachineSpecificationDTO machineSpecification = engine.getMachineSpecification();

            System.out.println("Machine specifications:");
            System.out.println("-----------------------");

            System.out.println("Total rotors in configuration: " + machineSpecification.getTotalRotorsCount());
            System.out.println("Total reflectors in configuration: " + machineSpecification.getTotalReflectorsCount());
            System.out.println("Total processed messages since last XML load: "
                    + machineSpecification.getTotalProcessedMessages());

            System.out.println();

            System.out.println("Original code configuration (last code set by commands 3/4):");
            if (machineSpecification.getOriginalCodeConfiguration() != null) {
                System.out.println(machineSpecification.getOriginalCodeConfiguration());
            } else {
                System.out.println("<none>");
            }

            System.out.println();
            System.out.println("Current code configuration:");
            if (machineSpecification.getCurrentCodeConfiguration() != null) {
                System.out.println(machineSpecification.getCurrentCodeConfiguration());
            } else {
                System.out.println("<none>");
            }

        } catch (ConfigurationException e) {
            System.out.println("Cannot show machine specifications.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please load a valid XML file first (command 1).");

        } catch (Exception e) {
            System.out.println("An unexpected error occurred while retrieving machine specifications.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private void handleManualCodeConfiguration() {
        try {
            MachineSpecificationDTO spec = engine.getMachineSpecification();
            int totalReflectors = spec.getTotalReflectorsCount();

            System.out.println("Enter rotor IDs as a comma-separated list (left to right).");
            System.out.println("Example: 23,542,231");
            System.out.print("Rotors: ");
            String rotorsInput = scanner.nextLine().trim();

            if (rotorsInput.isEmpty()) {
                System.out.println("Rotors list must not be empty. Returning to main menu.");
                return;
            }
            if (!rotorsInput.matches("\\d+(\\s*,\\s*\\d+)*")) {
                System.out.println("Invalid format for rotor list.");
                System.out.println("Please enter decimal numbers separated by commas, e.g.: 23,542,231,545");
                return;
            }

            List<Integer> rotorIdsLeftToRight = new ArrayList<>();
            try {
                String[] parts = rotorsInput.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        rotorIdsLeftToRight.add(Integer.parseInt(trimmed));
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid rotor IDs. Please enter decimal numbers only, separated by commas.");
                return;
            }

            System.out.println();
            System.out.println("Enter initial rotor positions as a continuous string.");
            System.out.println("Example: A8D");
            System.out.print("Positions: ");
            String positionsInput = scanner.nextLine().trim();

            if (positionsInput.isEmpty()) {
                System.out.println("Positions string must not be empty. Returning to main menu.");
                return;
            }

            System.out.println();
            System.out.println("Choose reflector by index (decimal):");
            for (int i = 1; i <= totalReflectors; i++) {
                System.out.println(i + ". Reflector " + i);
            }
            System.out.print("Reflector index: ");
            String reflectorIndexStr = scanner.nextLine().trim();

            int reflectorIndexDecimal;
            try {
                reflectorIndexDecimal = Integer.parseInt(reflectorIndexStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid reflector index. Please enter a decimal number between 1 and " + totalReflectors + ".");
                return;
            }

            engine.setManualCodeConfiguration(
                    rotorIdsLeftToRight,
                    positionsInput,
                    reflectorIndexDecimal
            );

            System.out.println();
            System.out.println("Manual code configuration has been set successfully.");

        } catch (ConfigurationException e) {
            System.out.println("Failed to set manual code configuration.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please correct the input and try again from the menu.");

        } catch (Exception e) {
            System.out.println("An unexpected error occurred while setting manual code configuration.");
            System.out.println("Details: " + e.getMessage());
        }
    }

    private void handleAutomaticCodeConfiguration() {
        try {
            AutomaticCodeDTO autoCode = engine.codeAutomatic();

            System.out.println("A random code configuration has been chosen and set as the active code.");
            System.out.println();

            String rotorsStr = autoCode.getRotorIdsRightToLeft()
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            System.out.println("Chosen rotors (right-to-left): " + rotorsStr);
            System.out.println("Initial positions (right-to-left): "
                    + autoCode.getInitialPositionsRightToLeft());

            int reflectorId = autoCode.getReflectorIdNumeric();
            String reflectorRoman = intToRoman(reflectorId);

            System.out.println("Chosen reflector: " + reflectorId + " (" + reflectorRoman + ")");

        } catch (ConfigurationException e) {
            System.out.println("Cannot set automatic code configuration.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please make sure a valid XML file is loaded first (command 1).");

        } catch (Exception e) {
            System.out.println("An unexpected error occurred while setting automatic code configuration.");
            System.out.println("Details: " + e.getMessage());
        }
    }


    private void handleProcessInput() {
        try {
            engine.ensureMachineLoaded();
            System.out.println("Please enter the text you want to process:");
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                System.out.println("Input message must not be empty. Returning to main menu.");
                return;
            }

            String output = engine.processText(input);

            System.out.println();
            System.out.println("Processing result:");
            System.out.printf("<%s> --> <%s>%n", input, output);

        } catch (ConfigurationException e) {
            System.out.println("Cannot process input:");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Please fix the problem and try again.");

        } catch (Exception e) {
            System.out.println("An unexpected error occurred while processing the input.");
            System.out.println("Details: " + e.getMessage());
        }
    }

private void handleResetCode() {
    try {
        engine.resetToLastCode();

        System.out.println("The machine has been reset to the last code configuration defined by commands 3 or 4.");
    } catch (ConfigurationException e) {
        System.out.println("Cannot reset code configuration.");
        System.out.println("Reason: " + e.getMessage());
        System.out.println("Make sure a valid XML file is loaded and a code was defined using command 3 or 4.");

    } catch (Exception e) {
        System.out.println("An unexpected error occurred while resetting the code.");
        System.out.println("Details: " + e.getMessage());
    }
}


private void handleHistoryAndStatistics() {
    try {
        String history = engine.getHistoryAndStatistics();

        System.out.println("History & statistics:");
        System.out.println("----------------------");
        System.out.println(history);

    } catch (ConfigurationException e) {
        System.out.println("Cannot show history and statistics.");
        System.out.println("Reason: " + e.getMessage());
        System.out.println("Please load a valid XML file first (command 1).");

    } catch (Exception e) {
        System.out.println("An unexpected error occurred while retrieving history and statistics.");
        System.out.println("Details: " + e.getMessage());
    }
}
}

