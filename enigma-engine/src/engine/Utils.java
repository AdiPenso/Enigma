package engine;

public class Utils {

    public static String intToRoman(int num) {
        // According to the exercise, only 1..5 are valid
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> throw new ConfigurationException("Invalid numeric reflector id: " + num);
        };
    }

    public static int romanToInt(String roman) {
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

}
