package testsForEngine;

import engine.ConfigurationException;
import engine.Engine;
import engine.EngineImpl;

public class PlayGround {
    public static void main(String[] args) {
        final Engine engine = new EngineImpl(); // הנחה שיש מימוש של המנוע בשם EngineImpl
        String goodPath = "C:\\Users\\user\\Desktop\\LastYear\\PTML\\testingFiles\\ex1-sanity-small.xml";
        String badPath  = "C:\\Users\\user\\Desktop\\LastYear\\PTML\\testingFiles\\ex1-error-8.xml";
        System.out.println("=== Loading GOOD file ===");
        try {
            engine.loadXml(goodPath);
            System.out.println("GOOD file loaded successfully.");
        } catch (Exception e) {
            System.out.println("Unexpected failure on GOOD file: " + e.getMessage());
        }

        //System.out.println("Repository after GOOD file: " + (engine.getRepository() == null ? "NULL" : "NOT NULL"));

        System.out.println("\n=== Loading BAD file ===");
        try {
            engine.loadXml(badPath);
            System.out.println("BAD file loaded successfully (should NOT happen!)");
        } catch (ConfigurationException e) {
            System.out.println("BAD file failed as expected: " + e.getMessage());
        }

       // System.out.println("Repository after BAD file: " + (engine.getRepository() == null ? "NULL" : "NOT NULL"));
    }
}
